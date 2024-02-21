package net.impleri.playerskills.facades.minecraft.world

import com.mojang.brigadier.StringReader
import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.facades.minecraft.HasName
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.IsIngredient
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.{Item => MCItem}
import net.minecraft.world.item.Items
import net.minecraft.world.item.ItemStack

import scala.util.Try

case class Item(
  private val item: MCItem,
  private val stack: Option[ItemStack] = None,
  private val quantity: Int = 1,
  private val registry: Registry[MCItem] = Registry.Items,
) extends HasName with IsIngredient {
  def name: String = getName.fold("nothing")(_.toString)

  def getName: Option[ResourceLocation] = registry.getKey(item)

  def getStack: ItemStack = stack.getOrElse(new ItemStack(item, quantity))

  def isDefault: Boolean = getName == Item.DEFAULT_ITEM.getName

  def isEmptyStack: Boolean = stack.fold(quantity == 0)(_.isEmpty)

  def isEmpty: Boolean = !(isDefault || isEmptyStack)

  def isEnchanted: Boolean = getStack.isEnchanted

  def isNamespaced(namespace: String): Boolean = {
    getName.forall(_.getNamespace == namespace)
  }

  def is(that: Item): Boolean = {
    (name == that.name) && (ItemStack.isSameItemSameTags(getStack, that.getStack))
  }

  def matches(that: IsIngredient): Boolean = {
    that.inList(Seq(this))
  }

  override def inList(ingredients: Seq[Item]): Boolean = {
    ingredients.exists(is)
  }
}

object Item {
  def DEFAULT_ITEM: Item = Item(Items.AIR)

  def apply(itemStack: ItemStack): Item = {
    new Item(itemStack.getItem,
      Option(itemStack),
      itemStack.getCount,
    )
  }

  def apply(entity: ItemEntity): Item = apply(entity.getItem)

  def apply(name: ResourceLocation): Option[Item] = {
    Registry.Items.get(name).map(Item(_))
  }

  def apply(item: MCItem, tag: CompoundTag): Item = {
    val stack = new ItemStack(item)
    stack.setTag(tag)

    Item(stack)
  }

  /**
   * Parse
   *
   * Creates an Item facade using a string representation of item plus nbt if parsing is successful
   */
  def parse(identifier: String): Option[Item] = {
    Try(
      ItemParser.parseForItem(
        Registry.Items.getHolder,
        new StringReader(identifier),
      ),
    )
      .toOption
      .map(r => (r.item().value(), Option(r.nbt())))
      .map(t => if (t._2.nonEmpty) Item(t._1, t._2.get) else Item(t._1))
  }
}
