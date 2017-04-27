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

import java.nio.file.{Path, Paths}

import org.scalatest.Inside

class CreateAddActionsSpec extends TestSupportFixture with Inside with CreateAddActions {
  val sourceVideos: Path = Paths.get("src/test/resources/sourceVideos")

  "createAddVideo" should "return a filled-in video element" in {
    val videoElem = createAddVideo(Paths.get("/my/source/vid.mp4"), "vid01.mp4")

    videoElem.label shouldBe "video"
    videoElem.attribute("src").get.head.text shouldBe "/my/source/vid.mp4"
    videoElem.attribute("target").get.head.text shouldBe "vid01.mp4"
  }

  it should "prepend sourceVideos if srcVideo is relative" in {
    val videoElem = createAddVideo(Paths.get("my/source/vid.mp4"), "vid01.mp4")

    videoElem.attribute("src").get.head.text shouldBe "src/test/resources/sourceVideos/my/source/vid.mp4"
  }

  "createAddPresentation" should "provide the correct wrapper elements for a single added video" in {
    val a = createAddPresentation(Seq(Video(Paths.get("/my/source/vid.mp4"), "dans", "user01", "coll01", "pres01", "vid01.mp4", requireTicket = false)))

    a.label shouldBe "add"
    a.attribute("target").get.head.text shouldBe "domain/dans/user/user01/collection/coll01"
    val p = a \\ "presentation"
    p should have length 1
    p.head.attribute("name").get.head.text shouldBe "pres01"
    val pl = a \ "presentation" \\ "video-playlist"
    pl should have length 1
    pl.head.attribute("require-ticket").get.head.text shouldBe "false"
    val vs = a \ "presentation" \ "video-playlist" \\ "video"
    vs should have length 1
    vs.head.attribute("src").get.head.text shouldBe "/my/source/vid.mp4"
    vs.head.attribute("target").get.head.text shouldBe "vid01.mp4"
  }

  it should "sort multiple videos alphabetically" in {
    val a = createAddPresentation(Seq(
      Video(Paths.get("/my/source/vid2.mp4"), "dans", "user01", "coll01", "pres01", "vid02.mp4", requireTicket = false),
      Video(Paths.get("/my/source/vid1.mp4"), "dans", "user01", "coll01", "pres01", "vid01.mp4", requireTicket = false)))

    val vs = a \ "presentation" \ "video-playlist" \\ "video"
    vs should have length 2

    inside(vs.toList) {
      case List(v1, v2) =>
          v1.attribute("target").get.head.text shouldBe "vid01.mp4"
          v2.attribute("target").get.head.text shouldBe "vid02.mp4"
    }
  }


}
