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
import java.nio.file.{Path, Paths}

import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.util.Try
import scala.xml.{Elem, XML}
import scalaj.http.Http

trait Smither2Xml {
  this: DebugEnhancedLogging =>
  val smithers2BaseUri: URI
  val smithers2ConnectionTimeoutMs: Int
  val smithers2ReadTimoutMs: Int

  def getSmithers2Xml(path: Path): Try[Elem] = Try {
    trace(path)
    val uri = new URI(smithers2BaseUri.getScheme,
      smithers2BaseUri.getUserInfo,
      smithers2BaseUri.getHost,
      smithers2BaseUri.getPort,
      Paths.get(smithers2BaseUri.getPath).resolve(path).toString, null, null)
    debug(s"Smithers2 URI: $uri")
    val response = Http(uri.toASCIIString).timeout(connTimeoutMs = smithers2ConnectionTimeoutMs, readTimeoutMs = smithers2ReadTimoutMs).asBytes

    if (response.code == 200) XML.load(new ByteArrayInputStream(response.body))
    else throw new RuntimeException(s"Server responded with error code: ${response.code} ${response.statusLine}")
  }
}
