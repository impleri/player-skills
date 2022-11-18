package net.impleri.playerskills.integration.kubejs.numeric;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.impleri.playerskills.integration.kubejs.Registries;
import net.impleri.playerskills.numeric.NumericSkillType;

public class NumericSkillPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        Registries.SKILLS.addType(NumericSkillType.name.toString(), NumericSkillJS.Builder.class, NumericSkillJS.Builder::new);
    }
}
