package net.impleri.playerskills.integration.kubejs.specialized;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.impleri.playerskills.integration.kubejs.Registries;
import net.impleri.playerskills.specialized.SpecializedSkillType;

public class SpecializedSkillPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        Registries.SKILLS.addType(SpecializedSkillType.name.toString(), SpecializedSkillJS.Builder.class, SpecializedSkillJS.Builder::new);
    }
}
