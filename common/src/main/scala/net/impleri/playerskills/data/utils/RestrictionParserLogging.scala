package net.impleri.playerskills.data.utils

import net.impleri.playerskills.restrictions.conditions.TargetedRestriction
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation

trait RestrictionParserLogging {
  def logger: Logger

  protected def logLoading[T](name: ResourceLocation): T => Unit =
    logger.debugT[T](s"Parsing JSON for $name")

  protected def logBuilder(restrictionType: String, name: ResourceLocation): TargetedRestriction => Unit = builder => {
    if (builder.isValid) logger.trace(s"Created $restrictionType builder for ${builder.getTarget} as $name") else logger.warn(s"Could not parse $restrictionType restriction $name for ${builder.getTarget}")
  }
}
