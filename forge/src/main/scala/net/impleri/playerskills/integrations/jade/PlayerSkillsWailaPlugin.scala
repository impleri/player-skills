package net.impleri.playerskills.integrations.jade

import net.minecraft.world.entity.item.ItemEntity
import snownee.jade.api.{EntityAccessor, IWailaClientRegistration, IWailaPlugin}

class PlayerSkillsWailaPlugin extends IWailaPlugin {
  override def registerClient(registration: IWailaClientRegistration): Unit =
    registration.addRayTraceCallback { (_, accessor, _) =>
      accessor match {
        case e: EntityAccessor => handleEntity(e, registration)
        case _ => accessor
      }
    }

  private def handleEntity(accessor: EntityAccessor, registration: IWailaClientRegistration): EntityAccessor = {
    accessor.getEntity match {
      case i: ItemEntity => registration.entityAccessor().from(accessor).build()
      case _ => accessor
    }
  }
}
