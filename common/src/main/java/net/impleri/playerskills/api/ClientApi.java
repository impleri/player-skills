package net.impleri.playerskills.api;

import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ClientApi {
    private static final List<Skill<?>> playerSkills = new ArrayList<>();

    @ApiStatus.Internal
    public static void syncFromServer(List<Skill<?>> skills) {
        var prev = playerSkills.stream().toList();

        playerSkills.clear();

        PlayerSkillsCore.LOGGER.debug("Syncing Client-side skills: {}", skills.stream().map(s -> {
            var value = s.getValue() == null ? "NULL" : s.getValue();
            return "" + s.getName() + "=" + value;
        }).collect(Collectors.joining(", ")));

        playerSkills.addAll(skills);

        ClientSkillsUpdatedEvent.EVENT.invoker().act(new ClientSkillsUpdatedEvent(skills, prev));
    }

    /**
     * Determines if a Player has a skill using a string identifier, optionally at an expected value
     */
    public static <T> boolean can(String skillName, @Nullable T expectedValue) {
        return can(SkillResourceLocation.of(skillName), expectedValue);
    }

    public static <T> boolean can(String skillName) {
        return can(skillName, null);
    }

    /**
     * Determines if a Player has a skill using a ResourceLocation, optionally at an expected value
     */
    public static <T> boolean can(ResourceLocation skillName, @Nullable T expectedValue) {
        Optional<Skill<?>> foundSkill = playerSkills.stream().filter(skill -> skill.getName().equals(skillName)).findFirst();
        if (foundSkill.isEmpty()) {
            return false;
        }

        return can((Skill<T>) foundSkill.get(), expectedValue);
    }

    public static <T> boolean can(ResourceLocation skillName) {
        return can(skillName, null);
    }

    public static <T> boolean can(Skill<T> skill, @Nullable T expectedValue) {
        try {
            return SkillType.forSkill(skill).can(skill, expectedValue);
        } catch (RegistryItemNotFound e) {
            PlayerSkillsCore.LOGGER.warn("No skill type found for {} to check if {} has {}", skill.getName(), expectedValue);
        }

        return false;
    }

    public static <T> boolean can(Skill<T> skill) {
        return can(skill, null);
    }
}
