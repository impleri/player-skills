package net.impleri.playerskills.facades.minecraft.world

import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.facades.minecraft.HasName
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.{Item => MCItem}
import net.minecraft.world.item.Items
import net.minecraft.world.item.ItemStack

case class Item(
  private val item: MCItem,
  private val stack: Option[ItemStack] = None,
  private val quantity: Int = 1,
  private val registry: Registry[MCItem] = Registry.Items,
) extends HasName {
  def name: String = getName.fold("nothing")(_.toString)

  def getName: Option[ResourceLocation] = registry.getKey(item)

  def getStack: ItemStack = stack.getOrElse(new ItemStack(item, quantity))

  def isDefault: Boolean = getName == Item.DEFAULT_ITEM.getName

  def isEmptyStack: Boolean = stack.fold(false)(_.isEmpty)

  def isEmpty: Boolean = !(isDefault || isEmptyStack)
}

object Item {
  def DEFAULT_ITEM: Item = Item(Items.AIR)

  def apply(itemStack: ItemStack): Item = new Item(itemStack.getItem, Option(itemStack), itemStack.getCount)

  def apply(entity: ItemEntity): Item = apply(entity.getItem)

  def apply(name: ResourceLocation): Option[Item] = {
    Registry.Items.get(name).map(Item(_))
  }
}
