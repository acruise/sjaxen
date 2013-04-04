sjaxen
======

Use Jaxen to navigate Scala object trees

This originally came from the Scala Ur-Wiki at sygneca.com; I got it from the new Scala wiki.

Unfortunately no license was attached.  I've asked @rossjudson to clarify. :)

This is what it lets you do:

    class Zinger(val lst: List[String]) {
      val name = "Ross"
    }
    case class Both(name: String, left: Zinger, right: Zinger)

    val b = Both("Ross", new Zinger(List("Hello", "World")), new Zinger(List("Bottom", "Ladder")))
    val xp = ProductNavigator.parseXPath("left/lst|left/../right[@name='Ross']/lst")
    println(xp evaluate b)

    >> [Hello, World, Bottom, Ladder]
