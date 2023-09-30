package net.impleri.playerskills

import dev.architectury.registry.registries.DeferredRegister
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.registry.Players
import net.impleri.playerskills.skills.registry.SkillTypes
import net.impleri.playerskills.skills.registry.Skills
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

import scala.util.chaining._

object PlayerSkills {
  final val MOD_ID = "playerskills"

  private val SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey[SkillType[_]](SkillTypes.REGISTRY_KEY)
  private val SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY)

  def init(): Unit = {
    initializeRegistries()
    registerTypes()
    EventHandlers.init()
    PlayerSkillsLogger.SKILLS.info("PlayerSkills Loaded")
  }

  // @TODO: Maybe move elsewhere?
    //    if (Platform.isModLoaded("ftbquests")) {
    //      net.impleri.playerskills.integrations.ftbquests.PlayerSkillsIntegration.init()
    //    }

  def emitSkillChanged[T](player: Player, newSkill: Skill[T], oldSkill: Option[Skill[T]]): Unit = {
    SkillChangedEvent.EVENT.invoker().accept(SkillChangedEvent[T](player, Some(newSkill), oldSkill))

    player match {
      case sp: ServerPlayer => newSkill
        .getNotification(oldSkill.flatMap(_.value))
        .foreach(sp.sendSystemMessage(_, true))
    }
  }

  private def initializeRegistries(): Unit = {
    Skills.init()
    Players.init()
    SkillTypes.init()
  }

  private def registerTypes() =
  SKILL_TYPES
    .tap(_.register(BasicSkillType.NAME, () => BasicSkillType()))
    .tap(_.register(NumericSkillType.NAME, () => NumericSkillType()))
    .tap(_.register(TieredSkillType.NAME, () => TieredSkillType()))
    .tap(_.register(SpecializedSkillType.NAME, () => SpecializedSkillType()))
    .tap(_.register())
}
