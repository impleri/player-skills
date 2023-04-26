package net.impleri.playerskills.variant.tiered;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TieredSkillType extends SkillType<String> {
    public static ResourceLocation name = SkillResourceLocation.of("tiered");

    @Override
    public ResourceLocation getName() {
        return name;
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
        int givenValue = getIndexValue(skill);
        int testValue = getIndexValue(expectedValue, getOptions(skill));

        PlayerSkills.LOGGER.debug("Checking if player can {} (is {}->{} >= {}<-{})", skill.getName(), skill.getValue(), givenValue, testValue, expectedValue);
        return givenValue >= testValue;
    }

    @Override
    @Nullable
    public String getPrevValue(Skill<String> skill, @Nullable String min, @Nullable String max) {
        if (!skill.areChangesAllowed()) {
            return null;
        }

        int currentValue = getIndexValue(skill);

        var options = getOptions(skill);
        int maxIndex = getIndexValue(max, options);
        int minIndex = getIndexValue(min, options);

        int currentMinusOne = currentValue - 1;

        // Ensure we jump down to the max value
        int nextVal = (max == null) ? currentMinusOne : Integer.min(currentMinusOne, maxIndex);

        // We're stopping at 0, so no fallback needed
        int minVal = (min == null) ? 0 : Integer.max(minIndex, 0);

        // Decrement the current value if we're over the min
        return (nextVal >= minVal) ? getIndexName(nextVal, skill) : null;
    }

    @Override
    @Nullable
    public String getNextValue(Skill<String> skill, @Nullable String min, @Nullable String max) {
        if (!skill.areChangesAllowed()) {
            return null;
        }

        int currentValue = getIndexValue(skill);

        var options = getOptions(skill);
        int maxIndex = getIndexValue(max, options);
        int minIndex = getIndexValue(min, options);

        int currentPlusOne = currentValue + 1;
        int rawNextVal = Integer.min(currentPlusOne, options.size());

        // Ensure we jump up to the min value immediately
        int nextVal = (min == null) ? rawNextVal : Integer.max(rawNextVal, minIndex);

        // If no max, use nextVal so that we increment
        int maxVal = (max == null) ? rawNextVal : Integer.min(maxIndex, rawNextVal);

        // Increment the current value if at or below the max
        return (nextVal <= maxVal) ? getIndexName(nextVal, skill) : null;
    }

    private List<String> getOptions(Skill<String> skill) {
        return skill.getOptions();
    }

    private int getActualIndexValue(@Nullable String value, List<String> options) {
        return value == null ? -1 : options.indexOf(value);
    }

    private int getIndexValue(String value, List<String> options, @Nullable String fallback) {
        int fallbackIndex = getActualIndexValue(fallback, options);
        int realFallback = Integer.max(fallbackIndex, 0);

        int indexValue = getActualIndexValue(value, options);
        return indexValue == -1 ? realFallback : indexValue;
    }

    private int getIndexValue(String value, List<String> options) {
        return getIndexValue(value, options, null);
    }

    private int getIndexValue(Skill<String> skill) {
        return getIndexValue(skill.getValue(), getOptions(skill), null);
    }

    private String getIndexName(int index, Skill<String> skill) {
        List<String> options = getOptions(skill);

        return options.get(index);
    }
}
