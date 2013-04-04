package com.metaforsoftware.util.sjaxen

import org.junit.Test

class BasicTest {
  @Test
  def testStuff() {
    val b = Both("Ross", new Zinger(List("Hello", "World")), new Zinger(List("Bottom", "Ladder")))
    val xp = ProductNavigator.parseXPath("left/lst|left/../right[@name='Ross']/lst")
    println(xp evaluate b)
  }

  class Zinger(val lst: List[String]) {
    val name = "Ross"
  }
  case class Both(name: String, left: Zinger, right: Zinger)

}
