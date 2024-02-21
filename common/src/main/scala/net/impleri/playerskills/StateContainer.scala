package net.impleri.playerskills

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.bindings.InteractionEvents
import net.impleri.playerskills.bindings.LifecycleEvents
import net.impleri.playerskills.bindings.PlayerEvents
import net.impleri.playerskills.facades.architectury.Network
import net.impleri.playerskills.facades.architectury.Registrar
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.restrictions.recipe.RecipeRestrictionOps
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger

/**
 * Single place for all stateful classes shared between client and server
 */
case class StateContainer(
  private val skillRegistrar: Registrar[Skill[_]] = Registrar(None),
  private val skillTypeRegistrar: Registrar[SkillType[_]] = Registrar(None),
  RESTRICTIONS: RestrictionRegistry = RestrictionRegistry(),
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  val SKILL_TYPES: SkillTypeRegistry = SkillTypeRegistry(skillTypeRegistrar)
  val SKILLS: SkillRegistry = SkillRegistry(gameRegistrar = skillRegistrar)

  lazy val SKILL_TYPE_OPS: SkillTypeOps = SkillType(SKILL_TYPES)

  lazy val SKILL_OPS: SkillOps = Skill(SKILL_TYPE_OPS, SKILLS)

  lazy val ITEM_RESTRICTIONS: ItemRestrictionOps = ItemRestrictionOps(RESTRICTIONS)

  lazy val RECIPE_RESTRICTIONS: RecipeRestrictionOps = RecipeRestrictionOps(RESTRICTIONS)

  val NETWORK: Network = Network()
  private val LIFECYCLE = LifecycleEvents(onSetup)
  private val INTERACTION = InteractionEvents(ITEM_RESTRICTIONS)
  private val PLAYER = PlayerEvents(ITEM_RESTRICTIONS)

  logger.info("PlayerSkills Loaded")
  LIFECYCLE.registerEvents()
  INTERACTION.registerEvents()
  PLAYER.registerEvents()

  private def onSetup(): Unit = {
    SKILL_TYPES.resync()
    SKILLS.resync()
  }
}
