package net.impleri.playerskills

import dev.architectury.registry.registries.DeferredRegister
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.impleri.playerskills.skills.SkillRegistry
import net.minecraft.resources.ResourceKey

object PlayerSkills {
  final val MOD_ID = "playerskills"

  val STATE: StateContainer = StateContainer(SkillRegistry.REGISTRAR, SkillTypeRegistry.REGISTRAR)

  private val SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey[SkillType[_]](SkillTypeRegistry.REGISTRY_KEY)
  private val SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY)

  // We create the server-side handling here in case we are running in an integrated server/single-player instance
  PlayerSkillsServer.create()

  def init(): Unit = {
    registerTypes()
  }

  private def registerTypes(): Unit = {
    SKILL_TYPES.register(BasicSkillType.NAME, () => BasicSkillType(STATE.SKILL_OPS))
    SKILL_TYPES.register(NumericSkillType.NAME, () => NumericSkillType(STATE.SKILL_OPS))
    SKILL_TYPES.register(TieredSkillType.NAME, () => TieredSkillType(STATE.SKILL_OPS))
    SKILL_TYPES.register(SpecializedSkillType.NAME, () => SpecializedSkillType(STATE.SKILL_OPS))

    SKILL_TYPES.register()
  }
}
