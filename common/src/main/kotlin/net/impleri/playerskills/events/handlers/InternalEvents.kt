package net.impleri.playerskills.events.handlers

import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.registry.ReloadListenerRegistry
import net.impleri.playerskills.api.BlockRestrictions
import net.impleri.playerskills.data.ItemRestrictionDataLoader
import net.impleri.playerskills.data.MobRestrictionDataLoader
import net.impleri.playerskills.data.SkillsDataLoader
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.skills.registry.Players
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import java.util.UUID

internal class InternalEvents : ResourceManagerReloadListener {
  fun registerEvents() {
    // Player Skills Events
    SkillChangedEvent.EVENT.register { onSkillChanged(it) }

    PlayerEvent.PLAYER_JOIN.register { onJoin(it) }
    PlayerEvent.PLAYER_QUIT.register { onQuit(it) }

    // Vanilla Events
    ReloadListenerRegistry.register(PackType.SERVER_DATA, this)
    ReloadListenerRegistry.register(PackType.SERVER_DATA, SkillsDataLoader())
    ReloadListenerRegistry.register(PackType.SERVER_DATA, ItemRestrictionDataLoader())
    ReloadListenerRegistry.register(PackType.SERVER_DATA, MobRestrictionDataLoader())
  }

  override fun onResourceManagerReload(resourceManager: ResourceManager) {
    val players = Players.close()
    Players.open(players)

    if (EventHandlers.server.isInitialized()) {
      EventHandlers.server.value.playerList
        .players
        .forEach { player -> NetHandler.syncPlayer(player) }
    }
  }

  fun resyncPlayer(server: Lazy<MinecraftServer>, playerId: UUID) {
    if (server.isInitialized()) {
      server.value.playerList.getPlayer(playerId)?.let { NetHandler.syncPlayer(it) }
    }
  }

  private val playerMap = HashMap<ServerPlayer, Long>()

  private fun onJoin(player: ServerPlayer) {
    playerMap[player] = BlockRestrictions.getReplacementsCountFor(player)
  }

  private fun onQuit(player: ServerPlayer) {
    playerMap.remove(player)
  }

  private fun onSkillChanged(event: SkillChangedEvent<*>) {
    NetHandler.syncPlayer(event)

    // get the ServerPlayer we should have from the initial filling of the cache
    val eventPlayerId = event.player.uuid

    playerMap.keys.firstOrNull { eventPlayerId.equals(it.uuid) }
      ?.let {
        val originalCount = playerMap[it]
        val newCount = BlockRestrictions.getReplacementsCountFor(it)

        // We're assuming that the number of replaced blocks should change if a skill change actually changes replacements
        // If we run into an issue where a skills change should trigger a refresh but the count difference doesn't change,
        // we'll have to rework this
        if (originalCount == newCount) {
          return
        }

        playerMap[it] = newCount
        it.getLevel().chunkSource.chunkMap.updatePlayerStatus(it, true)
      }
  }
}
