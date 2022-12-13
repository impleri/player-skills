package net.impleri.playerskills.variant.basic;

import net.impleri.playerskills.api.Skill;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasicSkill extends Skill<Boolean> {
    public BasicSkill(ResourceLocation name) {
        this(name, null);
    }

    public BasicSkill(ResourceLocation name, @Nullable Boolean value) {
        this(name, value, null);
    }

    public BasicSkill(ResourceLocation name, @Nullable Boolean value, @Nullable String description) {
        this(name, value, description, new ArrayList<>());
    }

    public BasicSkill(ResourceLocation name, @Nullable Boolean value, @Nullable String description, List<Boolean> options) {
        super(name, BasicSkillType.name, value, description, options);
    }

    public BasicSkill(ResourceLocation name, @Nullable Boolean value, int changesAllowed) {
        this(name, value, null, changesAllowed);
    }

    public BasicSkill(ResourceLocation name, @Nullable Boolean value, @Nullable String description, int changesAllowed) {
        this(name, value, description, new ArrayList<>(), changesAllowed);
    }

    public BasicSkill(ResourceLocation name, @Nullable Boolean value, @Nullable String description, List<Boolean> options, int changesAllowed) {
        super(name, BasicSkillType.name, value, description, options, changesAllowed);
    }

    @Override
    public Skill<Boolean> copy() {
        return new BasicSkill(name, value, description, changesAllowed);
    }
}
