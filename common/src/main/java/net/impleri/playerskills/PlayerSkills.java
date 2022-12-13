package net.impleri.playerskills;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.registry.registries.DeferredRegister;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.client.PlayerSkillsClient;
import net.impleri.playerskills.commands.PlayerSkillsCommands;
import net.impleri.playerskills.registry.SkillTypes;
import net.impleri.playerskills.server.PlayerSkillsServer;
import net.impleri.playerskills.utils.PlayerSkillsLogger;
import net.impleri.playerskills.variant.basic.BasicSkillType;
import net.impleri.playerskills.variant.numeric.NumericSkillType;
import net.impleri.playerskills.variant.specialized.SpecializedSkillType;
import net.impleri.playerskills.variant.tiered.TieredSkillType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class PlayerSkills {
    public static final String MOD_ID = "playerskills";

    public static final PlayerSkillsLogger LOGGER = PlayerSkillsLogger.create(MOD_ID, "SKILLS");

    private static final ResourceKey<Registry<SkillType<?>>> SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey(SkillTypes.REGISTRY_KEY);
    private static final DeferredRegister<SkillType<?>> SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY);

    public static void init() {
        PlayerSkillsServer.init();
        PlayerSkillsClient.init();
        registerCommands();
        registerTypes();

        LOGGER.info("PlayerSkills Loaded");
    }

    private static void registerCommands() {
        CommandRegistrationEvent.EVENT.register(PlayerSkillsCommands::register);
    }

    private static void registerTypes() {
        SKILL_TYPES.register(BasicSkillType.name, BasicSkillType::new);
        SKILL_TYPES.register(NumericSkillType.name, NumericSkillType::new);
        SKILL_TYPES.register(TieredSkillType.name, TieredSkillType::new);
        SKILL_TYPES.register(SpecializedSkillType.name, SpecializedSkillType::new);
        SKILL_TYPES.register();
    }
}
