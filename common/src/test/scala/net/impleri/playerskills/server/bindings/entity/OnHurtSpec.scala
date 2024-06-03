package net.impleri.playerskills.server.bindings.entity

import dev.architectury.event.EventResult
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.HasSource
import net.impleri.slab.entity.Player
import net.impleri.slab.events.EntityEvents
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger
import net.minecraft.world.entity.player.{Player => MinecraftPlayer}

class OnHurtSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockUpstream = mock[EntityEvents]
  private val mockLogger = mock[Logger]

  private val testUnit = OnHurt(mockOps, mockUpstream, mockLogger, mockLogger)

  private val mockEntity = mock[Entity[_]]
  private val mockAttacker = mock[Entity[_]]
  private val mockPlayer = mock[Player[MinecraftPlayer]]
  mockAttacker.asPlayer[MinecraftPlayer] returns mockPlayer
  private val mockItem = mock[Item]
  mockPlayer.getItemInMainHand returns Option(mockItem)
  private val mockDamange = 13F

  "OnHurt.handler" should "interrupt the event if the player's item is unusable" in {
    val mockSource = mock[HasSource]
    mockSource.isPlayer returns true
    mockSource.source returns Option(mockAttacker)
    mockAttacker.isPlayer returns true

    mockOps.isHarmful(mockPlayer, mockItem) returns false

    val result = testUnit.handler(mockEntity, Option(mockSource), mockDamange)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "do nothing to the event if item is usable" in {
    val mockSource = mock[HasSource]
    mockSource.isPlayer returns true
    mockSource.source returns Option(mockAttacker)
    mockAttacker.isPlayer returns true

    mockOps.isHarmful(mockPlayer, mockItem) returns true

    testUnit.handler(mockEntity, Option(mockSource), mockDamange) shouldBe EventResult.pass()
  }

  it should "do nothing to the event if attacker is not a player" in {
    val mockSource = mock[HasSource]
    mockSource.isPlayer returns false

    testUnit.handler(mockEntity, Option(mockSource), mockDamange) shouldBe EventResult.pass()

    mockOps.isHarmful(mockPlayer, mockItem) wasNever called

  }
}
