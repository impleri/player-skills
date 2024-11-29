package net.impleri.playerskills.utils

import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger

trait EventLogging {
  protected def logger: Logger

  protected def logEvent(player: Player, action: String)(can: Boolean): Boolean = {
    if (!can) {
      logger.debug(s"${player.handle} cannot $action")
    } else {
      logger.trace(s"${player.handle} is going to $action")
    }

    can
  }
}
