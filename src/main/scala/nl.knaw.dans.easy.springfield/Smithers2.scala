/**
 * Copyright (C) 2017 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.springfield

import java.io.ByteArrayInputStream
import java.net.URI
import java.nio.file.{ Path, Paths }

import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.util.{ Failure, Success, Try }
import scala.xml.{ Elem, XML }
import scalaj.http.Http

trait Smithers2 {
  this: DebugEnhancedLogging =>
  val smithers2BaseUri: URI
  val smithers2ConnectionTimeoutMs: Int
  val smithers2ReadTimoutMs: Int

  def getXmlFromPath(path: Path): Try[Elem] = {
    trace(path)
    val uri = path2Uri(path)
    debug(s"Smithers2 URI: $uri")
    for {
      response <- http("GET", uri)
      if response.code == 200
      xml <- isResponseOk(response.body)
    } yield xml
  }

  /**
   * Attempts to delete the item at `path`
   *
   * @param path the Springfield path of the item to delete
   * @return Success if the item was deleted, Failure with an error message otherwise
   */
  def deletePath(path: Path): Try[Unit] = {
    trace(path)
    val uri = path2Uri(path)
    debug(s"Smithers2 URI: $uri")
    for {
      response <- http("DELETE", uri)
      if response.code == 200
      _ <- isResponseOk(response.body)
    } yield ()
  }

  def getReferencedPaths(path: Path): Try[Seq[Path]] = {
    getXmlFromPath(path)
      .map(_ \\ "@referid")
      .map {
        _.map {
          n =>
            if (n.text.startsWith("/")) n.text.substring(1)
            else n.text
        }
          .map(Paths.get(_))
      }
      .map {
        paths =>
          paths.map(getReferencedPaths).collectResults.map {
            s =>
              if (s.isEmpty) Seq()
              else s.reduce(_ ++ _)
          }
            .map(_ ++ paths)
      }.flatten
  }

  private def path2Uri(path: Path): URI = {
    new URI(smithers2BaseUri.getScheme,
      smithers2BaseUri.getUserInfo,
      smithers2BaseUri.getHost,
      smithers2BaseUri.getPort,
      Paths.get(smithers2BaseUri.getPath).resolve(path).toString, null, null)
  }

  private def http(method: String, uri: URI) = Try {
    Http(uri.toASCIIString)
      .method(method)
      .timeout(connTimeoutMs = smithers2ConnectionTimeoutMs, readTimeoutMs = smithers2ReadTimoutMs)
      .asBytes
  }

  def isResponseOk(content: Array[Byte]): Try[Elem] = {
    /*
     * Never mind about the status codes. Springfield only returns 200 :-/
     */
    val xml = XML.load(new ByteArrayInputStream(content))
    xml.label match {
      case "error" =>
        val errorCode = xml.attribute("id").flatMap(_.headOption).map(_.text).getOrElse("<no error code>")
        val message = (xml \ "properties" \ "message").headOption.map(_.text).getOrElse("<no message>")
        val details = (xml \ "properties" \ "details").headOption.map(_.text).getOrElse("<no details>")
        Failure(new Exception(s"($errorCode) $message: $details"))
      case _ => Success(xml)
    }
  }
}
