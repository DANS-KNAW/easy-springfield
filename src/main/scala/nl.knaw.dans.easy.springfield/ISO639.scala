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

import better.files.File
import better.files.File.root
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.configuration.PropertiesConfiguration
import scala.collection.JavaConverters._

case class ISO639(version: String, properties: PropertiesConfiguration) { //TODO should this class have versioning?
  def isAValidLanguageCode(code: String): Boolean = {
    Option(properties.getString(code)).isDefined
  }

  def getCountryNameForCode(code: String): Option[String] = {
    Option(properties.getString(code))
  }

  def getSupportedCodes: List[String] = {
    properties
      .getKeys
      .asScala
      .toList
  }

  def getSupportedCodesWithName: Map[String, String] = {
    getSupportedCodes.flatMap(key => Map(key -> properties.getString(key)))
      .toMap
  }
}

object ISO639 extends DebugEnhancedLogging {

  def apply(home: File): ISO639 = {
    val cfgPath = Seq(
      root / "etc" / "opt" / "dans.knaw.nl" / "easy-springfield",
      home / "cfg"
    )
      .find(_.exists)
      .getOrElse { throw new IllegalStateException("No configuration directory found") }
    logger.info(s"cfgPath: $cfgPath")

    new ISO639(
      version = (home / "bin" / "version").contentAsString,
      properties = new PropertiesConfiguration() {
        setDelimiterParsingDisabled(true)
        load((cfgPath / "iso-639-1.properties").toJava)
      }
    )
  }
}
