package net.impleri.playerskills.basic;

import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

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
        @Nullable boolean castValue = skillValue == stringValTrue || ((skillValue == stringValFalse) ? false : null);

        return new BasicSkill(SkillResourceLocation.of(skillName), castValue);
    }
}
