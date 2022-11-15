package net.impleri.playerskills;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.registries.DeferredRegister;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.basic.BasicSkill;
import net.impleri.playerskills.basic.BasicSkillType;
import net.impleri.playerskills.registry.PlayerSkills;
import net.impleri.playerskills.registry.SkillTypes;
import net.impleri.playerskills.registry.Skills;
import net.impleri.playerskills.registry.storage.SkillStorage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PlayerSkillsCore {
    public static final String MOD_ID = "playerskills";

    public static final PlayerSkillsLogger LOGGER = PlayerSkillsLogger.create(MOD_ID, "SKILLS");

    private static final PlayerSkillsCore eventHandler = new PlayerSkillsCore();

    private static final ResourceKey<Registry<Skill<?>>> SKILL_REGISTRY = ResourceKey.createRegistryKey(Skills.REGISTRY_KEY);
    private static final DeferredRegister<Skill<?>> SKILLS = DeferredRegister.create(MOD_ID, SKILL_REGISTRY);

    private static final ResourceKey<Registry<SkillType<?>>> SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey(SkillTypes.REGISTRY_KEY);
    private static final DeferredRegister<SkillType<?>> SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY);

    public static void init() {

        eventHandler.registerEventHandlers();

        LOGGER.info("PlayerSkills Loaded");
        // NB: KubeJS bindings are managed via resources/kubejs.plugin.txt
    }

    public static void end() {
        eventHandler.unregisterEventHandlers();
    }

    private void registerEventHandlers() {
        LifecycleEvent.SERVER_BEFORE_START.register(this::connectRegistryStorage);
        LifecycleEvent.SERVER_STOPPING.register(this::savePlayers);
        CommandRegistrationEvent.EVENT.register(PlayerSkillsCommands::register);

        PlayerEvent.PLAYER_JOIN.register(this::addPlayer);
        PlayerEvent.PLAYER_QUIT.register(this::removePlayer);

        registerCoreTypes();
    }

    private void unregisterEventHandlers() {

        LifecycleEvent.SERVER_BEFORE_START.unregister(this::connectRegistryStorage);
        LifecycleEvent.SERVER_STOPPING.unregister(this::savePlayers);
        CommandRegistrationEvent.EVENT.unregister(PlayerSkillsCommands::register);

        PlayerEvent.PLAYER_JOIN.unregister(this::addPlayer);
        PlayerEvent.PLAYER_QUIT.unregister(this::removePlayer);
    }

    // Modloading lifecycle events

    private void registerCoreTypes() {
        LOGGER.info("Adding core types");

        // All that is needed to register a skill type
        SKILL_TYPES.register(BasicSkillType.name, BasicSkillType::new);
        SKILL_TYPES.register();

        // And to register a skill
        var skillName = SkillResourceLocation.of("test");
        SKILLS.register(skillName, () -> new BasicSkill(SkillResourceLocation.of("test")));
        SKILLS.register();
    }

    // Server lifecycle events

    private void connectRegistryStorage(MinecraftServer server) {
        SkillStorage.setup(server);
    }

    private void savePlayers(MinecraftServer server) {
        PlayerSkills.closeAllPlayers();
    }

    // Events while server running

    private void addPlayer(ServerPlayer player) {
        PlayerSkills.openPlayer(player.getUUID(), Skills.entries());
    }

    private void removePlayer(ServerPlayer player) {
        PlayerSkills.closePlayer(player.getUUID());
    }
}
