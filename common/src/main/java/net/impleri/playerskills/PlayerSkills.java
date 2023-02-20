package net.impleri.playerskills;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.commands.PlayerSkillsCommands;
import net.impleri.playerskills.integration.kubejs.PlayerSkillsPlugin;
import net.impleri.playerskills.network.Manager;
import net.impleri.playerskills.registry.SkillTypes;
import net.impleri.playerskills.server.NetHandler;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.impleri.playerskills.server.registry.Skills;
import net.impleri.playerskills.server.registry.storage.SkillStorage;
import net.impleri.playerskills.utils.PlayerSkillsLogger;
import net.impleri.playerskills.variant.basic.BasicSkillType;
import net.impleri.playerskills.variant.numeric.NumericSkillType;
import net.impleri.playerskills.variant.specialized.SpecializedSkillType;
import net.impleri.playerskills.variant.tiered.TieredSkillType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class PlayerSkills implements ResourceManagerReloadListener {
    public static final String MOD_ID = "playerskills";

    public static final PlayerSkillsLogger LOGGER = PlayerSkillsLogger.create(MOD_ID, "SKILLS");

    private static final ResourceKey<Registry<SkillType<?>>> SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey(SkillTypes.REGISTRY_KEY);
    private static final DeferredRegister<SkillType<?>> SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY);

    private static final PlayerSkills INSTANCE = new PlayerSkills();

    public static void init() {
        registerTypes();
        registerCommands();

        INSTANCE.registerEvents();
        Manager.register();

        LOGGER.info("PlayerSkills Loaded");
    }

    public static void enableDebug() {
        LOGGER.enableDebug();
    }

    public static <T> void emitSkillChanged(Player player, Skill<T> newSkill, Skill<T> oldSkill) {
        SkillChangedEvent.EVENT.invoker().accept(new SkillChangedEvent<T>(player, newSkill, oldSkill));
    }

    public static <T> void resync(UUID playerId) {
        var player = INSTANCE.serverInstance.getPlayerList().getPlayer(playerId);
        if (player != null) {
            NetHandler.syncPlayer(player);
        }
    }

    private static void registerCommands() {
        CommandRegistrationEvent.EVENT.register(PlayerSkillsCommands::register);
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

    private MinecraftServer serverInstance;

    private void registerEvents() {
        LifecycleEvent.SERVER_BEFORE_START.register(this::beforeServerStart);
        LifecycleEvent.SERVER_STOPPING.register(this::beforeSeverStops);

        PlayerEvent.PLAYER_JOIN.register(this::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(this::onPlayerQuit);

        ReloadListenerRegistry.register(PackType.SERVER_DATA, this);

        SkillChangedEvent.EVENT.register(NetHandler::syncPlayer);
    }

    private void beforeServerStart(MinecraftServer server) {
        serverInstance = server;

        // Connect Player Skills Registry file storage
        SkillStorage.setup(server);

        // Fill up the deferred skills registry
        Skills.resync();

        // Trigger skills modification event
        PlayerSkillsPlugin.modifySkills();
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        List<UUID> players = net.impleri.playerskills.server.registry.PlayerSkills.closeAllPlayers();

        net.impleri.playerskills.server.registry.PlayerSkills.openPlayers(players);

        if (serverInstance != null) {
            serverInstance.getPlayerList().getPlayers().forEach(NetHandler::syncPlayer);
        }
    }

    private void onPlayerJoin(ServerPlayer player) {
        net.impleri.playerskills.server.registry.PlayerSkills.openPlayer(player.getUUID());
        NetHandler.syncPlayer(player);
    }

    private void onPlayerQuit(ServerPlayer player) {
        net.impleri.playerskills.server.registry.PlayerSkills.closePlayer(player.getUUID());
    }


    private void beforeSeverStops(MinecraftServer server) {
        net.impleri.playerskills.server.registry.PlayerSkills.closeAllPlayers();

        serverInstance = null;
    }
}
