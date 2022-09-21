/*
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

import scala.util.Try
import scala.xml.Elem

trait ListFiles {

  def listFiles(parent: Elem): Seq[(String, String, String,String, Boolean)] = {
    for {
      presentationInCollection <- parent \ "user" \ "collection" \ "presentation"
      datasetId = presentationInCollection \@ "id"
      presentationReferId = presentationInCollection \@ "referid"
      presentationNr = presentationReferId.replaceAll(".*/", "")
      presentation <- parent \ "user" \ "presentation"
      if (presentation \@ "id") == presentationNr
      avDef <- (presentation \ "videoplaylist").flatMap(_.nonEmptyChildren)
      if Seq("audio","video").contains(avDef.label)
      avType = avDef.label
      avReferId = avDef \@ "referid"
      avNr = avReferId.replaceAll(".*/", "")
      av <- parent \ "user" \ avType
      if (av \@ "id") == avNr
      priv = Try((av \ "properties" \ "private").text.toBoolean).getOrElse(false)
      raw1 <- (av \ s"raw$avType").filter(raw => (raw \@ "id") == "1")
      raw2 <- (av \ s"raw$avType").filter(raw => (raw \@ "id") == "2")
      fileName1 = (raw1 \ "properties" \ "filename").text // assuming <original>true</original>
      fileName2 = (raw2 \ "properties" \ "filename").text
    } yield {
      (datasetId, presentationReferId, fileName1, s"$avReferId/raw$avType/2/$fileName2", priv)
    }
  }
}
