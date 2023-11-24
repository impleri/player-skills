package net.impleri.playerskills.server.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.facades.minecraft.{Player => MinecraftPlayer}
import net.impleri.playerskills.server.api.Player
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

import scala.jdk.FunctionConverters.enrichAsJavaPredicate

trait SkillValueCommand extends ValuesCommandUtils with WithPlayer with WithSkill with CommandHelpers {
  protected def playerOps: Player

  protected def registerValueCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("value")
        .`then`(
          getPlayerArg.requires(hasPermission().asJavaPredicate)
            .`then`(
              getSkillArg.executes(
                withListValuesContext(c => getSkillValue(getPlayer(c), getSkillName(c))),
              ),
            ),
        )
        .`then`(
          getSkillArg
            .executes(
              withListValuesContext(c => getSkillValue(Option(getCurrentPlayer(c.getSource)), getSkillName(c))),
            ),
        ),
    )
  }

  private[commands] def getSkillValue[T](
    player: Option[MinecraftPlayer[_]],
    skillName: Option[ResourceLocation],
  ): (Component, List[String]) = {
    val foundSkill = player.flatMap(p => skillName.flatMap(playerOps.get[T](p, _)))

    val message = if (foundSkill.nonEmpty) {
      Component.translatable("commands.playerskills.acquired_skills", 1)
    } else {
      Component.translatable("commands.playerskills.no_acquired_skills")
    }

    (message, foundSkill.map(s => s"${s.name} = ${s.value.getOrElse("None")}").toList)
  }
}
