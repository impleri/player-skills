package net.impleri.playerskills.basic;

import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
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
    public String serialize(Skill<Boolean> skill) {
        String stringValue = (skill.getValue() == null) ? "" : (skill.getValue()) ? stringValTrue : stringValFalse;

        return serialize(skill, stringValue);
    }

    @Override
    public Skill<Boolean> unserialize(String skillName, String skillValue) {
        @Nullable Boolean castValue = Objects.equals(skillValue, stringValTrue) || ((Objects.equals(skillValue, stringValFalse)) ? false : null);

        return new BasicSkill(SkillResourceLocation.of(skillName), castValue);
    }

    @Override
    public boolean can(Skill<Boolean> skill, @Nullable Boolean expectedValue) {
        boolean givenValue = BooleanUtils.toBoolean(skill.getValue());
        boolean testValue = expectedValue == null || BooleanUtils.toBoolean(expectedValue);

        PlayerSkillsCore.LOGGER.debug("Checking if player can {} (does {}->{} == {}<-{})", skill.getName(), expectedValue, testValue, givenValue, skill.getValue());
        return testValue == givenValue;
    }

    @Override
    @Nullable
    public Boolean getPrevValue(Skill<Boolean> skill, @Nullable Boolean min, @Nullable Boolean max) {
        return BooleanUtils.toBoolean(skill.getValue()) ? false : null;
    }

    @Override
    @Nullable
    public Boolean getNextValue(Skill<Boolean> skill, @Nullable Boolean min, @Nullable Boolean max) {
        return BooleanUtils.toBoolean(skill.getValue()) ? null : true;
    }
}
