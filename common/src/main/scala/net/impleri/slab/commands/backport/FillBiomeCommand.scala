package net.impleri.slab.commands.backport

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.{BaseCommand, Command, CommandAction, CommandSegment, CommandString, CoordinatesArgument, ResourceLocationArgument}
import net.impleri.slab.logging.Logger
import net.impleri.slab.registry.BiomeTag
import net.impleri.slab.world.Level

import scala.jdk.StreamConverters._
import scala.util.chaining.scalaUtilChainingOps

case class FillBiomeCommand(logger: Option[Logger] = None) extends BaseCommand {
  override def register(
    dispatcher: CommandDispatcher[Command.Source],
  ): LiteralCommandNode[Command.Source] =
    dispatcher.register(command.asRoot)

  override def command: CommandString =
    CommandString("fillbiome")
      .requireMod()
      .option(
        CoordinatesArgument(FillBiomeCommand.posArgument).option(
          ResourceLocationArgument(FillBiomeCommand.biomeArgument)
            .executes(CommandAction(handler).message())
        ),
      )
      .asInstanceOf[CommandString]

  private def handler: CommandAction.Callback = context =>
    {
      val level = Level(context.getSource.getLevel)
      val message = for {
        coords <- CoordinatesArgument.getValue(FillBiomeCommand.posArgument, context)
        biomeName <- ResourceLocationArgument.getValue(FillBiomeCommand.biomeArgument, context)
        biome <- level.getBiome(biomeName)
        climateSampler <- level.getClimateSampler
        chunk <- level.getChunk(coords.toPosition)
      } yield {
        chunk.setBiome(biome, climateSampler)
          .pipe(level.updateChunk)

        level.save()

        TranslatableText("commands.slab.fill_biomes_success", biome.toString, coords.toPosition.toString)
      }

      message.toRight(TranslatableText("commands.slab.fill_biomes_failed"))
    }

  override protected def builders[T <: CommandSegment.Any]: List[T => T] = List.empty
}

object FillBiomeCommand {
  private final val posArgument: String = "pos"
  private final val biomeArgument: String = "biome"
}
