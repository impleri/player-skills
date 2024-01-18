package net.impleri.playerskills

import net.minecraft.SharedConstants
import net.minecraft.WorldVersion
import net.minecraft.server.Bootstrap
import net.minecraft.world.level.storage.DataVersion
import org.apache.logging.log4j.LogManager
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito
import org.scalatest.BeforeAndAfterAll
import org.scalatest.EitherValues
import org.scalatest.OneInstancePerTest
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.Date
import java.util.UUID

class BaseSpec extends AnyFlatSpec
  with Matchers
  with BeforeAndAfterAll
  with OneInstancePerTest
  with ArgumentMatchersSugar
  with IdiomaticMockito
  with OptionValues
  with EitherValues {
  override def beforeAll(): Unit = {
    System.setProperty("log4j.configurationFile", "test-logging.xml")
    LogManager.getLogger()
    BaseSpec.setup()
  }
}

object BaseSpec {
  private case class TEST_GAME_VERSION(private val version: String = "1.19.2") extends WorldVersion {
    override def getDataVersion: DataVersion = new DataVersion(3120, "main")

    override def getId: String = UUID.randomUUID().toString.replaceAll("-", "")

    override def getName: String = version

    override def getReleaseTarget: String = version

    override def getProtocolVersion: Int = SharedConstants.getProtocolVersion

    override def getBuildTime: Date = new Date()

    override def isStable: Boolean = true
  }

  def setup(): Unit = {}

  protected def bootstrapMinecraft(): Unit = {
    SharedConstants.setVersion(TEST_GAME_VERSION())
    Bootstrap.bootStrap()
  }

  bootstrapMinecraft()
}
