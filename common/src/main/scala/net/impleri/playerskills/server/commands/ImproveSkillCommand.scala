package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString
import net.impleri.slab.commands.PlayerArgument
import net.impleri.slab.entity.Player

trait ImproveSkillCommand {
  protected def skillOps: SkillOps

  protected def teamOps: TeamOps

  private val factory: SetCommandFactory = SetCommandFactory(
    skillOps,
    teamOps,
    action,
    successMessage,
    failureMessage,
  )

  protected def registerImproveCommand[T <: CommandSegment.Any](
    builder: T,
  ): T = {
    builder
      .option(
        CommandString("improve")
          .requireMod()
          .option(
            PlayerArgument()
              .option(
                SkillHandler.getArgument.executes(
                  CommandAction(factory.createCallback(false)).message(),
                ),
              ),
          )
          .option(
            SkillHandler.getArgument
              .executes(CommandAction(factory.createCallback(true)).message()),
          ),
      )
      .asInstanceOf[T]
  }

  private def successMessage: String = "commands.playerskills.skill_improved"

  private def failureMessage: String =
    "commands.playerskills.skill_improve_failed"

  private def action(player: Player.Any, skill: Skill[_]): Option[Boolean] = {
    teamOps.improve(player, skill, None, None)
  }
}
