package net.impleri.playerskills.server.bindings

import dev.architectury.event.events.common.TickEvent
import dev.architectury.event.Event
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger

class TickEventsSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockEvent = mock[Event[TickEvent.Player]]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = TickEvents(mockOps, mockEvent, mockLogger)

  private val mockPlayer = mock[Player[_]]

  private val indexRestricted = 2
  private val restrictedItem = mock[Item]
  private val indexUnrestricted = 1
  private val unrestrictedItem = mock[Item]
  private val indexOther = 3
  private val otherItem = mock[Item]

  mockPlayer.isClientSide returns false

  "TickEvents.registerEvents" should "register event handlers" in {
    testUnit.registerEvents()

    mockEvent.register(*) wasCalled once
  }

  "TickEvents.onPlayerTick" should "do nothing clientside" in {
    mockPlayer.isClientSide returns true
    mockPlayer.armor returns Map.empty

    testUnit.onPlayerTick(mockPlayer)

    mockOps.isWearable(mockPlayer, *) wasNever called
    mockOps.isHoldable(mockPlayer, *) wasNever called
  }

  it should "filter unwearable armor" in {
    mockOps.isWearable(mockPlayer, restrictedItem) returns false
    mockOps.isWearable(mockPlayer, unrestrictedItem) returns true
    mockOps.isWearable(mockPlayer, otherItem) returns true
    mockOps.isHoldable(mockPlayer, *) returns true

    mockPlayer.armor returns Map(indexRestricted -> restrictedItem, indexUnrestricted -> unrestrictedItem)
    mockPlayer.offHand returns Map(indexOther -> otherItem)
    mockPlayer.inventory returns Map(indexRestricted -> restrictedItem,
      indexUnrestricted -> unrestrictedItem,
      indexOther -> otherItem,
    )
    testUnit.onPlayerTick(mockPlayer)

    mockPlayer.putInInventory(restrictedItem) wasCalled once
    mockPlayer.emptyArmor(indexRestricted) wasCalled once

    mockPlayer.toss(*) wasNever called
  }

  it should "filter unholdable offhand item" in {
    mockOps.isWearable(mockPlayer, *) returns true

    mockOps.isHoldable(mockPlayer, restrictedItem) returns false
    mockOps.isHoldable(mockPlayer, unrestrictedItem) returns true
    mockOps.isHoldable(mockPlayer, otherItem) returns true

    mockPlayer.armor returns Map(indexUnrestricted -> unrestrictedItem)
    mockPlayer.offHand returns Map(indexRestricted -> restrictedItem)
    mockPlayer.inventory returns Map(indexRestricted -> restrictedItem,
      indexUnrestricted -> unrestrictedItem,
      indexOther -> otherItem,
    )

    testUnit.onPlayerTick(mockPlayer)

    mockPlayer.putInInventory(restrictedItem) wasCalled once
    mockPlayer.emptyOffHand(indexRestricted) wasCalled once
    mockPlayer.toss(restrictedItem) wasCalled once
  }
}
