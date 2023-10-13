package net.impleri.playerskills

import org.apache.logging.log4j.LogManager
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito
import org.scalatest.BeforeAndAfterAll
import org.scalatest.EitherValues
import org.scalatest.OneInstancePerTest
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

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
  }
}
