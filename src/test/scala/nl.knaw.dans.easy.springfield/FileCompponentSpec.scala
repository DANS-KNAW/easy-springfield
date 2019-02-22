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

import java.nio.file.Paths

import org.scalatest._

class FileComponentSpec extends FlatSpec with Matchers with CustomMatchers with FileComponent {


  "createLanguageAdjustedfileName" should "change a fileName called webvtt.vtt with language nl to nl_webvtt_nl.vtt" in {
    createLanguageAdjustedfileName(Paths.get("/path/to/nowehere/webvtt.vtt"), "nl")  shouldBe s"nl_webvtt_nl.vtt"

  }

  it should "do ? without name extension after the dot" in {
    createLanguageAdjustedfileName(Paths.get("/path/to/nowehere/webvtt."), "nl")  shouldBe s"nl_webvtt_nl."
  }

  it should "do ? without extension" in {
    createLanguageAdjustedfileName(Paths.get("/path/to/nowehere/webvtt"), "nl")  shouldBe s"nl_webvtt_nl."
  }

  it should "do ? with an empty language" in {
    createLanguageAdjustedfileName(Paths.get("/path/to/nowehere/webvtt.webvtt"), "")  shouldBe s"_webvtt_.webvtt"
  }
}
