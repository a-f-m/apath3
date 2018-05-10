package org.afm.apath3.accessors

import java.io.InputStream
import java.util
import java.util.function.Consumer

import org.afm.apath3.core._
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


class JsoupAcc extends Accessor {

  setIsPropertyMapFunc(obj => obj.isInstanceOf[Element])

  setSelectorFunc((node, e) => {
    val o = node.obj
    e match {
      case Property(name, isAttribute, _) => o match {
        case elm: Element => nameRes(elm, name, isAttribute)
        case _ => throw new RuntimeException("fatal should not happen")
      }
      case ArraySubscript(_) => //
        throw new RuntimeException(s"no array subscript allowed, use sequence subscript (object: '${
          PrintUtil.abbr(node)
        }')")
      case Children() => o match {
        case elm: Element => iter(elm.children().iterator())
        case _ => throw new RuntimeException("fatal should not happen")
      }
      case Text() => o match {
        case elm: Element => iterO(elm.text(), "")
        case s: String => iterO(s, "")
        case _ => throw new RuntimeException("fatal should not happen")
      }
      case _ => new DelegatedIter()
    }
  })

  private def nameRes(elm: Element, name: String, isAttribute: Boolean): NodeIter = {

    // unfortunately jsoup parses tag names to lowercase
    if (isAttribute) {
      val a = elm.attr(name)
      if (a == "") NilIter() else iterO(a, name.toLowerCase)
    } else {
      val children = new util.ArrayList[Element]()
      elm.children().forEach(new Consumer[Element] {
        override def accept(x: Element): Unit = if (x.tagName() == name.toLowerCase) children.add(x)
      })

      if (children.isEmpty) NilIter() else iter(children.iterator())
    }
  }

  override def parse(expr: String): Any = Jsoup.parse(expr)

  override def parse(in: InputStream): Any = Jsoup.parse(in, "utf-8", "")
}
