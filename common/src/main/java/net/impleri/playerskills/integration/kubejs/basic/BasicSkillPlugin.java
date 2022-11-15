package net.impleri.playerskills.integration.kubejs.basic;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.integration.kubejs.Registries;

public class BasicSkillPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        Registries.SKILLS.addType(SkillResourceLocation.of("basic").toString(), BasicSkillJS.Builder.class, BasicSkillJS.Builder::new);
    }
}
