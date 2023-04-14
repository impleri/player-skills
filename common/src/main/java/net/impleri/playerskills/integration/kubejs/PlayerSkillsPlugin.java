package net.impleri.playerskills.integration.kubejs;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.util.AttachedData;
import net.impleri.playerskills.integration.kubejs.events.EventsBinding;
import net.impleri.playerskills.integration.kubejs.events.PlayerSkillChangedEventJS;
import net.impleri.playerskills.integration.kubejs.events.SkillsModificationEventJS;
import net.impleri.playerskills.integration.kubejs.events.SkillsRegistrationEventJS;
import net.impleri.playerskills.integration.kubejs.skills.BasicSkillJS;
import net.impleri.playerskills.integration.kubejs.skills.NumericSkillJS;
import net.impleri.playerskills.integration.kubejs.skills.SpecializedSkillJS;
import net.impleri.playerskills.integration.kubejs.skills.TieredSkillJS;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.impleri.playerskills.variant.basic.BasicSkillType;
import net.impleri.playerskills.variant.numeric.NumericSkillType;
import net.impleri.playerskills.variant.specialized.SpecializedSkillType;
import net.impleri.playerskills.variant.tiered.TieredSkillType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public class PlayerSkillsPlugin extends KubeJSPlugin {
    private final PlayerSkillsKubeJSWrapper skillWrapper = new PlayerSkillsKubeJSWrapper();

    @Override
    public void init() {
        LifecycleEvent.SERVER_STARTING.register(this::onServerStart);
        SkillChangedEvent.EVENT.register(this::onSkillChange);
        Registries.SKILLS.addType(BasicSkillType.name.toString(), BasicSkillJS.Builder.class, BasicSkillJS.Builder::new);
        Registries.SKILLS.addType(NumericSkillType.name.toString(), NumericSkillJS.Builder.class, NumericSkillJS.Builder::new);
        Registries.SKILLS.addType(TieredSkillType.name.toString(), TieredSkillJS.Builder.class, TieredSkillJS.Builder::new);
        Registries.SKILLS.addType(SpecializedSkillType.name.toString(), SpecializedSkillJS.Builder.class, SpecializedSkillJS.Builder::new);
    }

    private void onServerStart(MinecraftServer minecraftServer) {
        // Trigger skills modification event
        EventsBinding.MODIFICATION.post(new SkillsModificationEventJS(Registries.SKILLS.types));
    }

    @Override
    public void registerEvents() {
        EventsBinding.GROUP.register();
    }

    @Override
    public void initStartup() {
        registerSkills();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("PlayerSkills", skillWrapper);
    }

    @Override
    public void attachPlayerData(AttachedData<Player> event) {
        event.add("skills", new MutablePlayerDataJS(event.getParent()));
    }

    private void registerSkills() {
        EventsBinding.REGISTRATION.post(new SkillsRegistrationEventJS(Registries.SKILLS.types));
    }

    private <T> void onSkillChange(SkillChangedEvent<T> event) {
        EventsBinding.SKILL_CHANGED.post(event.getSkill().getName(), new PlayerSkillChangedEventJS<T>(event));
    }
}
