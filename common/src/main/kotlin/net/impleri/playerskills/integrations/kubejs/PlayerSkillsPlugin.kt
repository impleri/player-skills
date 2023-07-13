package net.impleri.playerskills.integrations.kubejs

import dev.architectury.event.events.common.LifecycleEvent
import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.script.BindingsEvent
import dev.latvian.mods.kubejs.util.AttachedData
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.integrations.kubejs.mobs.MobEventsBinding
import net.impleri.playerskills.integrations.kubejs.mobs.MobRestrictionsEventJS
import net.impleri.playerskills.integrations.kubejs.skills.BasicSkillJS
import net.impleri.playerskills.integrations.kubejs.skills.MutablePlayerDataJS
import net.impleri.playerskills.integrations.kubejs.skills.NumericSkillJS
import net.impleri.playerskills.integrations.kubejs.skills.PlayerSkillChangedEventJS
import net.impleri.playerskills.integrations.kubejs.skills.PlayerSkillsKubeJSWrapper
import net.impleri.playerskills.integrations.kubejs.skills.SkillsEventsBinding
import net.impleri.playerskills.integrations.kubejs.skills.SkillsModificationEventJS
import net.impleri.playerskills.integrations.kubejs.skills.SkillsRegistrationEventJS
import net.impleri.playerskills.integrations.kubejs.skills.SpecializedSkillJS
import net.impleri.playerskills.integrations.kubejs.skills.TieredSkillJS
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import java.util.function.Consumer

class PlayerSkillsPlugin : KubeJSPlugin() {
  private val skillWrapper = PlayerSkillsKubeJSWrapper()
  override fun init() {
    LifecycleEvent.SERVER_STARTING.register(LifecycleEvent.ServerState { onServerStart(it) })

    SkillChangedEvent.EVENT.register(Consumer { onSkillChange(it) })

    Registries.SKILLS.addType(
      BasicSkillType.NAME.toString(),
      BasicSkillJS.Builder::class.java,
    ) { BasicSkillJS.Builder(it) }

    Registries.SKILLS.addType(
      NumericSkillType.NAME.toString(),
      NumericSkillJS.Builder::class.java,
    ) { NumericSkillJS.Builder(it) }

    Registries.SKILLS.addType(
      TieredSkillType.NAME.toString(),
      TieredSkillJS.Builder::class.java,
    ) { TieredSkillJS.Builder(it) }

    Registries.SKILLS.addType(
      SpecializedSkillType.NAME.toString(),
      SpecializedSkillJS.Builder::class.java,
    ) { SpecializedSkillJS.Builder(it) }
  }

  private fun onServerStart(server: MinecraftServer) {
    // Trigger skills modification event
    SkillsEventsBinding.MODIFICATION.post(SkillsModificationEventJS(Registries.SKILLS.types))
    MobEventsBinding.RESTRICTIONS.post(MobRestrictionsEventJS(server))
  }

  override fun registerEvents() {
    SkillsEventsBinding.GROUP.register()
    MobEventsBinding.GROUP.register()
  }

  override fun initStartup() {
    registerSkills()
  }

  override fun registerBindings(event: BindingsEvent) {
    event.add("PlayerSkills", skillWrapper)
  }

  override fun attachPlayerData(event: AttachedData<Player>) {
    event.add("skills", MutablePlayerDataJS(event.parent))
  }

  private fun registerSkills() {
    SkillsEventsBinding.REGISTRATION.post(SkillsRegistrationEventJS(Registries.SKILLS.types))
  }

  private fun <T> onSkillChange(event: SkillChangedEvent<T>) {
    SkillsEventsBinding.SKILL_CHANGED.post(event.skill.name, PlayerSkillChangedEventJS(event))
  }
}
