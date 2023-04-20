package net.impleri.playerskills.variant.numeric;

import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.TeamMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NumericSkill extends Skill<Double> {
    public NumericSkill(ResourceLocation name, @Nullable Double value, @Nullable String description, List<Double> options, int changesAllowed, TeamMode teamMode) {
        super(name, NumericSkillType.name, value, description, options, changesAllowed, teamMode);
    }

    @Override
    public Skill<Double> copy(Double value, int changesAllowed) {
        return new NumericSkill(name, value, description, options, changesAllowed, teamMode);
    }
}
