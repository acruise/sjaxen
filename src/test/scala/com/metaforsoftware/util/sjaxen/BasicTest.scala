package com.metaforsoftware.util.sjaxen

import org.junit.{ Test, Assert }
import collection.JavaConverters._

class BasicTest {
  @Test
  def testStuff() {
    val b = Both("Ross", new Zinger(List("Hello", "World")), new Zinger(List("Bottom", "Ladder")))
    val xp = ProductNavigator.parseXPath("left/lst|left/../right[@name='Ross']/lst")
    val result = (xp evaluate b).asInstanceOf[java.util.ArrayList[_]]
    Assert.assertEquals(List(ProductElement("lst","Hello"), ProductElement("lst","World"), ProductElement("lst","Bottom"), ProductElement("lst","Ladder"), "Hello", "World", "Bottom", "Ladder"), result.asScala)
  }

  class Zinger(val lst: List[String]) {
    val name = "Ross"
  }
  case class Both(name: String, left: Zinger, right: Zinger)

}
