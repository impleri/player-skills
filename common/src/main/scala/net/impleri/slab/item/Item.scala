package net.impleri.slab.item

import com.mojang.brigadier.StringReader
import net.impleri.slab.registry.IsIngredient
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.{Item => MCItem}
import net.minecraft.world.item.Items
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

import scala.util.Try

case class Item(
  override val underlying: MCItem,
  private val stack: Option[ItemStack] = None,
  private val quantity: Int = 1,
  private val registry: Registry[Item, MCItem] = Registry.Items,
) extends ResourceWrapper[MCItem]
    with IsIngredient {
  def asString: String = name.fold("nothing")(_.toString)

  override val name: Option[ResourceLocation] = registry.getKey(this)

  def getStack: ItemStack = stack.getOrElse(new ItemStack(underlying, quantity))

  def getAmountInStack: Int = quantity

  def isDefault: Boolean = name == Item.DEFAULT_ITEM.name

  def isEmptyStack: Boolean = stack.fold(quantity == 0)(_.isEmpty)

  def isEmpty: Boolean = !(isDefault || isEmptyStack)

  def isEnchanted: Boolean = getStack.isEnchanted

  def isNamespaced(namespace: String): Boolean =
    name.forall(_.namespace == namespace)

  def is(that: Item): Boolean =
    (name == that.name) && ItemStack.isSameItemSameTags(getStack, that.getStack)

  def matches(that: IsIngredient): Boolean =
    that.inList(Seq(this))

  override def inList(ingredients: Seq[Item]): Boolean =
    ingredients.exists(is)
}

object Item {
  type Vanilla = MCItem

  type VanillaIngredient = Ingredient

  type VanillaStack = ItemStack

  def EMPTY_STACK: VanillaStack = ItemStack.EMPTY

  def DEFAULT_ITEM: Item = new Item(Items.AIR)

  def apply(itemStack: ItemStack): Item = {
    new Item(itemStack.getItem, Option(itemStack), itemStack.getCount)
  }

  def apply(entity: ItemEntity): Item = apply(entity.getItem)

  def apply(name: ResourceLocation): Option[Item] =
    Registry.Items.get(name)

  def apply(item: MCItem, tag: CompoundTag): Item = {
    val stack = new ItemStack(item)
    stack.setTag(tag)

    Item(stack)
  }

  /** Parse
    *
    * Creates an Item facade using a string representation of item plus nbt if
    * parsing is successful
    */
  def parse(identifier: String): Option[Item] =
    for {
      holder <- Try(ItemParser.parseForItem(
        Registry.Items.getHolder,
        new StringReader(identifier),
      )
      ).toOption
      item = holder.item().value()
      tag = Option(holder.nbt())
    } yield {
      if (tag.nonEmpty) Item(item, tag.get) else Item(item)
    }
}
