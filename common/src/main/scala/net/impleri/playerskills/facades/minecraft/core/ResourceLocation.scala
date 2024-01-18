package net.impleri.playerskills.facades.minecraft.core

import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.{ResourceLocation => McResourceLocation}

import scala.util.Failure
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

case class ResourceLocation(name: McResourceLocation) {
  def getName: Option[McResourceLocation] = Option(name)

  def getAsString: String = name.toString

  def getNamespace: String = name.getNamespace

  def getPath: String = name.getPath

  override def equals(obj: Any): Boolean = {
    obj match {
      case r: ResourceLocation => name.equals(r.name)
      case r: McResourceLocation => name.equals(r)
      case _ => false
    }
  }

  override def toString: String = getAsString
}

object ResourceLocation {
  def apply(name: McResourceLocation): ResourceLocation = new ResourceLocation(name)

  def apply(namespace: String, path: String): Option[ResourceLocation] = {
    Try(
      new McResourceLocation(namespace, path),
    ).tap {
        case Failure(_) => PlayerSkillsLogger
          .SKILLS
          .error(s"Could not parse resource location $namespace${McResourceLocation.NAMESPACE_SEPARATOR}$path")
        case _ => ()
      }
      .toOption
      .map(apply)
  }

  def apply(value: String, isSkill: Boolean = true): Option[ResourceLocation] = {
    value match {
      case s"$namespace:$path" => apply(namespace, path)
      case path if !value.contains(McResourceLocation.NAMESPACE_SEPARATOR) && isSkill => apply(DEFAULT_NAMESPACE, path)
      case path if !value.contains(McResourceLocation.NAMESPACE_SEPARATOR) && !isSkill => {
        apply(McResourceLocation
          .DEFAULT_NAMESPACE, path,
        )
      }
    }
  }

  private[core] val DEFAULT_NAMESPACE = "skills"
}
