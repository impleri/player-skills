package net.impleri.playerskills.server.bindings.player

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.entity.Player
import net.impleri.slab.events.TickEvents
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger

class OnPlayerTickSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockUpstream = mock[TickEvents]
  private val mockLogger = mock[Logger]

  private val testUnit = OnPlayerTick(mockOps, mockUpstream, mockLogger)

  private val mockPlayer = mock[Player]

  private val indexRestricted = 2
  private val restrictedItem = mock[Item]
  private val indexUnrestricted = 1
  private val unrestrictedItem = mock[Item]
  private val indexOther = 3
  private val otherItem = mock[Item]

  mockPlayer.isClient returns false

  "OnPlayerTick.handler" should "do nothing clientside" in {
    mockPlayer.isClient returns true
    mockPlayer.armor returns Map.empty

    testUnit.handler(mockPlayer)

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
    testUnit.handler(mockPlayer)

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

    testUnit.handler(mockPlayer)

    mockPlayer.putInInventory(restrictedItem) wasCalled once
    mockPlayer.emptyOffHand(indexRestricted) wasCalled once
    mockPlayer.toss(restrictedItem) wasCalled once
  }
}
