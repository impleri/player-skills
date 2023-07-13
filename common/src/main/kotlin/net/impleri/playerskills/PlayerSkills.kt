package net.impleri.playerskills

import dev.architectury.platform.Platform
import dev.architectury.registry.registries.DeferredRegister
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.network.Manager
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.registry.SkillTypes
import net.impleri.playerskills.skills.registry.Skills
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

object PlayerSkills {
  const val MOD_ID = "playerskills"

  internal val LOGGER = PlayerSkillsLogger.create(MOD_ID, "SKILLS")

  private val SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey<SkillType<*>>(SkillTypes.REGISTRY_KEY)
  private val SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY)

  fun init() {
    registerTypes()
    Manager.register()
    EventHandlers.init()

    LOGGER.info("PlayerSkills Loaded")

    // @TODO: Maybe move elsewhere?
    if (Platform.isModLoaded("ftbquests")) {
      net.impleri.playerskills.integrations.ftbquests.PlayerSkillsIntegration.init()
    }
  }

  private fun registerTypes() {
    SkillTypes.buildRegistry()
    Skills.buildRegistry()
    SKILL_TYPES.register(BasicSkillType.NAME) { BasicSkillType() }
    SKILL_TYPES.register(NumericSkillType.NAME) { NumericSkillType() }
    SKILL_TYPES.register(TieredSkillType.NAME) { TieredSkillType() }
    SKILL_TYPES.register(SpecializedSkillType.NAME) { SpecializedSkillType() }
    SKILL_TYPES.register()
  }

  fun toggleDebug(): Boolean {
    return LOGGER.toggleDebug()
  }

  fun <T> emitSkillChanged(player: Player, newSkill: Skill<T>, oldSkill: Skill<T>) {
    SkillChangedEvent.EVENT.invoker().accept(SkillChangedEvent(player, newSkill, oldSkill))

    if (player is ServerPlayer) {
      val message = newSkill.getNotification(oldSkill.value)
      if (message != null) {
        player.sendSystemMessage(message, true)
      }
    }
  }
}
