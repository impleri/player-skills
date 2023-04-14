package net.impleri.playerskills.server.registry;

import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.server.registry.storage.SkillStorage;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PlayerSkills {
    /**
     * MUTABLE in-memory cache
     */
    private static final Map<UUID, List<Skill<?>>> players = new HashMap<>();

    /**
     * Helper function to save to in-memory cache only
     */
    private static void saveToCache(UUID playerUuid, List<Skill<?>> skills) {
        players.put(playerUuid, skills);
    }

    /**
     * Helper function to save to in-memory cache AND persistent storage
     */
    private static void save(UUID playerUuid, List<Skill<?>> skills) {
        saveToCache(playerUuid, skills);

        writeToStorage(playerUuid, skills);
    }

    /**
     * Get a copy of the in memory cache
     */
    public static Map<UUID, List<Skill<?>>> entries() {
        return new HashMap<>(players);
    }

    /**
     * Read-through internal cache. Gets player's skills in cache map and reads from storage if not found in cache
     */
    private static List<Skill<?>> getFor(UUID playerUuid) {
        var cachedSkills = players.get(playerUuid);

        if (cachedSkills == null) {
            return readFromStorage(playerUuid);
        }

        return cachedSkills;
    }

    /**
     * Get all skills for a player from the in-memory cache
     */
    public static List<Skill<?>> getAllForPlayer(UUID playerUuid) {
        return getFor(playerUuid);
    }

    public static <T> Long countValue(List<UUID> playerIds, Skill<T> skill) {
        SkillType<T> skillType = SkillType.maybeForSkill(skill);
        if (skillType == null) {
            return 0L;
        }

        return playerIds.stream()
                .filter(playerId -> {
                    var isActive = players.containsKey(playerId);

                    if (!isActive) {
                        openPlayer(playerId);
                    }

                    Optional<Skill<T>> current = filterSkill(getFor(playerId), skill.getName());
                    var can = current.map(currentSkill -> skillType.can(currentSkill, skill.getValue())).orElseGet(() -> false);

                    if (!isActive) {
                        closePlayer(playerId);
                    }

                    return can;
                })
                .count();
    }

    public static List<Skill<?>> handleOpenFor(UUID playerUuid) {
        net.impleri.playerskills.PlayerSkills.LOGGER.info("Opening player {}, ensuring saved skills are still valid", playerUuid);

        var registeredSkills = Skills.entries();
        // Get all the names of the registered skills
        List<ResourceLocation> registeredSkillNames = registeredSkills.stream()
                .map(Skill::getName)
                .toList();

        var rawSkills = getFor(playerUuid);
        net.impleri.playerskills.server.api.Skill.logSkills(rawSkills, "Found saved skills for player");

        // Get an intersection of saved skills that are still registered
        List<Skill<?>> savedSkills = rawSkills.stream()
                .filter(skill -> registeredSkillNames.contains(skill.getName()))
                .toList();

        net.impleri.playerskills.server.api.Skill.logSkills(savedSkills, "Saved skills still registered for player");

        List<ResourceLocation> savedSkillNames = savedSkills.stream()
                .map(Skill::getName)
                .toList();

        List<Skill<?>> newSkills = registeredSkills.stream()
                .filter(skill -> !savedSkillNames.contains(skill.getName()))
                .toList();

        net.impleri.playerskills.server.api.Skill.logSkills(newSkills, "Appending registerd skills for player");

        return Stream.concat(savedSkills.stream(), newSkills.stream()).toList();
    }

    /**
     * Instantiates the in-memory cache for a player. Note that this will automatically prune saved skills that do not
     * match the skill type in the Skills Registry as well as those which no longer exist at all in the Skills Registery
     */
    public static void openPlayer(UUID playerUuid) {
        var skills = handleOpenFor(playerUuid);

        // Immediately sync in-memory cache AND persistent storage with updated skills set
        save(playerUuid, skills);
    }

    public static List<UUID> openPlayers(List<UUID> playerUuids) {
        var playersAdded = playerUuids.stream().filter(playerId -> !players.containsKey(playerId)).toList();

        Map<UUID, List<Skill<?>>> skillsList = playerUuids.stream()
                .collect(Collectors.toMap(Function.identity(), net.impleri.playerskills.server.registry.PlayerSkills::handleOpenFor));


        players.putAll(skillsList);

        return playersAdded;
    }

    /**
     * Helper function to upsert a skill for a player in memory only
     */
    private static List<Skill<?>> handleUpsert(UUID playerUuid, Skill<?> skill) {
        List<Skill<?>> existingSkills = getAllForPlayer(playerUuid);

        // Only replace skill with same name AND type (edge case of same name but different type is handled in openPlayer)
        List<Skill<?>> filteredSkills = List.copyOf(existingSkills).stream()
                .filter(existing -> !skill.getName().equals(existing.getName()))
                .toList();

        List<Skill<?>> addedSkills = new ArrayList<>();
        addedSkills.add(skill);

        List<Skill<?>> newSkills = Stream.concat(filteredSkills.stream(), addedSkills.stream()).toList();
        saveToCache(playerUuid, newSkills);

        return newSkills;
    }

    /**
     * Upsert a skill for a player and saves to persistent storage
     */
    public static List<Skill<?>> upsert(UUID playerUuid, Skill<?> skill) {
        List<Skill<?>> newSkills = handleUpsert(playerUuid, skill);

        save(playerUuid, newSkills);

        return newSkills;
    }

    public static <T> Map<UUID, Skill<T>> upsertMany(List<UUID> playerIds, Skill<T> skill) {
        Map<UUID, Skill<T>> updates = new HashMap<>();

        SkillType<T> skillType = SkillType.maybeForSkill(skill);

        playerIds.forEach(playerId -> {
            var isActive = players.containsKey(playerId);

            if (!isActive) {
                openPlayer(playerId);
            }

            var oldSkills = getFor(playerId);
            Optional<Skill<T>> oldSkillOption = filterSkill(oldSkills, skill.getName());
            Skill<T> oldSkill = oldSkillOption.orElse(skill);

            // Only update if the new skill is better or we can't tell if it is
            if (skillType == null || oldSkillOption.isEmpty() || !skillType.can(oldSkill, skill.getValue())) {
                upsert(playerId, skill);
                if (isActive) {
                    updates.put(playerId, oldSkill);
                }
            }

            if (!isActive) {
                closePlayer(playerId);
            }
        });

        return updates;
    }

    /**
     * Add a skill to a player only if the player does not have it
     */
    public static List<Skill<?>> add(UUID playerUuid, Skill<?> skill) {
        List<Skill<?>> existingSkills = getAllForPlayer(playerUuid);

        // Skill already exists, so do nothing
        if (!existingSkills.stream().filter(skill::equals).toList().isEmpty()) {
            return existingSkills;
        }

        List<Skill<?>> newSkills = handleUpsert(playerUuid, skill);

        save(playerUuid, newSkills);

        return newSkills;
    }

    /**
     * Completely remove a skill from a player
     */
    public static List<Skill<?>> remove(UUID playerUuid, ResourceLocation name) {
        List<Skill<?>> existingSkills = getFor(playerUuid);
        List<Skill<?>> newSkills = existingSkills.stream().filter(existing -> existing.getName() != name).toList();

        save(playerUuid, newSkills);

        return newSkills;
    }

    private static void handleCloseFor(UUID playerUuid) {
        net.impleri.playerskills.PlayerSkills.LOGGER.info("Closing player {}, ensuring skills are saved", playerUuid);

        List<Skill<?>> skills = getFor(playerUuid);
        writeToStorage(playerUuid, skills);
    }

    /**
     * Save a player's skills to persistent storage then remove player from in-memory cache
     */
    public static void closePlayer(UUID playerUuid) {
        handleCloseFor(playerUuid);

        players.remove(playerUuid);
    }

    public static void closePlayers(List<UUID> playerIds) {
        playerIds.stream().parallel().forEach(net.impleri.playerskills.server.registry.PlayerSkills::handleCloseFor);
        playerIds.stream().parallel().forEach(players::remove);
    }

    /**
     * Save multiple players' skills to persistent storage then remove them from in-memory cache
     */
    public static List<UUID> closeAllPlayers() {
        List<UUID> playerIds = players.keySet().stream().toList();
        closePlayers(playerIds);

        // Wipe away any players that are somehow still present
        players.clear();

        return playerIds;
    }

    private static List<Skill<?>> readFromStorage(UUID playerUuid) {
        net.impleri.playerskills.PlayerSkills.LOGGER.debug("Restoring saved skills for {}", playerUuid);

        return SkillStorage.read(playerUuid).stream()
                .<Skill<?>>map(SkillType::unserializeFromString)
                .filter(Objects::nonNull)
                .toList();
    }

    private static void writeToStorage(UUID playerUuid, List<Skill<?>> skills) {
        net.impleri.playerskills.PlayerSkills.LOGGER.debug("Saving skills for {}", playerUuid);

        List<String> rawSkills = skills.stream()
                .map(SkillType::serializeToString)
                .filter(s -> s.length() > 0)
                .toList();

        SkillStorage.write(playerUuid, rawSkills);
    }

    public static <T> Optional<Skill<T>> filterSkill(List<Skill<?>> playerSkills, ResourceLocation name) {
        return playerSkills.stream()
                .filter(skill -> skill.getName().equals(name))
                .map(skill -> {
                    @SuppressWarnings("unchecked") Skill<T> castSkill = (Skill<T>) skill;
                    return castSkill;
                })
                .findFirst();
    }
}
