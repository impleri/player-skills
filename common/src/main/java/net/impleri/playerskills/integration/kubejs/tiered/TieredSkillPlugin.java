package net.impleri.playerskills.integration.kubejs.tiered;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.impleri.playerskills.integration.kubejs.Registries;
import net.impleri.playerskills.tiered.TieredSkillType;

public class TieredSkillPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        Registries.SKILLS.addType(TieredSkillType.name.toString(), TieredSkillJS.Builder.class, TieredSkillJS.Builder::new);
    }
}
