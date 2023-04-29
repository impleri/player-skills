package net.impleri.playerskills.integration.kubejs.skills;

import net.impleri.playerskills.variant.numeric.NumericSkill;
import net.minecraft.resources.ResourceLocation;

public class NumericSkillJS extends NumericSkill {
    public NumericSkillJS(Builder builder) {
        super(builder.id, builder.initialValue, builder.description, builder.options, builder.changesAllowed, builder.teamMode, builder.notify, builder.notifyKey);
    }

    public static class Builder extends GenericSkillBuilderJS<Double> {
        public Builder(ResourceLocation name) {
            super(name);
        }

        @Override
        public NumericSkillJS createObject() {
            return new NumericSkillJS(this);
        }
    }
}
