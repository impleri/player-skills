package net.impleri.playerskills.client;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ClientApi {
    /**
     * Determines if a Player has a skill, optionally at an expected value
     */
    public static <T> boolean can(Skill<T> skill, @Nullable T expectedValue) {
        try {
            return SkillType.forSkill(skill).can(skill, expectedValue);
        } catch (RegistryItemNotFound e) {
            PlayerSkills.LOGGER.warn("No skill type found for {} to check if {} has {}", skill.getName(), expectedValue);
        }

        return false;
    }

    public static <T> boolean can(Player player, Skill<T> skill, @Nullable T expectedValue) throws MismatchedClientPlayerException {
        checkPlayer(player);
        return can(skill, expectedValue);
    }

    public static <T> boolean can(ResourceLocation skillName, @Nullable T expectedValue) {
        Optional<Skill<?>> foundSkill = Registry.get().stream()
                .filter(skill -> skill.getName().equals(skillName))
                .findFirst();
        if (foundSkill.isEmpty()) {
            return false;
        }

        return can((Skill<T>) foundSkill.get(), expectedValue);
    }

    public static <T> boolean can(Player player, ResourceLocation skillName, @Nullable T expectedValue) throws MismatchedClientPlayerException {
        checkPlayer(player);
        return can(skillName, expectedValue);
    }

    public static <T> boolean can(String skillName, @Nullable T expectedValue) {
        return can(SkillResourceLocation.of(skillName), expectedValue);
    }

    public static <T> boolean can(Player player, String skillName, @Nullable T expectedValue) throws MismatchedClientPlayerException {
        checkPlayer(player);
        return can(skillName, expectedValue);
    }

    public static <T> boolean can(Skill<T> skill) {
        return can(skill, null);
    }

    public static <T> boolean can(ResourceLocation skillName) {
        return can(skillName, null);
    }

    public static <T> boolean can(String skillName) {
        return can(skillName, null);
    }

    private static void checkPlayer(Player player) throws MismatchedClientPlayerException {
        if (!player.getLevel().isClientSide) {
            throw new MismatchedClientPlayerException();
        }

        var localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || !localPlayer.equals(player)) {
            throw new MismatchedClientPlayerException();
        }
    }
}
