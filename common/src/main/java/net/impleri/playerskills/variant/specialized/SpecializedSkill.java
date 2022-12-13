package net.impleri.playerskills.variant.specialized;

import net.impleri.playerskills.api.Skill;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpecializedSkill extends Skill<String> {
    public SpecializedSkill(ResourceLocation name, List<String> options) {
        this(name, options, null);
    }

    public SpecializedSkill(ResourceLocation name, List<String> options, @Nullable String value) {
        this(name, options, value, null);
    }

    public SpecializedSkill(ResourceLocation name, List<String> options, @Nullable String value, @Nullable String description) {
        super(name, SpecializedSkillType.name, value, description, options);
    }

    public SpecializedSkill(ResourceLocation name, List<String> options, int changesAllowed) {
        this(name, options, null, changesAllowed);
    }

    public SpecializedSkill(ResourceLocation name, List<String> options, @Nullable String value, int changesAllowed) {
        this(name, options, value, null, changesAllowed);
    }

    public SpecializedSkill(ResourceLocation name, List<String> options, @Nullable String value, @Nullable String description, int changesAllowed) {
        super(name, SpecializedSkillType.name, value, description, options, changesAllowed);
    }

    @Override
    public Skill<String> copy() {
        return new SpecializedSkill(name, options, value, description, changesAllowed);
    }
}
