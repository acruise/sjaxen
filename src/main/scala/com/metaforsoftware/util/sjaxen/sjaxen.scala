package com.metaforsoftware.util.sjaxen

import java.util.{Iterator=>JIterator, Collection=>JCollection, List=>JList}

import collection.JavaConverters._

import org.jaxen._

/**
 * ProductNavigator implements the singleton navigator object for
 * Jaxen-xpath navigation over Scala objects. Most methods here exist
 * to be called by Jaxen. You can call parseXPath to create an evaluator
 * for an xpath expression against Scala objects.
 * @author Ross Judson
 */
object ProductNavigator extends ScalaNavigator {
  /**
   * Create an xpath evaluator for an expression. This compiles the expression
   * into a form that can be executed against Scala objects.
   */
  def parseXPath(xpath: String) = new ProductXPath(xpath)
  def getTextStringValue(obj: Object) = obj match {
    case pe: ProductElement => pe.toString
    case _ => obj.toString
  }
  def isElement(obj: Object) = obj.isInstanceOf[ProductElement]
  def getElementName(obj: Object) = obj.asInstanceOf[ProductElement].name
  override def getParentAxisIterator(obj: Object) = obj match {
    case pe: ProductElement => singletonIterator(pe.parent)
    case _ => Iterator[AnyRef]().asJava
  }
  override def getChildAxisIterator(obj: Object): JIterator[_ <: AnyRef] = obj match {
    case xpc: XPathChildren => xpc.xpathChildren.iterator.asJava
    case _ => Iterator[AnyRef]().asJava
  }
  def getChildAxisIterator(contextNode: Object, localName: String, prefix: String, namespace: String): JIterator[_] = {
    val context = contextNode.asInstanceOf[ProductElement]
		val targ = context.product
    targ match {
      case xa: XPathAxes => new ProductElementIter(context, localName, xa.xpathAxis(localName).iterator.asJava)
      case _ =>
        targ.getClass getMethod(localName) invoke(targ) match {
          case null => Iterator[AnyRef]().asJava
          case itr: Iterable[Object] => new ProductElementIter(context, localName, itr.iterator.asJava)
          case coll: JCollection[AnyRef] => new ProductElementIter(context, localName, coll.iterator)
          case arr: Object if arr.getClass.isArray => Iterator[AnyRef]().asJava
          case o: Object => singletonIterator(ProductElement(context, localName, o))
        }
    }
  }

  // This differs from getChildAxisIterator in that it's retrieving
  // an attribute and returns the value in the field, rather than a
  // list of field values that can be iterated through.
  def getAttributeAxisIterator(contextNode: Object, localName: String, prefix: String, namespace: String): JIterator[_] = {
    val context = contextNode.asInstanceOf[ProductElement]
    val targ = context.product
    targ match {
      case xa: XPathAxes => new ProductElementIter(context, localName, xa.xpathAxis(localName).iterator.asJava)
      case _ =>
        targ.getClass getMethod (localName) invoke (targ) match {
          case null => Iterator[AnyRef]().asJava
          case itr: Iterable[_] => itr.iterator.asJava
          case coll: JCollection[_] => coll.iterator
          case arr: Object if arr.getClass.isArray => Iterator[AnyRef]().asJava
          case o: Object => singletonIterator(o)
        }
    }
  }

  private def singletonIterator(obj: Object) = Iterator(obj).asJava
  private def ProductElement(parent: ProductElement, name: String, product: Object) =
    new ProductElement(name, product).setParent(parent)

  private class ProductElementIter(context: ProductElement, name: String, iter: JIterator[_ <: AnyRef]) extends JIterator[ProductElement] {
		def remove() {
      throw new UnsupportedOperationException
    }
    def hasNext = iter.hasNext
    def next = ProductElement(context, name, iter.next)
  }
}

private [sjaxen] case class ProductElement(name: String, product: Object) {
  var parent: ProductElement = _
  def setParent(p: ProductElement) = { parent = p; this }
}

class ProductXPath(xpath: String) extends BaseXPath(xpath, ProductNavigator) {
  override protected def getContext(node: Object) = node match {
    case c: Context => c
    case pe: ProductElement => super.getContext(node)
    case lst: JList[AnyRef] =>
      super.getContext(lstMap(lst, o => ProductElement("root", o)))
    case _ => super.getContext(ProductElement("root", node))
  }

  override def evaluate(node: Object) = super.evaluate(node) match {
    case ProductElement(_, obj) => obj
    case coll: JCollection[AnyRef] => lstMap(coll, {
      case ProductElement(_, obj) => obj
      case v: Object => v
    })
    case x => x
  }

  private def lstMap(lst: JCollection[_ <: AnyRef], f: Object => Object) = {
    val newList = new java.util.ArrayList[AnyRef](lst.size)
      val itr = lst.iterator()
      while (itr.hasNext) {
        newList.add(f(itr.next()))
      }
    newList
  }
}

/** Provides defaults for many of the Jaxen methods, so implementation of
concrete classes is easier. */
abstract class ScalaNavigator extends DefaultNavigator with NamedAccessNavigator {
  def getNamespacePrefix(obj: Object) = null
  def getCommentStringValue(obj: Object) = null
  def getNamespaceStringValue(obj: Object) = obj.toString
  def getAttributeStringValue(obj: Object) = obj.toString
  def getElementStringValue(obj: Object) = getTextStringValue(obj)
  def isProcessingInstruction(obj: Object) = false
  def isText(obj: Object) = obj.isInstanceOf[String]
  def isComment(obj: Object) = false
  def isDocument(obj: Object) = false
  def isNamespace(obj: Object) = false
  def isAttribute(obj: Object) = false
  def getAttributeQName(obj: Object) = ""
  def getAttributeName(obj: Object) = ""
  def getAttributeNamespaceUri(obj: Object) = ""
  def getElementQName(obj: Object) = ""
  def getElementNamespaceUri(obj: Object) = ""
}

/** To take direct control over axes, extend your class
with this trait, and you'll be able to return whatever you'd
like. */
trait XPathAxes {
  def xpathAxis(name: String): Iterable[AnyRef]
}
/** If there's a natural "child" axis for your object,
you can extend with this trait and get some default
behavior. */
trait XPathChildren extends XPathAxes {
  def xpathChildren: Iterable[_ <: AnyRef]
  def xpathAxis(name: String) = name match {
    case "children" => xpathChildren
    case _ => Nil
  }
}

