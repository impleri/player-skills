package net.impleri.playerskills.basic;

import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class BasicSkillType extends SkillType<Boolean> {
    public static ResourceLocation name = SkillResourceLocation.of("basic");

    private static final String stringValTrue = "true";
    private static final String stringValFalse = "false";

    @Override
    public ResourceLocation getName() {
        return name;
    }

    private String castToString(Boolean value) {
        return (value == null) ? "" : (value) ? stringValTrue : stringValFalse;
    }

    @Nullable
    private Boolean castToBool(String value) {
        return (value == null || value.equals("")) ? null : Objects.equals(value, stringValTrue);
    }

    @Override
    public String serialize(Skill<Boolean> skill) {
        String stringValue = castToString(skill.getValue());
        List<String> stringOptions = skill.getOptions().stream().map(this::castToString).toList();

        return serialize(skill, stringValue, stringOptions);
    }

    @Override
    public Skill<Boolean> unserialize(String skillName, String skillValue, int changesAllowed, List<String> skillOptions) {
        @Nullable Boolean castValue = castToBool(skillValue);
        ResourceLocation name = SkillResourceLocation.of(skillName);
        @Nullable String description = getDescriptionFor(name);
        List<Boolean> options = skillOptions.stream()
                .map(this::castToBool)
                .filter(Objects::nonNull)
                .toList();

        return new BasicSkill(SkillResourceLocation.of(skillName), castValue, description, options, changesAllowed);
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
