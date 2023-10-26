package net.impleri.playerskills.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.server.api.Player
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.{Player => MinePlayer}

import scala.util.Try

trait SkillValueCommand extends ValuesCommandUtils {
  private val REQUIRED_PERMISSION = 2

  protected def registerValueCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("value")
        .`then`(
          Commands.argument("player", EntityArgument.player())
            .requires(c => c.hasPermission(REQUIRED_PERMISSION))
            .`then`(
              Commands.argument("skill", ResourceLocationArgument.id())
                .executes(
                  c => getSkillValue(
                    c.getSource,
                    Try(EntityArgument.getPlayer(c, "player")).toOption,
                    Try(ResourceLocationArgument.getId(c, "skill")).toOption,
                  ),
                ),
            ),
        )
        .`then`(
          Commands.argument("skill", ResourceLocationArgument.id())
            .executes(
              c => getSkillValue(
                c.getSource,
                Try(c.getSource.getPlayerOrException).toOption,
                Try(ResourceLocationArgument.getId(c, "skill")).toOption,
              ),
            ),
        ),
    )
  }

  private def getSkillValue(
    source: CommandSourceStack,
    player: Option[MinePlayer],
    skillName: Option[ResourceLocation],
  ): Int = {
    val foundSkill = player.flatMap(p => skillName.flatMap(Player().get(p, _)))
    val message = if (foundSkill.nonEmpty) {
      Component.translatable("commands.playerskills.acquired_skills",
        1,
      )
    } else {
      Component.translatable("commands.playerskills.no_acquired_skills")
    }
    listValues(source, message, foundSkill.map(s => s"${s.name} = ${s.value.getOrElse("None")}").toList)
  }
}
