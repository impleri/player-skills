package net.impleri.playerskills;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.impleri.playerskills.commands.PlayerSkillsCommands;
import net.impleri.playerskills.server.NetHandler;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.impleri.playerskills.server.registry.PlayerSkills;
import net.impleri.playerskills.server.registry.Skills;
import net.impleri.playerskills.server.registry.storage.SkillStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class PlayerSkillsEvents implements ResourceManagerReloadListener {

    private MinecraftServer serverInstance;

    public void registerEvents() {
        LifecycleEvent.SERVER_BEFORE_START.register(this::beforeServerStart);
        LifecycleEvent.SERVER_STOPPING.register(this::beforeSeverStops);

        PlayerEvent.PLAYER_JOIN.register(this::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(this::onPlayerQuit);

        ReloadListenerRegistry.register(PackType.SERVER_DATA, this);

        SkillChangedEvent.EVENT.register(NetHandler::syncPlayer);

        CommandRegistrationEvent.EVENT.register(PlayerSkillsCommands::register);
    }

    public <T> void resyncPlayer(UUID playerId) {
        var player = serverInstance.getPlayerList().getPlayer(playerId);
        if (player != null) {
            NetHandler.syncPlayer(player);
        }
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        List<UUID> players = PlayerSkills.closeAllPlayers();

        PlayerSkills.openPlayers(players);

        if (serverInstance != null) {
            serverInstance.getPlayerList().getPlayers().forEach(NetHandler::syncPlayer);
        }
    }

    private void beforeServerStart(MinecraftServer server) {
        serverInstance = server;

        // Connect Player Skills Registry file storage
        SkillStorage.setup(server);

        // Fill up the deferred skills registry
        Skills.resync();
    }

    private void onPlayerJoin(ServerPlayer player) {
        PlayerSkills.openPlayer(player.getUUID());
        NetHandler.syncPlayer(player, true);
    }

    private void onPlayerQuit(ServerPlayer player) {
        PlayerSkills.closePlayer(player.getUUID());
    }

    private void beforeSeverStops(MinecraftServer server) {
        net.impleri.playerskills.server.registry.PlayerSkills.closeAllPlayers();

        serverInstance = null;
    }
}
