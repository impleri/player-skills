package net.impleri.playerskills.integration.kubejs;

import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;

import java.util.List;

class PlayerSkillsKubeJSWrapper {
    public List<Skill<?>> getSkills() {
        return net.impleri.playerskills.server.api.Skill.all();
    }

    public List<SkillType<?>> getSkillTypes() {
        return SkillType.all();
    }

    public List<SkillType<?>> getTypes() {
        return getSkillTypes();
    }
}
