package net.impleri.playerskills.tiered;

import net.impleri.playerskills.api.Skill;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TieredSkill extends Skill<String> {
    protected List<String> options;

    public TieredSkill(ResourceLocation name, List<String> options) {
        this(name, options, null);
    }

    public TieredSkill(ResourceLocation name, List<String> options, @Nullable String value) {
        this(name, options, value, null);
    }

    public TieredSkill(ResourceLocation name, List<String> options, @Nullable String value, @Nullable String description) {
        super(name, TieredSkillType.name, value, description, options);
    }

    public TieredSkill(ResourceLocation name, List<String> options, int changesAllowed) {
        this(name, options, null, changesAllowed);
    }

    public TieredSkill(ResourceLocation name, List<String> options, @Nullable String value, int changesAllowed) {
        this(name, options, value, null, changesAllowed);
    }

    public TieredSkill(ResourceLocation name, List<String> options, @Nullable String value, @Nullable String description, int changesAllowed) {
        super(name, TieredSkillType.name, value, description, options, changesAllowed);
    }

    @Override
    public Skill<String> copy() {
        return new TieredSkill(name, options, value, description, changesAllowed);
    }
}
