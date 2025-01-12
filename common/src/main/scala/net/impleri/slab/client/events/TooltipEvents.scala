package net.impleri.slab.client.events

import dev.architectury.event.Event
import dev.architectury.event.events.client.ClientTooltipEvent
import net.impleri.slab.chat.{Message, StaticText}
import net.impleri.slab.item.Item
import net.minecraft.network.chat.Component
import net.minecraft.world.item.{ItemStack, TooltipFlag}

import scala.jdk.CollectionConverters._
import java.util

case class TooltipEvents(
    private val onItem: Event[ClientTooltipEvent.Item] = ClientTooltipEvent.ITEM,
    private val onBeforeRender: Event[ClientTooltipEvent.Render] = ClientTooltipEvent.RENDER_PRE,
    private val onPosition: Event[ClientTooltipEvent.RenderModifyPosition] = ClientTooltipEvent.RENDER_MODIFY_POSITION,
    private val onColore: Event[ClientTooltipEvent.RenderModifyColor] = ClientTooltipEvent.RENDER_MODIFY_COLOR,
) {
  def onRenderItem(f: TooltipEvents.ON_RENDER): Unit =
    onItem.register { (rawItem: ItemStack, components: util.List[Component], tooltipFlag: TooltipFlag) =>
      for {
        item <- Option(rawItem).map(Item(_))
        messages = components.asScala.toSeq.map(m => StaticText(m.asInstanceOf[Message.Vanilla]))
        replacements <- f(item, messages, tooltipFlag.isAdvanced)
      } yield {
        components.clear()
        components.addAll(replacements.map(_.output).asJava)
      }
    }

//  def beforeRender(f: TooltipEvents.ON_RENDER): Unit =
//    onBeforeRender.register { (rawItem: ItemStack, components: util.List[Component], tooltipFlag: TooltipFlag) =>
//      val item = Option(rawItem).map(Item(_))
//      val messages = components.asScala.toSeq.map(m => StaticText(m.asInstanceOf[Message.Vanilla]))
//
//      f(item, messages, tooltipFlag.isAdvanced)
//    }
}

object TooltipEvents {
  type ON_RENDER = (Item, Seq[Message[_]], Boolean) => Option[Seq[Message[_]]]
}
