package net.impleri.playerskills.client.api

import net.impleri.playerskills.api.FluidRestrictions
import net.impleri.playerskills.client.RestrictionsClient
import net.impleri.playerskills.restrictions.Registry
import net.impleri.playerskills.restrictions.fluids.FluidRestriction
import net.minecraft.world.level.material.Fluid

class FluidRestrictionClient private constructor(registry: Registry<FluidRestriction>, serverApi: FluidRestrictions) :
  RestrictionsClient<Fluid, FluidRestriction, FluidRestrictions>(registry, serverApi) {
  private fun pluckTarget(list: List<FluidRestriction>): List<Fluid> {
    return list.map { it.target }.toList()
  }

  val hidden: List<Fluid>
    get() = pluckTarget(getFiltered { !it.producible && !it.consumable })

  val unproducible: List<Fluid>
    get() = pluckTarget(getFiltered { !it.producible })

  val unconsumable: List<Fluid>
    get() = pluckTarget(getFiltered { !it.consumable })

  fun isProducible(fluid: Fluid?): Boolean {
    return serverApi.isProducible(player, fluid!!)
  }

  fun isConsumable(fluid: Fluid?): Boolean {
    return serverApi.isConsumable(player, fluid!!)
  }

  fun isIdentifiable(fluid: Fluid?): Boolean {
    return serverApi.isIdentifiable(player, fluid!!)
  }

  companion object {
    val INSTANCE = FluidRestrictionClient(FluidRestrictions.RestrictionRegistry, FluidRestrictions.INSTANCE)
  }
}
