package net.impleri.playerskills.integration.kubejs.numeric;

import net.impleri.playerskills.integration.kubejs.skills.GenericSkillBuilderJS;
import net.impleri.playerskills.numeric.NumericSkill;
import net.minecraft.resources.ResourceLocation;

public class NumericSkillJS extends NumericSkill {
    public NumericSkillJS(Builder builder) {
        super(builder.id, builder.initialValue, builder.description);
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
