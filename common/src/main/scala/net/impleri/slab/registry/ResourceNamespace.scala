package net.impleri.slab.registry

import net.impleri.slab.item.Item

case class ResourceNamespace(namespace: String) extends IsIngredient {
  override def inList(ingredients: Seq[Item]): Boolean =
    ingredients.exists(_.isNamespaced(namespace))
}
