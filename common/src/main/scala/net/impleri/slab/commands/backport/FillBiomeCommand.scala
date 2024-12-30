package net.impleri.slab.commands.backport

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.{BaseCommand, Command, CommandAction, CommandSegment, CommandString, CoordinatesArgument, ResourceLocationArgument}
import net.impleri.slab.registry.BuiltinRegistry

import scala.util.chaining.scalaUtilChainingOps

case class FillBiomeCommand() extends BaseCommand {
  override def register(
    dispatcher: CommandDispatcher[Command.Source],
  ): LiteralCommandNode[Command.Source] =
    dispatcher.register(command.asRoot)

  override def command: CommandString =
    CommandString("fillbiome")
      .requireAdmin()
      .option(
        CoordinatesArgument(FillBiomeCommand.fromArgument).option(
          CoordinatesArgument(FillBiomeCommand.toArgument).option(
            ResourceLocationArgument(FillBiomeCommand.biomeArgument).executes(CommandAction(handler))
          ),
        ),
      )
      .asInstanceOf[CommandString]

  private def handler: CommandAction.Callback = context =>
    {
      val message = for {
        fromCoords <- CoordinatesArgument.getValue(FillBiomeCommand.fromArgument, context)
        toCoords <- CoordinatesArgument.getValue(FillBiomeCommand.toArgument, context)
        biomeName <- ResourceLocationArgument.getValue(FillBiomeCommand.biomeArgument, context)
        biome <- BuiltinRegistry.BIOME.get(biomeName)
        player <- CommandAction.getPlayer(context, skipArgument = true)
        level <- player.level
        climateSampler <- level.getClimateSampler
        chunks = level.getChunksBetween(fromCoords, toCoords)
      } yield {
        chunks.foreach {
          _.setBiome(biome, climateSampler)
            .pipe(level.updateChunk)
        }

        TranslatableText("commands.slab.fill_biomes")
      }

      message.toRight(TranslatableText("commands.slab.fill_biomes_failed"))
    }

  override protected def builders[T <: CommandSegment.Any]: List[T => T] = List.empty
}

object FillBiomeCommand {
  private final val fromArgument: String = "from"
  private final val toArgument: String = "to"
  private final val biomeArgument: String = "biome"
}
