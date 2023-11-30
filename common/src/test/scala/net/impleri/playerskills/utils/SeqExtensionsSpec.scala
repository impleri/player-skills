package net.impleri.playerskills.utils

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.utils.SeqExtensions.EnhancedDiff

import scala.util.Try

class SeqExtensionsSpec extends BaseSpec {
  "SeqExtensions.containsList" should "return true if A has all elements in B" in {
    val as = Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val bs = Seq(7, 3, 4)

    as.containsList(bs) should be(true)
  }

  it should "return false if A is missing any element from B" in {
    val as = Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val bs = Seq(7, 11, 3, 4)

    as.containsList(bs) should be(false)
  }

  "SeqExtensions.containsListWith" should "return true if A has all elements in B" in {
    val as = Seq(1.4, 2.6, 4.55, 6.9, 7.7, 10.74)
    val bs = Seq(7.9, 3.2, 4.8)

    as.containsListWith(bs, v => Try(v.round).toOption) should be(true)
  }

  it should "return false if A is missing any element from B" in {
    val as = Seq(1.4, 2.6, 4.55, 6.9, 7.7, 10.74)
    val bs = Seq(16.3, 7.9, 3.2, 4.8)

    as.containsListWith(bs, v => Try(v.round).toOption) should be(false)
  }

  "SeqExtensions.diffWith" should "return elements in A that are not in B" in {
    val as = Seq(1.4, 2.6, 4.55, 6.9, 7.7, 10.74)
    val bs = Seq(7.9, 3.2, 4.8)

    val expected = Seq(1.4, 6.9, 10.74)

    as.diffWith(bs, v => Try(v.round).toOption) should be(expected)
  }
}
