package nl.knaw.dans.easy.springfield

import scala.util.Try
import scala.xml.Elem

trait ListAction {


  def listUsers(parent: Elem): Seq[String] = {
    for {
      user <- parent \ "user"
      if user.attribute("id").isDefined
    } yield user.attribute("id").get.head.text
  }
}
