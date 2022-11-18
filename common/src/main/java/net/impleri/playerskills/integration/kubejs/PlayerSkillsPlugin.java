package net.impleri.playerskills.integration.kubejs;

import dev.architectury.registry.ReloadListenerRegistry;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.util.AttachedData;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.integration.kubejs.events.SkillChangedEventJS;
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

    public static <T> void onSkillChange(Player player, Skill<T> next, Skill<T> prev) {
        EventsBinding.SKILL_CHANGED.post(next.getName(), new SkillChangedEventJS<T>(player, next, prev));
    }

    @Override
    public void attachPlayerData(AttachedData<Player> event) {
        event.add("skills", new PlayerDataJS(event.getParent()));
    }
}
