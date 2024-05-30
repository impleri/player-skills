package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.skills.Skill
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString
import net.impleri.slab.commands.PlayerArgument
import net.impleri.slab.entity.Player

trait ImproveSkillCommand extends SetCommandUtils {
  protected def registerImproveCommand(builder: CommandSegment.Any): CommandSegment.Any = {
    builder.option(
      CommandString("improve")
        .requireMod()
        .option(
          PlayerArgument().option(SkillHandler.getArgument.executes(CommandAction(handler(false)).message())),
        ).option(
          SkillHandler.getArgument.executes(CommandAction(handler(true)).message()),
        ),
    )
  }

  protected def successMessage: String = "commands.playerskills.skill_improved"

  protected def failureMessage: String = "commands.playerskills.skill_improve_failed"

  protected def action[T](player: Player.Any, skill: Skill[T]): Option[Boolean] = {
    teamOps.improve(player, skill, None, None)
  }
}
