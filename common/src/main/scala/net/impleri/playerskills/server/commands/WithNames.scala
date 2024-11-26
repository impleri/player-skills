package net.impleri.playerskills.server.commands

import net.impleri.slab.chat.StaticText
import net.impleri.slab.resources.Named

trait WithNames {
  protected def renderNames(as: Seq[Named]): Seq[StaticText]  =
    as
      .map(a => a.name)
      .map(_.asString)
      .map(StaticText(_))
}
