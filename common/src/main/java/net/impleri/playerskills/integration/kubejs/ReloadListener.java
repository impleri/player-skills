package net.impleri.playerskills.integration.kubejs;

import net.impleri.playerskills.integration.kubejs.skills.SkillsEventJS;
import net.impleri.playerskills.registry.Skills;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

class ReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        Skills.resync();
        // Add Skills from Java to game Skills registry
        EventsBinding.SKILLS.post(new SkillsEventJS(Registries.SKILLS.types));
    }
}
