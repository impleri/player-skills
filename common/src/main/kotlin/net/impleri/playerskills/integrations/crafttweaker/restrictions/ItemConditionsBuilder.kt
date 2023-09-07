package net.impleri.playerskills.integrations.crafttweaker.restrictions

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.restrictions.items.ItemConditions
import net.impleri.playerskills.restrictions.items.ItemRestriction
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import org.openzen.zencode.java.ZenCodeType
import java.util.function.Predicate

@ZenRegister
@ZenCodeType.Name("mods.playerskills.restrictions.ItemConditionsBuilder")
class ItemConditionsBuilder(
  private val onSave: (ItemConditionsBuilder) -> Unit,
) : AbstractRestrictionConditionsBuilder<Item, ItemRestriction>(), ItemConditions<Player> {
  override var replacement: Item? = null
  override var producible: Boolean? = null
  override var consumable: Boolean? = null
  override var identifiable: Boolean? = null
  override var holdable: Boolean? = null
  override var wearable: Boolean? = null
  override var usable: Boolean? = true
  override var harmful: Boolean? = null

  @ZenCodeType.Method
  fun save() {
    onSave(this)
  }

  @ZenCodeType.Method
  override fun condition(predicate: Predicate<Player>): ItemConditionsBuilder {
    super<ItemConditions>.condition(predicate)

    return this
  }

  @ZenCodeType.Method
  override fun unless(predicate: Predicate<Player>): ItemConditionsBuilder {
    super<ItemConditions>.unless(predicate)

    return this
  }

  @ZenCodeType.Method
  override fun producible(): ItemConditionsBuilder {
    super.producible()

    return this
  }

  @ZenCodeType.Method
  override fun unproducible(): ItemConditionsBuilder {
    super.unproducible()

    return this
  }

  @ZenCodeType.Method
  override fun consumable(): ItemConditionsBuilder {
    super.consumable()

    return this
  }

  @ZenCodeType.Method
  override fun unconsumable(): ItemConditionsBuilder {
    super.unconsumable()

    return this
  }

  @ZenCodeType.Method
  override fun identifiable(): ItemConditionsBuilder {
    super.identifiable()

    return this
  }

  @ZenCodeType.Method
  override fun unidentifiable(): ItemConditionsBuilder {
    super.unidentifiable()

    return this
  }

  @ZenCodeType.Method
  override fun holdable(): ItemConditionsBuilder {
    super.holdable()

    return this
  }

  @ZenCodeType.Method
  override fun unholdable(): ItemConditionsBuilder {
    super.unholdable()

    return this
  }

  @ZenCodeType.Method
  override fun wearable(): ItemConditionsBuilder {
    super.wearable()

    return this
  }

  @ZenCodeType.Method
  override fun unwearable(): ItemConditionsBuilder {
    super.unwearable()

    return this
  }

  @ZenCodeType.Method
  override fun usable(): ItemConditionsBuilder {
    super.usable()

    return this
  }

  @ZenCodeType.Method
  override fun unusable(): ItemConditionsBuilder {
    super.unusable()

    return this
  }

  @ZenCodeType.Method
  override fun harmful(): ItemConditionsBuilder {
    super.harmful()

    return this
  }

  @ZenCodeType.Method
  override fun harmless(): ItemConditionsBuilder {
    super.harmless()

    return this
  }

  @ZenCodeType.Method
  override fun nothing(): ItemConditionsBuilder {
    super.nothing()

    return this
  }

  @ZenCodeType.Method
  override fun everything(): ItemConditionsBuilder {
    super.everything()

    return this
  }
}
