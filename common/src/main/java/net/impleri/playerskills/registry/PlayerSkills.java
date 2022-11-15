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
        return players.computeIfAbsent(playerUuid, PlayerSkills::readFromStorage);
    }

    /**
     * Get all skills for a player from the in-memory cache
     */
    public static List<Skill<?>> getAllForPlayer(UUID playerUuid) {
        return players.get(playerUuid);
    }

    /**
     * Instantiates the in-memory cache for a player. Note that this will automatically prune saved skills that do not
     * match the skill type in the Skills Registry as well as those which no longer exist at all in the Skills Registery
     */
    public static List<Skill<?>> openPlayer(UUID playerUuid, List<Skill<?>> registeredSkills) {
        // Get all saved skills which still match name AND type of registered skills
        Stream<Skill<?>> savedSkills = getFor(playerUuid).stream()
                .filter(skill -> !registeredSkills.stream().filter(skill::equals).toList().isEmpty());

        // Get all registered skills not saved
        List<Skill<?>> finalSavedSkills = savedSkills.toList();
        Stream<Skill<?>> newSkills = registeredSkills.stream()
                .filter(skill -> !finalSavedSkills.contains(skill));

        List<Skill<?>> skills = Stream.concat(finalSavedSkills.stream(), newSkills).toList();

        // Immediately sync in-memory cache AND persistent storage with updated skills set
        save(playerUuid, skills);

        return skills;
    }

    /**
     * Helper function to upsert a skill for a player in memory only
     */
    private static List<Skill<?>> handleUpsert(UUID playerUuid, Skill<?> skill) {
        List<Skill<?>> existingSkills = getFor(playerUuid);

        // Only replace skill with same name AND type (edge case of same name but different type is handled in openPlayer)
        List<Skill<?>> filteredSkills = existingSkills.stream()
                .filter(skill::equals)
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
    public static void closeAllPlayers() {
        players.keySet().forEach(PlayerSkills::closePlayer);
    }

    private static List<Skill<?>> readFromStorage(UUID playerUuid) {
        return SkillStorage.read(playerUuid).stream()
                .<Skill<?>>map(PlayerSkills::transformFromStorage)
                .filter(Objects::nonNull)
                .toList();
    }

    private static @Nullable <T> Skill<T> transformFromStorage(String rawSkill) {
        if (rawSkill == null || rawSkill == "") {
            return null;
        }

        PlayerSkillsCore.LOGGER.debug("Hydrating skill {} from storage", rawSkill);

        String[] elements = SkillType.splitRawSkill(rawSkill);
        String name = elements[0];
        String type = elements[1];
        String value = elements[2];

        try {
            SkillType<T> skillType = SkillTypes.find(SkillResourceLocation.of(type));
            return skillType.unserialize(name, value);
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
        SkillType<T> type = SkillTypes.find(skill.getType());

        PlayerSkillsCore.LOGGER.debug("Dehydrating skill {} for storage", skill.getName());

        return type.serialize(skill);
    }
}
