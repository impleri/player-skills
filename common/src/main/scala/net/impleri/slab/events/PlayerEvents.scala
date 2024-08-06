package net.impleri.slab.events

import dev.architectury.event.Event
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import net.impleri.slab.advancements.Award
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.item.Item
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.world.Level
import net.minecraft.advancements.Advancement
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.{Entity => McEntity}
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.{Player => McPlayer}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.{Level => McLevel}
import net.minecraft.world.phys.HitResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.EntityHitResult

case class PlayerEvents(
  private val onJoinEvent: Event[PlayerEvent.PlayerJoin] =
    PlayerEvent.PLAYER_JOIN,
  private val onQuitEvent: Event[PlayerEvent.PlayerQuit] =
    PlayerEvent.PLAYER_QUIT,
  private val onRespawnEvent: Event[PlayerEvent.PlayerRespawn] =
    PlayerEvent.PLAYER_RESPAWN,
  private val onCloneEvent: Event[PlayerEvent.PlayerClone] =
    PlayerEvent.PLAYER_CLONE,
  private val onAwardEvent: Event[PlayerEvent.PlayerAdvancement] =
    PlayerEvent.PLAYER_ADVANCEMENT,
  private val afterCraftEvent: Event[PlayerEvent.CraftItem] =
    PlayerEvent.CRAFT_ITEM,
  private val afterSmeltEvent: Event[PlayerEvent.SmeltItem] =
    PlayerEvent.SMELT_ITEM,
  private val canPickupEvent: Event[PlayerEvent.PickupItemPredicate] =
    PlayerEvent.PICKUP_ITEM_PRE,
  private val onPickupEvent: Event[PlayerEvent.PickupItem] =
    PlayerEvent.PICKUP_ITEM_POST,
  private val onChangeDimensionEvent: Event[PlayerEvent.ChangeDimension] =
    PlayerEvent.CHANGE_DIMENSION,
  private val onDropEvent: Event[PlayerEvent.DropItem] = PlayerEvent.DROP_ITEM,
  //  onOpenMenuEvent: Event[PlayerEvent.OpenMenu] = PlayerEvent.OPEN_MENU,
  //  onCloseMenuEvent: Event[PlayerEvent.CloseMenu] = PlayerEvent.CLOSE_MENU,
  private val onFillBucketEvent: Event[PlayerEvent.FillBucket] =
    PlayerEvent.FILL_BUCKET,
  private val onAttackEvent: Event[PlayerEvent.AttackEntity] =
    PlayerEvent.ATTACK_ENTITY,
) {
  private def withServerPlayer(p: ServerPlayer): Option[Player.Server] =
    Option(p).map(Player(_))

  private def withPlayer(p: McPlayer): Option[Player.Any] =
    Option(p).map(Player(_))

  def onJoin(handler: PlayerEvents.OnJoinOrQuit): Unit = {
    onJoinEvent.register((player: ServerPlayer) =>
      withServerPlayer(player).foreach(handler),
    )
  }

  def onQuit(handler: PlayerEvents.OnJoinOrQuit): Unit = {
    onQuitEvent.register((player: ServerPlayer) =>
      withServerPlayer(player).foreach(handler),
    )
  }

  def onRespawn(handler: PlayerEvents.OnRespawn): Unit = {
    onRespawnEvent
      .register((player: ServerPlayer, wonGame: Boolean) =>
        withServerPlayer(player).foreach(handler(_, wonGame)),
      )
  }

  def onClone(handler: PlayerEvents.OnClone): Unit = {
    onCloneEvent.register {
      (oldPlayer: ServerPlayer, player: ServerPlayer, wonGame: Boolean) =>
        withServerPlayer(player).foreach(
          handler(_, Option(oldPlayer).map(Player(_)), wonGame),
        )
    }
  }

  def onAward(handler: PlayerEvents.OnAward): Unit = {
    onAwardEvent.register { (player: ServerPlayer, advancement: Advancement) =>
      withServerPlayer(player).foreach(
        handler(_, Option(advancement).map(Award(_))),
      )
    }
  }

  def afterCraft(handler: PlayerEvents.AfterCraft): Unit = {
    // TODO: Wrap Container and pass it into handler
    afterCraftEvent.register {
      (player: McPlayer, item: ItemStack, _: Container) =>
        withPlayer(player).foreach(handler(_, Option(item).map(Item(_))))
    }
  }

  def afterSmelt(handler: PlayerEvents.AfterSmelt): Unit = {
    afterSmeltEvent.register { (player: McPlayer, item: ItemStack) =>
      withPlayer(player).foreach(handler(_, Option(item).map(Item(_))))
    }
  }

  def canPickup(handler: PlayerEvents.CanPickup): Unit = {
    canPickupEvent.register {
      (player: McPlayer, entity: ItemEntity, item: ItemStack) =>
        withPlayer(player).fold(EventResult.pass())(
          handler(_, Option(item).map(Item(_)), Option(entity).map(Entity(_))),
        )
    }
  }

  def onPickup(handler: PlayerEvents.OnPickup): Unit = {
    onPickupEvent.register {
      (player: McPlayer, entity: ItemEntity, item: ItemStack) =>
        withPlayer(player).foreach(
          handler(_, Option(item).map(Item(_)), Option(entity).map(Entity(_))),
        )
    }
  }

  def onChangeDimension(handler: PlayerEvents.OnChangeDimension): Unit = {
    onChangeDimensionEvent
      .register {
        (
          player: ServerPlayer,
          oldLevel: ResourceKey[McLevel],
          newLevel: ResourceKey[McLevel],
        ) =>
          withPlayer(player).foreach(
            handler(
              _,
              Option(newLevel).map(_.location()).flatMap(ResourceLocation(_)),
              Option(oldLevel).map(_.location()).flatMap(ResourceLocation(_)),
            ),
          )
      }
  }

  def onDrop(handler: PlayerEvents.OnDrop): Unit = {
    onDropEvent.register { (player: McPlayer, item: ItemEntity) =>
      withPlayer(player).fold(EventResult.pass())(
        handler(_, Option(item).map(Entity(_))),
      )
    }
  }

  //  def onOpenMenu(): Unit = ???

  //  def onCloseMenu(): Unit = ???

  def onFillBucket(handler: PlayerEvents.OnFillBucket): Unit = {
    // TODO: Incorporate HitResult into callback
    onFillBucketEvent.register {
      (player: McPlayer, level: McLevel, item: ItemStack, _: HitResult) =>
        withPlayer(player).fold(CompoundEventResult.pass[ItemStack]())(
          handler(
            _,
            Option(level).map(Level(_)),
            Option(item).map(Item(_)),
          ),
        )
    }
  }

  def onAttack(handler: PlayerEvents.OnAttack): Unit = {
    // TODO: Incorporate EntityHitResult into callback
    onAttackEvent
      .register {
        (
          player: McPlayer,
          level: McLevel,
          entity: McEntity,
          hand: InteractionHand,
          _: EntityHitResult,
        ) =>
          withPlayer(player).fold(EventResult.pass())(
            handler(
              _,
              Option(entity).map(Entity(_)),
              Option(level).map(Level(_)),
              Hand.fromVanilla(hand),
            ),
          )
      }
  }
}

object PlayerEvents {
  type OnJoinOrQuit = Player.Any => Unit
  type OnRespawn = (Player.Any, Boolean) => Unit
  type OnClone = (Player.Any, Option[Player.Any], Boolean) => Unit
  type OnAward = (Player.Any, Option[Award]) => Unit
  type AfterCraft = (Player.Any, Option[Item]) => Unit
  type AfterSmelt = (Player.Any, Option[Item]) => Unit
  type CanPickup = (Player.Any, Option[Item], Option[Entity.Any]) => EventResult
  type OnPickup = (Player.Any, Option[Item], Option[Entity.Any]) => Unit
  type OnChangeDimension =
    (Player.Any, Option[ResourceLocation], Option[ResourceLocation]) => Unit
  type OnDrop = (Player.Any, Option[Entity.Any]) => EventResult
  type OnFillBucket = (
    Player.Any,
    Option[Level.Any],
    Option[Item],
  ) => CompoundEventResult[ItemStack]
  type OnAttack = (
    Player.Any,
    Option[Entity.Any],
    Option[Level.Any],
    Hand.Hand,
  ) => EventResult
}
