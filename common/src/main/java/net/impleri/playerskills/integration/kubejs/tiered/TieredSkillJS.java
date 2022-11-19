package net.impleri.playerskills.integration.kubejs.tiered;

import net.impleri.playerskills.integration.kubejs.skills.GenericSkillBuilderJS;
import net.impleri.playerskills.tiered.TieredSkill;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TieredSkillJS extends TieredSkill {
    public TieredSkillJS(Builder builder) {
        super(builder.id, builder.options, builder.initialValue, builder.description);
    }

    public static class Builder extends GenericSkillBuilderJS<String> {
        public List<String> options = new ArrayList<>();

        public Builder(ResourceLocation name) {
            super(name);
        }

        public GenericSkillBuilderJS<String> options(String[] options) {
            this.options = Arrays.stream(options).toList();

            return this;
        }

        @Override
        public TieredSkillJS createObject() {
            return new TieredSkillJS(this);
        }
    }
}
