package net.impleri.playerskills.api;

import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.integration.kubejs.PlayerSkillsPlugin;
import net.impleri.playerskills.registry.PlayerSkills;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.registry.Skills;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class PlayerSkill {
    /**
     * Get all Skills for a Player
     */
    public static List<Skill<?>> getAllSkills(Player player) {
        return PlayerSkills.getAllForPlayer(player.getUUID());
    }

    /**
     * Get a specific Skill for a player
     */
    public static <T> Skill<T> getSkill(Player player, ResourceLocation name) throws RegistryItemNotFound {
        List<Skill<?>> playerSkills = getAllSkills(player);
        Skill.logSkills(playerSkills, "All skills for " + player.getName().getString());

        @Nullable Skill<T> defaultSkill = Skills.find(name);

        Optional<Skill<T>> foundSkill = playerSkills.stream()
                .filter(skill -> skill.getName().equals(name))
                .map(skill -> {
                    @SuppressWarnings("unchecked") Skill<T> castSkill = (Skill<T>) skill;
                    return castSkill;
                })
                .findFirst();

        if (foundSkill.isEmpty()) {
            PlayerSkillsCore.LOGGER.warn("Could not find {} for player {}", name, player.getName().getString());
            return defaultSkill;
        }

        return foundSkill.get();
    }

    public static <T> Skill<T> getSkill(Player player, String name) throws RegistryItemNotFound {
        return getSkill(player, SkillResourceLocation.of(name));
    }

    /**
     * Determines if a Player has a skill using a string identifier, optionally at an expected value
     */
    public static <T> boolean can(Player player, String skillName, @Nullable T expectedValue) {
        return can(player, SkillResourceLocation.of(skillName), expectedValue);
    }

    public static <T> boolean can(Player player, String skillName) {
        return can(player, skillName, null);
    }

    /**
     * Determines if a Player has a skill using a ResourceLocation, optionally at an expected value
     */
    public static <T> boolean can(Player player, ResourceLocation skillName, @Nullable T expectedValue) {
        try {
            Skill<T> skill = getSkill(player, skillName);
            return can(player, skill, expectedValue);
        } catch (RegistryItemNotFound e) {
            PlayerSkillsCore.LOGGER.warn("No default found for {} skill when checking if {} has {}", skillName, player.getName(), expectedValue);
        }

        return false;
    }

    public static <T> boolean can(Player player, ResourceLocation skillName) {
        return can(player, skillName, null);
    }

    public static <T> boolean can(Player player, Skill<T> skill, @Nullable T expectedValue) {
        try {
            return SkillType.forSkill(skill).can(skill, expectedValue);
        } catch (RegistryItemNotFound e) {
            PlayerSkillsCore.LOGGER.warn("No skill type found for {} to check if {} has {}", skill.getName(), player.getName(), expectedValue);
        }

        return false;
    }

    public static <T> boolean can(Player player, Skill<T> skill) {
        return can(player, skill, null);
    }

    /**
     * Upsert a Skill at the specified value (or its default if none provided) for a Player
     */
    public static <T> boolean setByName(Player player, String skill, @Nullable T newValue) {
        return set(player, SkillResourceLocation.of(skill), newValue);
    }

    /**
     * Upsert a Skill at the specified value (or its default if none provided) for a Player
     */
    public static <T> boolean set(Player player, ResourceLocation skillName, @Nullable T newValue) {
        try {
            Skill<T> skill = Skill.find(skillName);
            return set(player, skill, newValue);
        } catch (RegistryItemNotFound e) {
            PlayerSkillsCore.LOGGER.warn("No skill {} in the registry to set for {}", skillName, player.getName());
        }

        return false;
    }

    /**
     * Upsert a Skill at the specified value (or its default if none provided) for a Player
     */
    public static <T> boolean set(Player player, Skill<T> skill, @Nullable T newValue) throws RegistryItemNotFound {
        T value = (newValue == null) ? skill.getValue() : newValue;

        Skill<T> oldSkill = getSkill(player, skill.getName());

        Skill<T> newSkill = skill.copy();
        newSkill.setValue(value);

        if (oldSkill.getValue() == newSkill.getValue()) {
            return false;
        }

        List<Skill<?>> newSkills = PlayerSkills.upsert(player.getUUID(), newSkill);

        PlayerSkillsPlugin.onSkillChange(player, newSkill, oldSkill);

        // TODO: Sync to player client

        return newSkills.contains(newSkill);
    }

    /**
     * Reset a Skill to the default value in the Skills registry
     */
    public static <T> boolean reset(Player player, String skill) {
        return reset(player, SkillResourceLocation.of(skill));
    }

    /**
     * Reset a Skill to the default value in the Skills registry
     */
    public static <T> boolean reset(Player player, ResourceLocation skill) {
        return set(player, skill, null);
    }

    /**
     * Completely remove a Skill from a Player
     */
    public static <T> boolean revoke(Player player, ResourceLocation skillName) {
        try {
            return revoke(player, Skill.find(skillName));
        } catch (RegistryItemNotFound e) {
            PlayerSkillsCore.LOGGER.warn("No skill {} in the registry to revoke for {}", skillName, player.getName());
        }

        return true;
    }

    /**
     * Completely remove a Skill from a Player
     */
    public static <T> boolean revoke(Player player, Skill<T> skill) {
        // Reset skill to default value
        List<Skill<?>> newSkills = PlayerSkills.remove(player.getUUID(), skill.getName());

        return newSkills.stream().noneMatch(skill::isSameAs);
    }
}
