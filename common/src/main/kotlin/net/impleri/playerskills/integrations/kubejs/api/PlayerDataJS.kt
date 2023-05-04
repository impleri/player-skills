package net.impleri.playerskills.integrations.kubejs.api

import net.impleri.playerskills.client.PlayerClient
import net.minecraft.world.entity.player.Player
import net.impleri.playerskills.api.Player as ServerApi

/**
 * Skills data that gets attached to Player
 */
open class PlayerDataJS(protected val player: Player) {
  @JvmOverloads
  fun <T> can(skillName: String, expectedValue: T? = null): Boolean {
    return when {
      (player.getLevel().isClientSide) -> PlayerClient.can<T>(skillName, expectedValue)
      else                             -> ServerApi.can(player, skillName, expectedValue)
    }
  }

  @JvmOverloads
  fun <T> cannot(skill: String, expectedValue: T? = null): Boolean {
    return !can<T?>(skill, expectedValue)
  }
}
