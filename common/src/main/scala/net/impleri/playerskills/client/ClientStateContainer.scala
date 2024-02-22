package net.impleri.playerskills.client

import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.client.restrictions.ItemRestrictionOpsClient
import net.impleri.playerskills.client.restrictions.RecipeRestrictionOpsClient
import net.impleri.playerskills.network.Manager
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class ClientStateContainer(
  globalState: StateContainer = StateContainer(),
  eventHandler: EventHandler = EventHandler(),
  client: Client = Client(),
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  val SKILLS: ClientSkillsRegistry = ClientSkillsRegistry(eventHandler)

  lazy val ITEM_RESTRICTIONS: ItemRestrictionOpsClient = ItemRestrictionOpsClient(globalState.RESTRICTIONS, client)

  lazy val RECIPE_RESTRICTIONS: RecipeRestrictionOpsClient = RecipeRestrictionOpsClient(globalState.RESTRICTIONS)

  lazy private val MANAGER = Manager(globalState, clientStateContainer = Option(this))

  def getNetHandler: NetHandler = NetHandler(client, SKILLS, MANAGER.RESYNC_SKILLS)
}
