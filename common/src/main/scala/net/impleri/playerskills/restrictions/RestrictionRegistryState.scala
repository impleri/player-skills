package net.impleri.playerskills.restrictions

import cats.data.State
import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.facades.minecraft.HasName
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation

import scala.collection.View

object RestrictionRegistryState {
  final case class Restrictions private (restrictions: List[Restriction[_]]) {
    protected def matchesTarget(kind: RestrictionType, name: ResourceLocation)(restriction: Restriction[_]): Boolean = {
      restriction.isType(kind) && restriction.targets(name)
    }

    def entries: List[Restriction[_]] = restrictions

    def get[T <: HasName](kind: RestrictionType, name: ResourceLocation): View[Restriction[T]] = {
      restrictions
        .view
        .filter(matchesTarget(kind, name))
        .asInstanceOf[View[Restriction[T]]]
    }

    def has(kind: RestrictionType, name: ResourceLocation): Boolean = {
      restrictions
        .view
        .exists(matchesTarget(kind, name))
    }

    def add(restriction: Restriction[_]): Restrictions = Restrictions(restrictions :+ restriction)
  }

  private def readOp[T](f: Restrictions => T): State[Restrictions, T] = State[Restrictions, T](s => (s, f(s)))

  val empty: Restrictions = Restrictions(List.empty)

  def add(restriction: Restriction[_]): State[Restrictions, Unit] = State.modify(_.add(restriction))

  def entries(): State[Restrictions, List[Restriction[_]]] = {
    State[Restrictions, List[Restriction[_]]](s => (s, s.entries))
  }

  def get[T <: HasName](kind: RestrictionType, name: ResourceLocation): State[Restrictions, View[Restriction[T]]] = {
    readOp(_.get[T](kind, name))
  }

  def has(kind: RestrictionType, name: ResourceLocation): State[Restrictions, Boolean] = readOp(_.has(kind, name))
}
