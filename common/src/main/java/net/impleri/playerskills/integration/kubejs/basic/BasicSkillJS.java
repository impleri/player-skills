package net.impleri.playerskills.integration.kubejs.basic;

import net.impleri.playerskills.basic.BasicSkill;
import net.impleri.playerskills.integration.kubejs.skills.GenericSkillBuilderJS;
import net.minecraft.resources.ResourceLocation;

public class BasicSkillJS extends BasicSkill {
    public BasicSkillJS(Builder builder) {
        super(builder.id, builder.initialValue, builder.description, builder.options, builder.changesAllowed);
    }

    public static class Builder extends GenericSkillBuilderJS<Boolean> {
        public Builder(ResourceLocation name) {
            super(name);
        }

        @Override
        public BasicSkillJS createObject() {
            return new BasicSkillJS(this);
        }
    }
}
