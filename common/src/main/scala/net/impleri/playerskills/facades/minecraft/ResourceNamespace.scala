package net.impleri.playerskills.facades.minecraft

import net.impleri.playerskills.facades.minecraft.world.Item

case class ResourceNamespace(namespace: String) extends IsIngredient {
  override def inList(ingredients: Seq[Item]): Boolean = {
    ingredients.exists(_.isNamespaced(namespace))
  }
}
