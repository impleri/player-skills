package net.impleri.playerskills

import org.apache.logging.log4j.LogManager
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BaseSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    System.setProperty("log4j.configurationFile","test-logging.xml")
    LogManager.getLogger()
  }
}
