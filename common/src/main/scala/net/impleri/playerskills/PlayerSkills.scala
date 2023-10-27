package net.impleri.playerskills

import dev.architectury.registry.registries.DeferredRegister
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

import scala.util.chaining.scalaUtilChainingOps

object PlayerSkills {
  final val MOD_ID = "playerskills"

  private val SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey[SkillType[_]](SkillTypeRegistry.REGISTRY_KEY)
  private val SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY)

  def init(): Unit = {
    registerTypes()
    StateContainer.init()
  }

  def emitSkillChanged[T](player: Player, newSkill: Skill[T], oldSkill: Option[Skill[T]]): Unit = {
    SkillChangedEvent.EVENT.invoker().accept(SkillChangedEvent[T](player, Option(newSkill), oldSkill))

    player match {
      case sp: ServerPlayer => {
        newSkill
          .getNotification(oldSkill.flatMap(_.value))
          .foreach(sp.sendSystemMessage(_, true))
      }
    }
  }

  private def registerTypes() = {
    SKILL_TYPES
      .tap(_.register(BasicSkillType.NAME, () => BasicSkillType()))
      .tap(_.register(NumericSkillType.NAME, () => NumericSkillType()))
      .tap(_.register(TieredSkillType.NAME, () => TieredSkillType()))
      .tap(_.register(SpecializedSkillType.NAME, () => SpecializedSkillType()))
      .tap(_.register())
  }
}
