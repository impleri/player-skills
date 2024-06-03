package net.impleri.playerskills.restrictions

import cats.data.State
import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper

import scala.collection.View

object RestrictionRegistryState {
  final case class Restrictions private[restrictions] (restrictions: List[Restriction[_, _]]) {
    protected def matchesTarget(kind: RestrictionType, name: ResourceLocation)
      (restriction: Restriction[_, _]): Boolean = {
      restriction.isType(kind) && restriction.targets(name)
    }

    def entries: List[Restriction[_, _]] = restrictions

    def get[T <: ResourceWrapper[U], U](kind: RestrictionType, name: ResourceLocation): View[Restriction[T, U]] = {
      restrictions
        .view
        .filter(matchesTarget(kind, name))
        .asInstanceOf[View[Restriction[T, U]]]
    }

    def has(kind: RestrictionType, name: ResourceLocation): Boolean = {
      restrictions
        .view
        .exists(matchesTarget(kind, name))
    }

    def add(restriction: Restriction[_, _]): Restrictions = Restrictions(restrictions :+ restriction)
  }

  private def readOp[T](f: Restrictions => T): State[Restrictions, T] = State[Restrictions, T](s => (s, f(s)))

  val empty: Restrictions = Restrictions(List.empty)

  def add(restriction: Restriction[_, _]): State[Restrictions, Unit] = State.modify(_.add(restriction))

  def entries(): State[Restrictions, List[Restriction[_, _]]] = {
    State[Restrictions, List[Restriction[_, _]]](s => (s, s.entries))
  }

  def get[T <: ResourceWrapper[U], U](
    kind: RestrictionType,
    name: ResourceLocation,
  ): State[Restrictions, View[Restriction[T, U]]] = {
    readOp(_.get[T, U](kind, name))
  }

  def has(kind: RestrictionType, name: ResourceLocation): State[Restrictions, Boolean] = readOp(_.has(kind, name))
}
