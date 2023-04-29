package net.impleri.playerskills.integration.kubejs.skills;

import net.impleri.playerskills.api.TeamMode;
import net.impleri.playerskills.variant.specialized.SpecializedSkill;
import net.minecraft.resources.ResourceLocation;

public class SpecializedSkillJS extends SpecializedSkill {
    public SpecializedSkillJS(Builder builder) {
        super(builder.id, builder.options, builder.initialValue, builder.description, builder.changesAllowed, builder.teamMode, builder.notify, builder.notifyKey);
    }

    public static class Builder extends GenericSkillBuilderJS<String> {

        public Builder(ResourceLocation name) {
            super(name);
        }

        @Override
        public SpecializedSkillJS createObject() {
            return new SpecializedSkillJS(this);
        }

        public Builder splitEvenlyAcrossTeam() {
            teamMode = TeamMode.splitEvenly();

            return this;
        }
    }
}
