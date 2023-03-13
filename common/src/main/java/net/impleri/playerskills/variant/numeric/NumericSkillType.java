package net.impleri.playerskills.variant.numeric;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class NumericSkillType extends SkillType<Double> {
    public static ResourceLocation name = SkillResourceLocation.of("numeric");

    @Override
    public ResourceLocation getName() {
        return name;
    }

    private String castToString(Double value) {
        return (value == null) ? "" : value.toString();
    }

    @Nullable
    @Override
    public Double castValue(String value) {
        try {
            return (value == null || value.equals("")) ? null : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            PlayerSkills.LOGGER.error("Unable to parse {} into an integer", value);
        }

        return null;
    }

    @Override
    public String serialize(Skill<Double> skill) {
        String stringValue = castToString(skill.getValue());
        List<String> stringOptions = skill.getOptions().stream().map(this::castToString).toList();

        return serialize(skill, stringValue, stringOptions);
    }

    @Override
    @Nullable
    public Skill<Double> unserialize(String skillName, String skillValue, int changesAllowed, List<String> skillOptions) {
        ResourceLocation name = SkillResourceLocation.of(skillName);
        @Nullable Double value = castValue(skillValue);
        @Nullable String description = getDescriptionFor(name);
        List<Double> options = skillOptions.stream()
                .map(this::castValue)
                .filter(Objects::nonNull)
                .toList();

        return new NumericSkill(name, value, description, options, changesAllowed);
    }

    private double getNumericValue(Double value, @Nullable Double fallback) {
        double realFallback = fallback == null ? 0 : fallback;
        return value == null ? realFallback : value;
    }

    private double getNumericValue(Double value) {
        return getNumericValue(value, null);
    }

    private double getNumericValue(Skill<Double> skill) {
        return getNumericValue(skill.getValue(), null);
    }

    @Override
    public boolean can(Skill<Double> skill, @Nullable Double expectedValue) {
        double givenValue = getNumericValue(skill);
        double testValue = getNumericValue(expectedValue);

        PlayerSkills.LOGGER.debug("Checking if player can {} (is {}->{} >= {}<-{})", skill.getName(), skill.getValue(), givenValue, testValue, expectedValue);
        return givenValue >= testValue;
    }

    @Override
    @Nullable
    public Double getPrevValue(Skill<Double> skill, @Nullable Double min, @Nullable Double max) {
        if (!skill.areChangesAllowed()) {
            return null;
        }

        double currentValue = getNumericValue(skill);

        // Ensure we jump down to the max value
        double nextVal = (max == null) ? currentValue : Double.min(currentValue - 1, max);

        // We're stopping at 0, so no fallback needed
        double minVal = getNumericValue(min);

        // Decrement the current value if we're over the min
        return (nextVal >= minVal) ? nextVal : null;
    }

    @Override
    @Nullable
    public Double getNextValue(Skill<Double> skill, @Nullable Double min, @Nullable Double max) {
        if (!skill.areChangesAllowed()) {
            return null;
        }

        double currentValue = getNumericValue(skill);

        // Ensure we jump up to the min value immediately
        double nextVal = (min == null) ? currentValue : Double.max(currentValue + 1, min);

        // If no max, use nextVal so that we increment
        double maxVal = getNumericValue(max, nextVal);

        // Increment the current value if below the max
        return (nextVal <= maxVal) ? nextVal : null;
    }
}
