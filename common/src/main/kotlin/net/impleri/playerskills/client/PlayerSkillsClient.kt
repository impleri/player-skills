package net.impleri.playerskills.client

import com.google.common.collect.ImmutableList
import dev.architectury.registry.ReloadListenerRegistry
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import org.jetbrains.annotations.ApiStatus

class PlayerSkillsClient : ResourceManagerReloadListener {
  private fun registerEvents() {
    ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, this)
  }

  override fun onResourceManagerReload(resourceManager: ResourceManager) {
    resyncSkills()
  }

  companion object {
    private val INSTANCE = PlayerSkillsClient()
    fun init() {
      INSTANCE.registerEvents()
      PlayerSkills.LOGGER.info("PlayerSkills Client started")
    }

    /**
     * Request Server to resend skills
     */
    fun resyncSkills() {
      NetHandler.resyncPlayer()
    }

    /**
     * Handle updated skills from server
     */
    @ApiStatus.Internal
    fun syncFromServer(skills: ImmutableList<Skill<*>>, force: Boolean) {
      Registry.syncFromServer(skills, force)
    }

    /**
     * Broadcast client-side event that skills have changed
     */
    @ApiStatus.Internal
    fun emitSkillsUpdated(skills: ImmutableList<Skill<*>>, prev: ImmutableList<Skill<*>>, force: Boolean) {
      ClientSkillsUpdatedEvent.EVENT.invoker().accept(ClientSkillsUpdatedEvent(skills, prev, force))
    }
  }
}
