package net.impleri.playerskills.api

import net.impleri.playerskills.items.ItemRestriction
import net.impleri.playerskills.items.ItemSkills
import net.impleri.playerskills.restrictions.RestrictionsApi
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import java.lang.reflect.Field
import net.impleri.playerskills.restrictions.Registry as RestrictionsRegistry

class ItemRestrictions private constructor(
  registry: RestrictionsRegistry<ItemRestriction>,
  fields: Array<Field>,
) : RestrictionsApi<Item, ItemRestriction>(registry, fields, ItemSkills.LOGGER) {
  override fun getTargetName(target: Item): ResourceLocation {
    return getName(target)
  }

  override fun createPredicateFor(target: Item): (Item) -> Boolean {
    return { it === target }
  }

  private fun canHelper(player: Player?, target: Item, pos: BlockPos?, fieldName: String): Boolean {
    player ?: return DEFAULT_CAN_RESPONSE

    val dimension = player.level.dimension().location()
    val actualPos = pos ?: player.onPos
    val biome = player.level.getBiome(actualPos).unwrapKey().orElseThrow().location()

    return canPlayer(player, target, dimension, biome, null, fieldName)
  }

  internal fun isProducible(player: Player?, item: Item, pos: BlockPos?): Boolean {
    return canHelper(player, item, pos, "producible")
  }

  internal fun isProducible(player: Player?, recipe: Recipe<*>, pos: BlockPos?): Boolean {
    val hasUncomsumable = recipe.ingredients
      .map { it.items }
      .flatMap { it.asSequence() }
      .any { !isConsumable(player, it.item, pos) }

    return !hasUncomsumable && isProducible(player, recipe.resultItem.item, pos)
  }

  internal fun isConsumable(player: Player?, item: Item, pos: BlockPos?): Boolean {
    return canHelper(player, item, pos, "consumable")
  }

  internal fun isHoldable(player: Player?, item: Item, pos: BlockPos?): Boolean {
    return canHelper(player, item, pos, "holdable")
  }

  internal fun isIdentifiable(player: Player?, item: Item, pos: BlockPos?): Boolean {
    return canHelper(player, item, pos, "identifiable")
  }

  internal fun isHarmful(player: Player?, item: Item, pos: BlockPos?): Boolean {
    return canHelper(player, item, pos, "harmful")
  }

  internal fun isWearable(player: Player?, item: Item, pos: BlockPos?): Boolean {
    return canHelper(player, item, pos, "wearable")
  }

  internal fun isWearable(player: Player?, stack: ItemStack, pos: BlockPos?): Boolean {
    return if (stack.isEmpty) DEFAULT_CAN_RESPONSE else canHelper(player, stack.item, pos, "wearable")
  }

  internal fun isUsable(player: Player?, item: Item, pos: BlockPos?): Boolean {
    return canHelper(player, item, pos, "usable")
  }

  companion object {
    internal val RestrictionRegistry = RestrictionsRegistry<ItemRestriction>()

    private val allRestrictionFields = ItemRestriction::class.java.declaredFields

    internal val INSTANCE = ItemRestrictions(RestrictionRegistry, allRestrictionFields)

    fun add(name: ResourceLocation, restriction: ItemRestriction) {
      RestrictionRegistry.add(name, restriction)
    }

    fun getName(stack: ItemStack): ResourceLocation {
      return getName(if (stack.isEmpty) null else stack.item)
    }

    fun getName(item: Item?): ResourceLocation {
      return Registry.ITEM.getKey(item)
    }

    fun getValue(resource: ResourceLocation?): Item {
      return Registry.ITEM[resource]
    }

    fun getValue(stack: ItemStack): Item {
      return stack.item
    }

    fun getValue(entity: ItemEntity): Item {
      return getValue(entity.item)
    }

    fun getStackOf(resource: ResourceLocation?): ItemStack {
      val item = getValue(resource)
      return ItemStack(item)
    }

    fun getItemUsed(player: Player, hand: InteractionHand): Item {
      val item = if (hand == InteractionHand.OFF_HAND) player.offhandItem else player.mainHandItem
      return getValue(item)
    }

    fun isDefaultItem(item: Item?): Boolean {
      return item == null || item === Items.AIR
    }

    fun isEmptyStack(stack: ItemStack?): Boolean {
      return stack == null || stack.isEmpty || isDefaultItem(stack.item)
    }

    fun canCraft(player: Player?, item: Item, pos: BlockPos?): Boolean {
      return INSTANCE.isProducible(player, item, pos)
    }

    fun canCraft(player: Player?, recipe: Recipe<*>, pos: BlockPos?): Boolean {
      return INSTANCE.isProducible(player, recipe, pos)
    }

    fun canCraft(player: Player?, level: Level, craftingContainer: CraftingContainer): Boolean {
      val server = level.server ?: return DEFAULT_CAN_RESPONSE
      val recipe = server.recipeManager.getRecipeFor(RecipeType.CRAFTING, craftingContainer, level)
      if (recipe.isEmpty) {
        return DEFAULT_CAN_RESPONSE
      }

      return canCraft(player, recipe.get(), player?.onPos)
    }

    fun canCraftWith(player: Player?, item: Item, pos: BlockPos?): Boolean {
      return INSTANCE.isConsumable(player, item, pos)
    }

    fun canHold(player: Player?, item: Item, pos: BlockPos?): Boolean {
      return INSTANCE.isHoldable(player, item, pos)
    }

    fun canIdentify(player: Player?, item: Item, pos: BlockPos?): Boolean {
      return INSTANCE.isIdentifiable(player, item, pos)
    }

    fun canAttackWith(player: Player?, item: Item, pos: BlockPos?): Boolean {
      return INSTANCE.isHarmful(player, item, pos)
    }

    fun canEquip(player: Player?, item: Item, pos: BlockPos?): Boolean {
      return INSTANCE.isWearable(player, item, pos)
    }

    fun canEquip(player: Player?, stack: ItemStack, pos: BlockPos?): Boolean {
      return INSTANCE.isWearable(player, stack, pos)
    }

    fun canUse(player: Player?, item: Item, pos: BlockPos?): Boolean {
      return INSTANCE.isUsable(player, item, pos)
    }

    private fun shouldRemoveItem(player: Player, wearable: Boolean): (ItemStack) -> Boolean {
      return {
        if (isEmptyStack(it)) {
          false
        } else {
          val item = getValue(it)
          val isHoldable = canHold(player, item, null)
          val isWearable = !wearable || canEquip(player, item, null)

          !isHoldable || !isWearable
        }
      }
    }

    fun getItemsToRemove(player: Player, inventory: NonNullList<ItemStack>): List<ItemStack> {
      return inventory.stream()
        .filter(shouldRemoveItem(player, false))
        .toList()
    }

    fun moveItemIntoInventory(player: Player, stack: ItemStack) {
      player.inventory.placeItemBackInInventory(stack)
    }

    private fun removeFromEquippedSlot(player: Player, list: NonNullList<ItemStack>, stack: ItemStack, index: Int) {
      ItemSkills.LOGGER.debug("${player.name} should not have ${getName(stack)} equipped")
      list[index] = ItemStack.EMPTY
      moveItemIntoInventory(player, stack)
    }

    fun filterFromList(player: Player, list: NonNullList<ItemStack>) {
      val predicate = shouldRemoveItem(player, true)
      for (i in list.indices) {
        val stack = list[i]
        if (predicate(stack)) {
          removeFromEquippedSlot(player, list, stack, i)
        }
      }
    }

    fun dropFromInventory(player: Player): (ItemStack) -> Unit {
      return {
        ItemSkills.LOGGER.debug("${player.name} should not be holding ${getName(it)}")
        player.inventory.removeItem(it)
        player.drop(it, true)
      }
    }
  }
}
