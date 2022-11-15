package net.impleri.playerskills.integration.kubejs;

import dev.architectury.registry.ReloadListenerRegistry;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.util.AttachedData;
import net.impleri.playerskills.integration.kubejs.skills.PlayerDataJS;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;

public class PlayerSkillsPlugin extends KubeJSPlugin {
    private final ReloadListener reloadListener = new ReloadListener();
    private final PlayerSkillsKubeJSWrapper skillWrapper = new PlayerSkillsKubeJSWrapper();

    @Override
    public void init() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, reloadListener);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("PlayerSkills", skillWrapper);
    }

    @Override
    public void registerEvents() {
        EventsBinding.GROUP.register();
    }

    @Override
    public void attachPlayerData(AttachedData<Player> event) {
        event.add("skills", new PlayerDataJS(event.getParent()));
    }
}
