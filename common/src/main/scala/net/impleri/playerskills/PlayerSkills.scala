package net.impleri.playerskills

import dev.architectury.registry.registries.DeferredRegister
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.minecraft.resources.ResourceKey

import scala.util.chaining.scalaUtilChainingOps

object PlayerSkills {
  final val MOD_ID = "playerskills"

  val STATE: StateContainer = StateContainer()

  private val SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey[SkillType[_]](SkillTypeRegistry.REGISTRY_KEY)
  private val SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY)

  def init(): Unit = {
    registerTypes()
  }

  private def registerTypes() = {
    val skillTypeOps = SkillType(STATE.SKILL_TYPES)
    val skillOps = Skill(skillTypeOps, STATE.SKILLS)
    SKILL_TYPES
      .tap(_.register(BasicSkillType.NAME, () => BasicSkillType(skillOps)))
      .tap(_.register(NumericSkillType.NAME, () => NumericSkillType(skillOps)))
      .tap(_.register(TieredSkillType.NAME, () => TieredSkillType(skillOps)))
      .tap(_.register(SpecializedSkillType.NAME, () => SpecializedSkillType(skillOps)))
      .tap(_.register())
  }
}
