package net.impleri.playerskills.server.api;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.server.registry.Skills;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Skill {
    /**
     * Get all registered Skills
     */
    public static List<net.impleri.playerskills.api.Skill<?>> all() {
        return Skills.entries();
    }

    /**
     * Find a Skill by string
     */
    public static <V> net.impleri.playerskills.api.Skill<V> find(String name) throws RegistryItemNotFound {
        return find(SkillResourceLocation.of(name));
    }

    /**
     * Find a Skill by name
     */
    public static <V> net.impleri.playerskills.api.Skill<V> find(ResourceLocation location) throws RegistryItemNotFound {
        return Skills.find(location);
    }

    @ApiStatus.Internal
    public static <V> String dumpSkill(@NotNull net.impleri.playerskills.api.Skill<V> skill) {
        return "" + skill.getName().toString() + "=" + Objects.requireNonNullElse(skill.getValue(), "null");
    }

    @ApiStatus.Internal
    public static void logSkills(@NotNull List<net.impleri.playerskills.api.Skill<?>> skills, @NotNull String description) {
        var skillList = skills.stream()
                .map(Skill::dumpSkill)
                .collect(Collectors.joining(", "));

        PlayerSkills.LOGGER.debug("{}: {}", description, skillList);
    }
}
