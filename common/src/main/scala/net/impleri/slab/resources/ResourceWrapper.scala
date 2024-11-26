package net.impleri.slab.resources

trait Registerable

trait Named extends Registerable {
  def name: ResourceLocation
}

trait ResourceWrapper[T] extends Registerable {
  protected def underlying: T

  def name: Option[ResourceLocation]

  def value: T = underlying
}
