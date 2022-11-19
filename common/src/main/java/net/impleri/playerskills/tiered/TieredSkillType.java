package net.impleri.playerskills.tiered;

import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TieredSkillType extends SkillType<String> {
    public static ResourceLocation name = SkillResourceLocation.of("tiered");
    private static final String optionsSeparator = "!";
    private static final String stringValueNull = "NULL";

    @Override
    public ResourceLocation getName() {
        return name;
    }

    private List<String> getOptions(Skill<String> skill) {
        return ((TieredSkill) skill).getOptions();
    }

    @Override
    public String serialize(Skill<String> skill) {
        String stringValue = (skill.getValue() == null) ? stringValueNull : skill.getValue();

        String elements = Stream.concat(
                Stream.of(stringValue),
                getOptions(skill).stream()
        ).collect(Collectors
                .joining(optionsSeparator));

        return serialize(skill, elements);
    }

    @Override
    @Nullable
    public Skill<String> unserialize(String skillName, String skillValue) {
        String[] elements = skillValue.split(optionsSeparator);
        if (elements.length < 2) {
            PlayerSkillsCore.LOGGER.warn("Unserializing tiered skill {} without any options", skillName);
        }
        String value = elements[0].equals(stringValueNull) ? null : elements[0];
        List<String> options = Arrays.stream(elements)
                .skip(1)
                .toList();

        return new TieredSkill(SkillResourceLocation.of(skillName), options, value);
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

    @Override
    public boolean can(Skill<String> skill, @Nullable String expectedValue) {
        int givenValue = getIndexValue(skill);
        int testValue = getIndexValue(expectedValue, getOptions(skill));

        PlayerSkillsCore.LOGGER.debug("Checking if player can {} (is {}->{} >= {}<-{})", skill.getName(), skill.getValue(), givenValue, testValue, expectedValue);
        return givenValue >= testValue;
    }

    @Override
    @Nullable
    public String getPrevValue(Skill<String> skill, @Nullable String min, @Nullable String max) {
        int currentValue = getIndexValue(skill);
        int maxIndex = getIndexValue(max, getOptions(skill));
        int minIndex = getIndexValue(min, getOptions(skill));

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
        int currentValue = getIndexValue(skill);
        int maxIndex = getIndexValue(max, getOptions(skill));
        int minIndex = getIndexValue(min, getOptions(skill));

        int currentPlusOne = currentValue + 1;
        int maybeEndOfList = Integer.min(currentPlusOne, getOptions(skill).size());

        // Ensure we jump up to the min value immediately
        int nextVal = (min == null) ? maybeEndOfList : Integer.max(maybeEndOfList, minIndex);

        // If no max, use nextVal so that we increment
        int maxVal = (max == null) ? maybeEndOfList : Integer.min(maxIndex, maybeEndOfList);

        // Increment the current value if below the max
        return (nextVal <= maxVal) ? getIndexName(nextVal, skill) : null;
    }
}
