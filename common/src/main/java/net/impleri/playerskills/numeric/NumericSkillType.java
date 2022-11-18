package net.impleri.playerskills.numeric;

import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class NumericSkillType extends SkillType<Double> {
    public static ResourceLocation name = SkillResourceLocation.of("numeric");

    @Override
    public ResourceLocation getName() {
        return name;
    }

    @Override
    public String serialize(Skill<Double> skill) {
        String stringValue = (skill.getValue() == null) ? "" : skill.getValue().toString();

        return serialize(skill, stringValue);
    }

    @Override
    @Nullable
    public Skill<Double> unserialize(String skillName, String skillValue) {
        try {
            @Nullable Double castValue = skillValue.equals("") ? null : Double.parseDouble(skillValue);
            return new NumericSkill(SkillResourceLocation.of(skillName), castValue);
        } catch (NumberFormatException e) {
            PlayerSkillsCore.LOGGER.error("Unable to parse {} into an integer for {}", skillValue, skillName);
        }

        return null;
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

        PlayerSkillsCore.LOGGER.debug("Checking if player can {} (is {}->{} >= {}<-{})", skill.getName(), skill.getValue(), givenValue, testValue, expectedValue);
        return givenValue >= testValue;
    }

    @Override
    @Nullable
    public Double getPrevValue(Skill<Double> skill, @Nullable Double min, @Nullable Double max) {
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
        double currentValue = getNumericValue(skill);

        // Ensure we jump up to the min value immediately
        double nextVal = (min == null) ? currentValue : Double.max(currentValue + 1, min);

        // If no max, use nextVal so that we increment
        double maxVal = getNumericValue(max, nextVal);

        // Increment the current value if below the max
        return (nextVal <= maxVal) ? nextVal : null;
    }
}
