package net.impleri.playerskills.variant.specialized;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.impleri.playerskills.variant.tiered.TieredSkill;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpecializedSkillType extends SkillType<String> {
    public static ResourceLocation name = SkillResourceLocation.of("specialized");
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
        return serialize(skill, skill.getValue(), skill.getOptions());
    }

    @Override
    @Nullable
    public Skill<String> unserialize(String skillName, String skillValue, int changesAllowed, List<String> options) {
        ResourceLocation name = SkillResourceLocation.of(skillName);
        @Nullable String description = getDescriptionFor(name);

        return new SpecializedSkill(name, options, skillValue, description, changesAllowed);
    }

    @Override
    public @Nullable String castValue(String value) {
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
