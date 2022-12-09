package net.impleri.playerskills;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.basic.BasicSkillType;
import net.impleri.playerskills.events.SkillChangedEvent;
import net.impleri.playerskills.integration.kubejs.PlayerSkillsPlugin;
import net.impleri.playerskills.network.NetHandler;
import net.impleri.playerskills.numeric.NumericSkillType;
import net.impleri.playerskills.registry.PlayerSkills;
import net.impleri.playerskills.registry.SkillTypes;
import net.impleri.playerskills.registry.Skills;
import net.impleri.playerskills.registry.storage.SkillStorage;
import net.impleri.playerskills.specialized.SpecializedSkillType;
import net.impleri.playerskills.tiered.TieredSkillType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlayerSkillsCore implements ResourceManagerReloadListener {
    public static final String MOD_ID = "playerskills";

    public static final PlayerSkillsLogger LOGGER = PlayerSkillsLogger.create(MOD_ID, "SKILLS");

    private static final PlayerSkillsCore eventHandler = new PlayerSkillsCore();

    private static final ResourceKey<Registry<SkillType<?>>> SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey(SkillTypes.REGISTRY_KEY);
    private static final DeferredRegister<SkillType<?>> SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY);

    private static final ResourceKey<Registry<SkillType<?>>> SKILL_REGISTRY = ResourceKey.createRegistryKey(Skills.REGISTRY_KEY);

    public static void init() {
        eventHandler.registerServerEvents();
        eventHandler.registerCommonEvents();

        LOGGER.info("PlayerSkills Loaded");
        // NB: KubeJS bindings are managed via resources/kubejs.plugin.txt
    }

    private MinecraftServer serverInstance;

    private void registerCommonEvents() {
        CommandRegistrationEvent.EVENT.register(PlayerSkillsCommands::register);

        SKILL_TYPES.register(BasicSkillType.name, BasicSkillType::new);
        SKILL_TYPES.register(NumericSkillType.name, NumericSkillType::new);
        SKILL_TYPES.register(TieredSkillType.name, TieredSkillType::new);
        SKILL_TYPES.register(SpecializedSkillType.name, SpecializedSkillType::new);
        SKILL_TYPES.register();
    }

    private void registerServerEvents() {
        LifecycleEvent.SERVER_BEFORE_START.register(this::beforeServerStart);
        LifecycleEvent.SERVER_STOPPING.register(this::beforeSeverStops);

        PlayerEvent.PLAYER_JOIN.register(this::addPlayer);
        PlayerEvent.PLAYER_QUIT.register(this::removePlayer);

        SkillChangedEvent.EVENT.register(NetHandler::syncPlayer);

        ReloadListenerRegistry.register(PackType.SERVER_DATA, this);
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        List<UUID> players = PlayerSkills.closeAllPlayers();

        Skills.resync();
        PlayerSkillsPlugin.registerSkills();

        PlayerSkills.openPlayers(players);

        if (serverInstance != null) {
            serverInstance.getPlayerList().getPlayers().forEach(NetHandler::syncPlayer);
        }
    }

    // Server lifecycle events

    private void beforeServerStart(MinecraftServer server) {
        serverInstance = server;
        connectRegistryStorage(server);
    }

    private void beforeSeverStops(MinecraftServer server) {
        serverInstance = null;
        savePlayers();
    }

    private void connectRegistryStorage(MinecraftServer server) {
        SkillStorage.setup(server);
    }

    private void savePlayers() {
        PlayerSkills.closeAllPlayers();
    }

    // Events while server running

    private void addPlayer(ServerPlayer player) {
        PlayerSkills.openPlayer(player.getUUID());
        NetHandler.syncPlayer(player);
    }

    private void removePlayer(ServerPlayer player) {
        PlayerSkills.closePlayer(player.getUUID());
    }

}
