package net.impleri.playerskills.integration.kubejs.basic;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.impleri.playerskills.basic.BasicSkillType;
import net.impleri.playerskills.integration.kubejs.Registries;

public class BasicSkillPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        Registries.SKILLS.addType(BasicSkillType.name.toString(), BasicSkillJS.Builder.class, BasicSkillJS.Builder::new);
    }
}
