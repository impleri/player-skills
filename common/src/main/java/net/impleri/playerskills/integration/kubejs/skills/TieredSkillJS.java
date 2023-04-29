package net.impleri.playerskills.integration.kubejs.skills;

import net.impleri.playerskills.api.TeamMode;
import net.impleri.playerskills.variant.tiered.TieredSkill;
import net.minecraft.resources.ResourceLocation;

public class TieredSkillJS extends TieredSkill {
    public TieredSkillJS(Builder builder) {
        super(builder.id, builder.options, builder.initialValue, builder.description, builder.changesAllowed, builder.teamMode, builder.notify, builder.notifyKey);
    }

    public static class Builder extends GenericSkillBuilderJS<String> {

        public Builder(ResourceLocation name) {
            super(name);
        }

        @Override
        public TieredSkillJS createObject() {
            return new TieredSkillJS(this);
        }

        public Builder pyramid() {
            teamMode = TeamMode.pyramid();

            return this;
        }
    }
}
