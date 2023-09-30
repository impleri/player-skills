package net.impleri.playerskills.utils

import net.minecraft.resources.ResourceLocation

object ListDiff {
  def contains[T](a: Seq[T], b: Seq[T], getName: T => ResourceLocation): Boolean = diff(a, b, getName).nonEmpty

  // TODO: Maybe Seq.diff is enough and this is not necessary?
  def diff[T](a: Seq[T], b: Seq[T], getName: T => ResourceLocation): Seq[T] = {
    val names = diffNames(a, b, getName)
    a.filter(v => names.contains(getName(v)))
  }

  private def diffNames[T](a: Seq[T], b: Seq[T], getName: T => ResourceLocation) = a.map(getName).diff(b.map(getName))
}
