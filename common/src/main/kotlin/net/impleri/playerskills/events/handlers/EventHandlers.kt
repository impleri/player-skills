package net.impleri.playerskills.events.handlers

import net.minecraft.server.MinecraftServer
import java.util.UUID

internal object EventHandlers {
  private val BLOCK = BlockEvents()
  private val COMMAND = CommandEvents()
  private val ENTITY = EntityEvents()
  private val INTERACTION = InteractionEvents()
  private val INTERNAL = InternalEvents()
  private val LIFECYCLE = LifecycleEvents()
  private val PLAYER = PlayerEvents()
  private val TICK = TickEvents()

  internal val server: Lazy<MinecraftServer> = lazy { LIFECYCLE.server }

  fun init() {
    LIFECYCLE.registerEvents()
    COMMAND.registerEvents()
    INTERNAL.registerEvents()
    TICK.registerEventHandlers()
    BLOCK.registerEventHandlers()
    ENTITY.registerEventHandlers()
    INTERACTION.registerEventHandlers()
    PLAYER.registerEvents()
  }

  fun resync(playerId: UUID) {
    INTERNAL.resyncPlayer(server, playerId)
  }
}
