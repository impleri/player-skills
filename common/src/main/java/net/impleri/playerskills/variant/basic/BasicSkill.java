package net.impleri.playerskills.variant.basic;

import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.TeamMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BasicSkill extends Skill<Boolean> {
    public BasicSkill(ResourceLocation name, @Nullable Boolean value, @Nullable String description, List<Boolean> options, int changesAllowed, TeamMode teamMode) {
        super(name, BasicSkillType.name, value, description, options, changesAllowed, teamMode);
    }

    @Override
    public Skill<Boolean> copy(Boolean value, int changesAllowed) {
        return new BasicSkill(name, value, description, options, changesAllowed, teamMode);
    }
}
