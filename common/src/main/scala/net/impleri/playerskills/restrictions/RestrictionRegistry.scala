package net.impleri.playerskills.restrictions

import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.facades.minecraft.HasName
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.utils.StatefulRegistry

import scala.collection.View
import scala.util.chaining.scalaUtilChainingOps

class RestrictionRegistry(override var state: RestrictionRegistryState.Restrictions)
  extends StatefulRegistry[RestrictionRegistryState.Restrictions] {
  def entries: List[Restriction[_]] = {
    RestrictionRegistryState.entries().pipe(maintainState)
  }

  def get[T <: HasName](kind: RestrictionType, key: ResourceLocation): View[Restriction[T]] = {
    RestrictionRegistryState.get[T](kind, key).pipe(maintainState)
  }

  def has(kind: RestrictionType, key: ResourceLocation): Boolean = {
    RestrictionRegistryState.has(kind, key).pipe(maintainState)
  }

  def add(restriction: Restriction[_]): Boolean = {
    RestrictionRegistryState.add(restriction)
      .run(state).map(r => {
        state = r._1
        true
      },
      ).value
  }
}

object RestrictionRegistry {
  def apply(
    state: RestrictionRegistryState.Restrictions = RestrictionRegistryState.empty,
  ): RestrictionRegistry = {
    new RestrictionRegistry(state)
  }
}
