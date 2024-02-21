package net.impleri.playerskills.facades.minecraft

import net.impleri.playerskills.facades.minecraft.world.Item

trait IsIngredient {
  def inList(ingredients: Seq[Item]): Boolean = {
    false
  }
}
