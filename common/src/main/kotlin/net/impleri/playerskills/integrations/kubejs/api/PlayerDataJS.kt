package net.impleri.playerskills.integrations.kubejs.api

import net.impleri.playerskills.client.api.PlayerClient
import net.minecraft.world.entity.player.Player
import net.impleri.playerskills.api.Player as PlayerServer

/**
 * Skills data that gets attached to Player
 */
open class PlayerDataJS(protected val player: Player) {
  fun <T> can(skillName: String, expectedValue: T?): Boolean {
    return when {
      (player.level.isClientSide) -> PlayerClient.can(skillName, expectedValue)
      else -> PlayerServer.can(player, skillName, expectedValue)
    }
  }

  fun <T> can(skillName: String): Boolean {
    return can(skillName, null)
  }

  fun <T> cannot(skill: String, expectedValue: T?): Boolean {
    return !can(skill, expectedValue)
  }

  fun <T> cannot(skill: String): Boolean {
    return cannot(skill, null)
  }
}
