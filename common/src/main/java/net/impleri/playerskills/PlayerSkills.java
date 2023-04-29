package net.impleri.playerskills;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.network.Manager;
import net.impleri.playerskills.registry.SkillTypes;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.impleri.playerskills.server.registry.Skills;
import net.impleri.playerskills.utils.PlayerSkillsLogger;
import net.impleri.playerskills.variant.basic.BasicSkillType;
import net.impleri.playerskills.variant.numeric.NumericSkillType;
import net.impleri.playerskills.variant.specialized.SpecializedSkillType;
import net.impleri.playerskills.variant.tiered.TieredSkillType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class PlayerSkills {
    public static final String MOD_ID = "playerskills";

    public static final PlayerSkillsLogger LOGGER = PlayerSkillsLogger.create(MOD_ID, "SKILLS");

    private static final ResourceKey<Registry<SkillType<?>>> SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey(SkillTypes.REGISTRY_KEY);
    private static final DeferredRegister<SkillType<?>> SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY);

    private static final PlayerSkillsEvents INSTANCE = new PlayerSkillsEvents();

    public static void init() {
        registerTypes();

        INSTANCE.registerEvents();
        Manager.register();

        LOGGER.info("PlayerSkills Loaded");

        if (Platform.isModLoaded("ftbquests")) {
            net.impleri.playerskills.integration.ftbquests.PlayerSkillsIntegration.init();
        }
    }

    public static void enableDebug() {
        LOGGER.enableDebug();
    }

    public static boolean toggleDebug() {
        return LOGGER.toggleDebug();
    }

    public static <T> void emitSkillChanged(Player player, Skill<T> newSkill, Skill<T> oldSkill) {
        SkillChangedEvent.EVENT.invoker().accept(new SkillChangedEvent<T>(player, newSkill, oldSkill));

        if (player instanceof ServerPlayer serverPlayer) {
            var message = newSkill.getNotification(oldSkill.getValue());
            if (message != null) {
                serverPlayer.sendSystemMessage(message, true);
            }
        }
    }

    public static <T> void resync(UUID playerId) {
        INSTANCE.resyncPlayer(playerId);
    }

    private static void registerTypes() {
        SkillTypes.buildRegistry();
        Skills.buildRegistry();

        SKILL_TYPES.register(BasicSkillType.name, BasicSkillType::new);
        SKILL_TYPES.register(NumericSkillType.name, NumericSkillType::new);
        SKILL_TYPES.register(TieredSkillType.name, TieredSkillType::new);
        SKILL_TYPES.register(SpecializedSkillType.name, SpecializedSkillType::new);
        SKILL_TYPES.register();
    }
}
