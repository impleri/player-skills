package net.impleri.playerskills.integration.kubejs;

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
import net.minecraft.world.entity.player.Player;

public class PlayerSkillsPlugin extends KubeJSPlugin {
    public static void modifySkills() {
        EventsBinding.MODIFICATION.post(new SkillsModificationEventJS(Registries.SKILLS.types));
    }

    private final PlayerSkillsKubeJSWrapper skillWrapper = new PlayerSkillsKubeJSWrapper();

    @Override
    public void init() {
        SkillChangedEvent.EVENT.register(this::onSkillChange);
        Registries.SKILLS.addType(BasicSkillType.name.toString(), BasicSkillJS.Builder.class, BasicSkillJS.Builder::new);
        Registries.SKILLS.addType(NumericSkillType.name.toString(), NumericSkillJS.Builder.class, NumericSkillJS.Builder::new);
        Registries.SKILLS.addType(TieredSkillType.name.toString(), TieredSkillJS.Builder.class, TieredSkillJS.Builder::new);
        Registries.SKILLS.addType(SpecializedSkillType.name.toString(), SpecializedSkillJS.Builder.class, SpecializedSkillJS.Builder::new);
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
        event.add("skills", new PlayerDataJS(event.getParent()));
    }

    private void registerSkills() {
        EventsBinding.REGISTRATION.post(new SkillsRegistrationEventJS(Registries.SKILLS.types));
    }

    private <T> void onSkillChange(SkillChangedEvent<T> event) {
        EventsBinding.SKILL_CHANGED.post(event.getSkill().getName(), new PlayerSkillChangedEventJS<T>(event));
    }
}
