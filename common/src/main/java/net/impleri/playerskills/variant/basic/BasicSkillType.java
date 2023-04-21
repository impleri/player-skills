package net.impleri.playerskills.variant.basic;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BasicSkillType extends SkillType<Boolean> {
    public static ResourceLocation name = SkillResourceLocation.of("basic");

    private static final String stringValTrue = "true";
    private static final String stringValFalse = "false";

    @Override
    public ResourceLocation getName() {
        return name;
    }

    @Override
    protected String castToString(Boolean value) {
        return (value == null) ? "" : (value) ? stringValTrue : stringValFalse;
    }

    @Nullable
    @Override
    public Boolean castFromString(String value) {
        return (value == null || value.equals("")) ? null : Objects.equals(value, stringValTrue);
    }

    @Override
    public boolean can(Skill<Boolean> skill, @Nullable Boolean expectedValue) {
        boolean givenValue = BooleanUtils.toBoolean(skill.getValue());
        boolean testValue = expectedValue == null || BooleanUtils.toBoolean(expectedValue);

        PlayerSkills.LOGGER.debug("Checking if player can {} (does {}->{} == {}<-{})", skill.getName(), expectedValue, testValue, givenValue, skill.getValue());
        return testValue == givenValue;
    }

    @Override
    @Nullable
    public Boolean getPrevValue(Skill<Boolean> skill, @Nullable Boolean min, @Nullable Boolean max) {
        if (!skill.areChangesAllowed()) {
            return null;
        }

        return BooleanUtils.toBoolean(skill.getValue()) ? false : null;
    }

    @Override
    @Nullable
    public Boolean getNextValue(Skill<Boolean> skill, @Nullable Boolean min, @Nullable Boolean max) {
        if (!skill.areChangesAllowed()) {
            return null;
        }

        return BooleanUtils.toBoolean(skill.getValue()) ? null : true;
    }
}
