package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.{CommandAction, CommandSegment, CommandString, ResourceLocationArgument, StringArgument}

trait ListRestrictionsCommand extends WithNames {
  protected def restrictions: RestrictionRegistry

  protected val options: Map[String, RestrictionType] = Map(
    "items" -> RestrictionType.Item,
    "recipes" -> RestrictionType.Recipe,
  )

  protected def registerRestrictionsCommand[T <: CommandSegment.Any](builder: T): T =
    builder.option(
      CommandString("restrictions")
        .requireAdmin()
        .options(
          options
            .keys
            .map(option => CommandString(option)
              .option(ResourceLocationArgument("target").executes(CommandAction(handlerFor(option)).message()))
            ).toSeq
        )
        .executes(CommandAction(handlerForAll).message())
    )
    .asInstanceOf[T]

  private def handlerFor(option: String): CommandAction.Callback = context =>
    {
      val tuple = for {
        target <- ResourceLocationArgument.getValue("target", context)
        restrictionType <- options.get(option)
      } yield (target, restrictions.get(restrictionType, target))

      val message = tuple match {
        case Some((target, found)) if found.nonEmpty => TranslatableText("commands.playerskills.registered_restrictions_type", found.size, target)
        case _ => TranslatableText("commands.playerskills.no_registered_restrictions")
      }

      Right(message)
    }

  private val handlerForAll: CommandAction.Callback = _ =>
    {
      val message = restrictions.entries match {
        case found if found.nonEmpty => TranslatableText("commands.playerskills.registered_restrictions", found.size)
        case _ => TranslatableText("commands.playerskills.no_registered_restrictions")
      }

      Right(message)
    }
}
