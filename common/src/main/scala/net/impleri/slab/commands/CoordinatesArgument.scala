package net.impleri.slab.commands

import net.impleri.slab.world.Coordinates
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.coordinates.{Coordinates => McCoordinates, Vec3Argument}

import scala.util.Try

class CoordinatesArgument(override val underlying: Command.Argument[McCoordinates])
    extends CommandSegment[Command.Argument[McCoordinates], CoordinatesArgument](underlying)

object CoordinatesArgument {
  def apply(name: String): CoordinatesArgument = new CoordinatesArgument(
    Commands.argument(name, Vec3Argument.vec3()),
  )

  def getValue(name: String, context: Command.Context): Option[Coordinates] =
    Try(Vec3Argument.getVec3(context, name))
      .toOption
      .map(Coordinates(_))
}
