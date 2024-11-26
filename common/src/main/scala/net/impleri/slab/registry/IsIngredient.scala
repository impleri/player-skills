package net.impleri.slab.registry

import net.impleri.slab.item.Item

trait IsIngredient {
  def inList(ingredients: Seq[Item]): Boolean =
    false
}
