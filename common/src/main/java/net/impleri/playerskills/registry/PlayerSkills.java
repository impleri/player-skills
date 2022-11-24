package net.impleri.playerskills.registry;

import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.registry.storage.SkillStorage;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
     * Get an IMMUTABLE copy of the in memory cache
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
            PlayerSkillsCore.LOGGER.info("Pulling {}'s skills from the freezer", playerUuid);
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

    /**
     * Instantiates the in-memory cache for a player. Note that this will automatically prune saved skills that do not
     * match the skill type in the Skills Registry as well as those which no longer exist at all in the Skills Registery
     */
    public static List<Skill<?>> openPlayer(UUID playerUuid, List<Skill<?>> registeredSkills) {
        // Get all the names of the registered skills
        List<ResourceLocation> registeredSkillNames = registeredSkills.stream()
                .map(Skill::getName)
                .toList();

        var rawSkills = getFor(playerUuid);
        Skill.logSkills(rawSkills, "Found saved skills for player");

        // Get an intersection of saved skills that are still registered
        List<Skill<?>> savedSkills = rawSkills.stream()
                .filter(skill -> registeredSkillNames.contains(skill.getName()))
                .toList();

        Skill.logSkills(savedSkills, "Saved skills still registered for player");

        List<ResourceLocation> savedSkillNames = savedSkills.stream()
                .map(Skill::getName)
                .toList();

        List<Skill<?>> newSkills = registeredSkills.stream()
                .filter(skill -> !savedSkillNames.contains(skill.getName()))
                .toList();

        Skill.logSkills(newSkills, "Appending registerd skills for player");

        List<Skill<?>> skills = Stream.concat(savedSkills.stream(), newSkills.stream()).toList();

        // Immediately sync in-memory cache AND persistent storage with updated skills set
        save(playerUuid, skills);

        return skills;
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

    /**
     * Save a player's skills to persistent storage then remove player from in-memory cache
     */
    public static void closePlayer(UUID playerUuid) {
        List<Skill<?>> skills = getFor(playerUuid);
        save(playerUuid, skills);

        players.remove(playerUuid);
    }

    /**
     * Save multiple players' skills to persistent storage then remove them from in-memory cache
     */
    public static Set<UUID> closeAllPlayers() {
        Set<UUID> playerIds = players.keySet();
        playerIds.forEach(PlayerSkills::closePlayer);

        return playerIds;
    }

    public static void resyncPlayers(Set<UUID> players, List<Skill<?>> registeredSkills) {
        players.forEach(player -> openPlayer(player, registeredSkills));
    }

    private static List<Skill<?>> readFromStorage(UUID playerUuid) {
        return SkillStorage.read(playerUuid).stream()
                .<Skill<?>>map(PlayerSkills::transformFromStorage)
                .filter(Objects::nonNull)
                .toList();
    }

    private static @Nullable <T> Skill<T> transformFromStorage(String rawSkill) {
        PlayerSkillsCore.LOGGER.debug("Unpacking skill {} from storage", rawSkill);

        if (rawSkill == null || rawSkill.equals("")) {
            return null;
        }

        String[] elements = SkillType.splitRawSkill(rawSkill);
        String name = elements[0];
        String type = elements[1];
        String value = elements[2];
        int changesAllowed;

        try {
            changesAllowed = Integer.parseInt(elements[3]);
        } catch (NumberFormatException e) {
            PlayerSkillsCore.LOGGER.error("Unable to parse changesAllowed ({}) back into an integer, data possibly corrupted", elements[3]);
            return null;
        }

        List<String> options = Arrays.stream(elements).skip(4).toList();

        try {
            SkillType<T> skillType = SkillTypes.find(SkillResourceLocation.of(type));
            PlayerSkillsCore.LOGGER.debug("Hydrating {} skill named {}: {}", type, name, value);
            return skillType.unserialize(name, value, changesAllowed, options);
        } catch (RegistryItemNotFound e) {
            PlayerSkillsCore.LOGGER.warn("No skill type {} in the registry to hydrate {}", type, name);
        }

        return null;
    }

    private static void writeToStorage(UUID playerUuid, List<Skill<?>> skills) {
        List<String> rawSkills = skills.stream().map(skill -> {
                    try {
                        return serializeSkill(skill);
                    } catch (RegistryItemNotFound e) {
                        PlayerSkillsCore.LOGGER.warn("No skill type {} in the registry to serialize {}", skill.getType(), skill.getName());
                    }

                    return null;
                }).filter(Objects::nonNull)
                .toList();

        SkillStorage.write(playerUuid, rawSkills);
    }

    private static <T> String serializeSkill(Skill<T> skill) throws RegistryItemNotFound {
        PlayerSkillsCore.LOGGER.debug("Serializing skill {} with type {}", skill.getName(), skill.getType());
        SkillType<T> type = SkillTypes.find(skill.getType());
        String storage = type.serialize(skill);

        PlayerSkillsCore.LOGGER.debug("Dehydrating skill {} for storage: {}", skill.getName(), storage);

        return type.serialize(skill);
    }
}
