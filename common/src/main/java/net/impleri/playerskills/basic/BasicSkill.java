package net.impleri.playerskills.basic;

import net.impleri.playerskills.api.Skill;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BasicSkill extends Skill<Boolean> {
    public BasicSkill(ResourceLocation name) {
        this(name, null);
    }

    public BasicSkill(ResourceLocation name, @Nullable Boolean value) {
        this(name, value, null);
    }

    public BasicSkill(ResourceLocation name, @Nullable Boolean value, @Nullable String description) {
        super(name, BasicSkillType.name, value, description);
    }

    @Override
    public Skill<Boolean> copy() {
        return new BasicSkill(name, value, description);
    }
}
