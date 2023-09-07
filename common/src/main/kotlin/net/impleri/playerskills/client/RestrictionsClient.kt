package net.impleri.playerskills.client

import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.restrictions.Registry
import net.impleri.playerskills.restrictions.RestrictionsApi
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player

abstract class RestrictionsClient<T, R : AbstractRestriction<T>, A : RestrictionsApi<T, R>> @JvmOverloads constructor(
  private val registry: Registry<R>,
  protected val serverApi: A,
  protected val logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  val all: List<R>
    get() = registry.entries()

  protected val player: Player?
    get() {
      try {
        return Minecraft.getInstance().player
      } catch (error: Throwable) {
        logger.warn("Unable to get the clientside player: ${error.message}")
      }
      return null
    }

  protected fun getFiltered(predicate: (R) -> Boolean): List<R> {
    return player?.let { serverApi.getFiltered(it, predicate) } ?: ArrayList()
  }
}
