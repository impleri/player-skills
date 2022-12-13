package net.impleri.playerskills.server;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.integration.kubejs.PlayerSkillsPlugin;
import net.impleri.playerskills.network.NetHandler;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.impleri.playerskills.server.registry.Skills;
import net.impleri.playerskills.server.registry.storage.SkillStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlayerSkillsServer implements ResourceManagerReloadListener {
    private static final PlayerSkillsServer INSTANCE = new PlayerSkillsServer();

    public static void init() {
        PlayerSkills.LOGGER.info("PlayerSkills Server started");
        INSTANCE.registerEvents();
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

    private MinecraftServer serverInstance;

    private void registerEvents() {
        LifecycleEvent.SERVER_BEFORE_START.register(this::beforeServerStart);
        LifecycleEvent.SERVER_STOPPING.register(this::beforeSeverStops);

        PlayerEvent.PLAYER_JOIN.register(this::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(this::onPlayerQuit);

        ReloadListenerRegistry.register(PackType.SERVER_DATA, this);
    }

    private void beforeServerStart(MinecraftServer server) {
        serverInstance = server;

        // Connect Player Skills Registry file storage
        SkillStorage.setup(server);

        // Fill up the deferred skills registry
        Skills.resync();

        // Trigger skills modification event
        PlayerSkillsPlugin.modifySkills();

        SkillChangedEvent.EVENT.register(NetHandler::syncPlayer);
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
        SkillChangedEvent.EVENT.unregister(NetHandler::syncPlayer);

        net.impleri.playerskills.server.registry.PlayerSkills.closeAllPlayers();

        serverInstance = null;
    }
}
