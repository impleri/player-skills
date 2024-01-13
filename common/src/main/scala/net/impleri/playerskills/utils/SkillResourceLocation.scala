package net.impleri.playerskills.utils

import net.minecraft.resources.ResourceLocation

import scala.util.Failure
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

// @TODO: refactor to facade
object SkillResourceLocation {
  def apply(original: String): Option[ResourceLocation] = of(original)

  private def toOptionLog(original: String)(result: Try[ResourceLocation]): Option[ResourceLocation] = {
    result
      .tap {
        case Failure(_) => PlayerSkillsLogger.SKILLS.error(s"Could not parse resource location $original")
        case _ => ()
      }.toOption
      .flatMap(Option(_))
  }

  def of(namespace: String, path: String): Option[ResourceLocation] = {
    Try(
      new ResourceLocation(namespace, path),
    ).pipe(toOptionLog(s"$namespace:$path"))
  }

  def of(value: String): Option[ResourceLocation] = {
    value match {
      case s"$namespace:$path" => of(namespace, path)
      case path if !value.contains(":") => of(DEFAULT_NAMESPACE, path)
    }
  }

  def ofMinecraft(value: String): Option[ResourceLocation] = {
    Try(ResourceLocation.tryParse(value)).pipe(toOptionLog(value))
  }

  private[utils] val DEFAULT_NAMESPACE = "skills"
}
