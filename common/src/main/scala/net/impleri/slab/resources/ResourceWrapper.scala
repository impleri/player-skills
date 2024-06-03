package net.impleri.slab.resources

trait Registerable

trait ResourceWrapper[T] extends Registerable {
  protected def underlying: T

  def name: Option[ResourceLocation]

  def value: T = underlying
}
