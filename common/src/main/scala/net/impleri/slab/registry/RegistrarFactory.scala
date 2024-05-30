package net.impleri.slab.registry

import net.impleri.slab.resources.ResourceLocation

case class RegistrarFactory(modId: String) {
  def create[T](key: ResourceLocation): Registrar[T] = Registrar(key, modId)
}
