package net.impleri.playerskills.variant.specialized;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpecializedSkillType extends SkillType<String> {
    public static ResourceLocation name = SkillResourceLocation.of("specialized");

    @Override
    public ResourceLocation getName() {
        return name;
    }

    private List<String> getOptions(Skill<String> skill) {
        return skill.getOptions();
    }

    @Override
    protected String castToString(String value) {
        return value;
    }

    @Override
    public @Nullable String castFromString(String value) {
        return value;
    }

    @Override
    public boolean can(Skill<String> skill, @Nullable String expectedValue) {
        if (expectedValue == null) {
            return skill.getValue() != null;
        }

        PlayerSkills.LOGGER.debug("Checking if player can {} (is {} == {})", skill.getName(), skill.getValue(), expectedValue);
        return expectedValue.equals(skill.getValue());
    }

    @Override
    @Nullable
    public String getPrevValue(Skill<String> skill, @Nullable String min, @Nullable String max) {
        if (!skill.areChangesAllowed()) {
            return null;
        }

        String nextVal = min == null ? max : min;

        if (nextVal == null) {
            return null;
        }

        return (getOptions(skill).contains(nextVal)) ? nextVal : null;
    }

    @Override
    @Nullable
    public String getNextValue(Skill<String> skill, @Nullable String min, @Nullable String max) {
        return getPrevValue(skill, min, max);
    }
}
