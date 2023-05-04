package net.impleri.playerskills.integrations.kubejs

import dev.architectury.event.events.common.LifecycleEvent
import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.script.BindingsEvent
import dev.latvian.mods.kubejs.util.AttachedData
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.integrations.kubejs.events.EventsBinding
import net.impleri.playerskills.integrations.kubejs.events.PlayerSkillChangedEventJS
import net.impleri.playerskills.integrations.kubejs.events.SkillsModificationEventJS
import net.impleri.playerskills.integrations.kubejs.events.SkillsRegistrationEventJS
import net.impleri.playerskills.integrations.kubejs.skills.BasicSkillJS
import net.impleri.playerskills.integrations.kubejs.skills.MutablePlayerDataJS
import net.impleri.playerskills.integrations.kubejs.skills.NumericSkillJS
import net.impleri.playerskills.integrations.kubejs.skills.SpecializedSkillJS
import net.impleri.playerskills.integrations.kubejs.skills.TieredSkillJS
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.minecraft.world.entity.player.Player
import java.util.function.Consumer

class PlayerSkillsPlugin : KubeJSPlugin() {
  private val skillWrapper = PlayerSkillsKubeJSWrapper()
  override fun init() {
    LifecycleEvent.SERVER_STARTING.register(LifecycleEvent.ServerState { onServerStart() })

    SkillChangedEvent.EVENT.register(Consumer { onSkillChange(it) })

    Registries.SKILLS.addType(
      BasicSkillType.NAME.toString(),
      BasicSkillJS.Builder::class.java
    ) { BasicSkillJS.Builder(it) }

    Registries.SKILLS.addType(
      NumericSkillType.NAME.toString(),
      NumericSkillJS.Builder::class.java
    ) { NumericSkillJS.Builder(it) }

    Registries.SKILLS.addType(
      TieredSkillType.NAME.toString(),
      TieredSkillJS.Builder::class.java
    ) { TieredSkillJS.Builder(it) }

    Registries.SKILLS.addType(
      SpecializedSkillType.NAME.toString(),
      SpecializedSkillJS.Builder::class.java
    ) { SpecializedSkillJS.Builder(it) }
  }

  private fun onServerStart() {
    // Trigger skills modification event
    EventsBinding.MODIFICATION.post(SkillsModificationEventJS(Registries.SKILLS.types))
  }

  override fun registerEvents() {
    EventsBinding.GROUP.register()
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
    EventsBinding.REGISTRATION.post(SkillsRegistrationEventJS(Registries.SKILLS.types))
  }

  private fun <T> onSkillChange(event: SkillChangedEvent<T>) {
    EventsBinding.SKILL_CHANGED.post(event.skill.name, PlayerSkillChangedEventJS(event))
  }
}
