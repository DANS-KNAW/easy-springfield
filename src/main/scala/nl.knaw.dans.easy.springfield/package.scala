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
package nl.knaw.dans.easy

import java.nio.file.Path

package object springfield {
  case class SpringfieldErrorException(errorCode: Int, message: String, details: String) extends Exception(s"($errorCode) $message: $details")
  case class VideoPathWithId(path: Path, id: String)

  val MAX_NAME_LENGTH = 100

  object AvType extends Enumeration {
    type AvType = Value
    val audio, video = Value
  }

  object Playmode extends Enumeration {
    type Playmode = Value
    val menu, continuous = Value
  }
}
