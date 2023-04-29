package net.impleri.playerskills.variant.tiered;

import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.TeamMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TieredSkill extends Skill<String> {
    public TieredSkill(ResourceLocation name, List<String> options, @Nullable String value, @Nullable String description, int changesAllowed, TeamMode teamMode, boolean notify, String notifyKey) {
        super(name, TieredSkillType.name, value, description, options, changesAllowed, teamMode, notify, notifyKey);
    }

    @Override
    public Skill<String> copy(String value, int changesAllowed) {
        return new TieredSkill(name, options, value, description, changesAllowed, teamMode, notify, notifyKey);
    }
}
