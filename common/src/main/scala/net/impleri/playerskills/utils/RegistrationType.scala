package net.impleri.playerskills.utils

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

sealed trait RegistrationTarget
object RegistrationTarget {
  case object Namespace extends RegistrationTarget
  case object Tag extends RegistrationTarget
  case object Single extends RegistrationTarget
}

sealed abstract class RegistrationType[T](target: T) {
  def when(targetType: RegistrationTarget)(consumer: T => Unit): Unit = target match {
    case t: String if targetType == RegistrationTarget.Namespace => consumer(target)
    case t: ResourceLocation if targetType == RegistrationTarget.Single => consumer(target)
    case t: TagKey[_] if targetType == RegistrationTarget.Tag => consumer(target)
    case _ =>
  }

  def ifTag(consumer: T => Unit): Unit = when(RegistrationTarget.Tag)(consumer)
  def ifNamespace(consumer: T => Unit): Unit = when(RegistrationTarget.Namespace)(consumer)
  def ifSingle(consumer: T => Unit): Unit = when(RegistrationTarget.Single)(consumer)
}

object RegistrationType {
  private case class Namespace(target: String) extends RegistrationType(target)
  private case class Tag[T](target: TagKey[T]) extends RegistrationType(target)
  private case class Single(target: ResourceLocation) extends RegistrationType(target)

  def apply[T](value: String, registryKey: ResourceKey[Registry[T]]): Option[RegistrationType[_]] = value.trim match {
    case s"@$namespace" => Some(RegistrationType.Namespace(namespace))
    case s"$namespace:*" => Some(RegistrationType.Namespace(namespace))

    case s"#$tag" => SkillResourceLocation.ofMinecraft(tag)
      .map(TagKey.create(registryKey, _))
      .map(RegistrationType.Tag(_))

    case _ => SkillResourceLocation.ofMinecraft(value).map(RegistrationType.Single)
  }
}
