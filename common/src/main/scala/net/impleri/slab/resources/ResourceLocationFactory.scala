package net.impleri.slab.resources

import net.impleri.slab.logging.Logger
import net.minecraft.resources.{ResourceLocation => McResourceLocation}

case class ResourceLocationFactory(private val defaultNamespace: String, logger: Logger = Logger.DEFAULT) {
  def create(value: String, useDefaultNS: Boolean = true): Option[ResourceLocation] = {
    value match {
      case s"$namespace:$path" => ResourceLocation(namespace, path)
      case path if !value.contains(McResourceLocation.NAMESPACE_SEPARATOR) && useDefaultNS =>
      ResourceLocation(defaultNamespace, path)

      case path if !value.contains(McResourceLocation.NAMESPACE_SEPARATOR) =>
      ResourceLocation(McResourceLocation.DEFAULT_NAMESPACE, path)

    }
  }
}
