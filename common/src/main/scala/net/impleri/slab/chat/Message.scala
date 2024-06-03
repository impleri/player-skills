package net.impleri.slab.chat

import net.impleri.slab.commands.Command
import net.minecraft.network.chat.MutableComponent
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

trait Message[T <: Message[_]] {
  protected def underlying: Message.Vanilla

  def output: Message.VanillaBase = underlying

  def mutableOutput: Message.Vanilla = underlying

  def asString: String = underlying.toString

  def copyAs(newVal: Message.Vanilla): T

  def sendSuccess(context: Command.Context): Unit = {
    context.getSource.sendSuccess(output, false)
  }

  def sendSuccessWithAdmins(context: Command.Context): Unit = {
    context.getSource.sendSuccess(output, true)
  }

  def sendGeneric(context: Command.Context): Unit = {
    context.getSource.sendSystemMessage(output)
  }

  def sendFailure(context: Command.Context): Unit = {
    context.getSource.sendFailure(output)
  }

  def append(value: Message.Any): T = {
    copyAs(underlying.append(value.underlying))
  }

  def append(value: String): T = {
    copyAs(underlying.append(value))
  }

  def black(): T = {
    copyAs(underlying.withStyle(ChatFormatting.BLACK))
  }

  def darkBlue(): T = {
    copyAs(underlying.withStyle(ChatFormatting.DARK_BLUE))
  }

  def darkGreen(): T = {
    copyAs(underlying.withStyle(ChatFormatting.DARK_GREEN))
  }

  def darkAqua(): T = {
    copyAs(underlying.withStyle(ChatFormatting.DARK_AQUA))
  }

  def darkRed(): T = {
    copyAs(underlying.withStyle(ChatFormatting.DARK_RED))
  }

  def darkPurple(): T = {
    copyAs(underlying.withStyle(ChatFormatting.DARK_PURPLE))
  }

  def gold(): T = {
    copyAs(underlying.withStyle(ChatFormatting.GOLD))
  }

  def gray(): T = {
    copyAs(underlying.withStyle(ChatFormatting.GRAY))
  }

  def darkGray(): T = {
    copyAs(underlying.withStyle(ChatFormatting.DARK_GRAY))
  }

  def blue(): T = {
    copyAs(underlying.withStyle(ChatFormatting.BLUE))
  }

  def green(): T = {
    copyAs(underlying.withStyle(ChatFormatting.GREEN))
  }

  def aqua(): T = {
    copyAs(underlying.withStyle(ChatFormatting.AQUA))
  }

  def red(): T = {
    copyAs(underlying.withStyle(ChatFormatting.RED))
  }

  def lightPurple(): T = {
    copyAs(underlying.withStyle(ChatFormatting.LIGHT_PURPLE))
  }

  def yellow(): T = {
    copyAs(underlying.withStyle(ChatFormatting.YELLOW))
  }

  def white(): T = {
    copyAs(underlying.withStyle(ChatFormatting.WHITE))
  }

  def obfuscated(): T = {
    copyAs(underlying.withStyle(ChatFormatting.OBFUSCATED))
  }

  def bold(): T = {
    copyAs(underlying.withStyle(ChatFormatting.BOLD))
  }

  def strikethrough(): T = {
    copyAs(underlying.withStyle(ChatFormatting.STRIKETHROUGH))
  }

  def underline(): T = {
    copyAs(underlying.withStyle(ChatFormatting.UNDERLINE))
  }

  def italic(): T = {
    copyAs(underlying.withStyle(ChatFormatting.ITALIC))
  }

  def reset(): T = {
    copyAs(underlying.withStyle(ChatFormatting.RESET))
  }
}

object Message {
  type Any = Message[_]

  type Vanilla = MutableComponent

  type VanillaBase = Component
}
