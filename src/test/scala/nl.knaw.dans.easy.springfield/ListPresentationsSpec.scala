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

class ListPresentationsSpec extends TestSupportFixture with ListPresentations {
  "listPresentations" should "return Seq of Presentations" in {
    val parent = <fsxml>
      <user id="Barbapappa">
        <collection id="Lala">
            <presentation id="easy-dataset:218799"
                referid="/domain/dans/user/Barbapappa/presentation/4">
            </presentation>
            <presentation id="easy-dataset:218800"
                referid="/domain/dans/user/Barbapappa/presentation/5">
            </presentation>
        </collection>
        <presentation id="1">
            <videoplaylist id="1">
                <audio id="Bright_20160325_interview.mp3"
                       referid="/domain/dans/user/Barbapappa/audio/1">
                    <properties>
                        <private>true</private>
                    </properties>
                </audio>
            </videoplaylist>
        </presentation>
      </user>
    </fsxml>

    listPresentations(parent) shouldBe Seq(
      Seq("easy-dataset:218799", "/domain/dans/user/Barbapappa/presentation/4"),
      Seq("easy-dataset:218800", "/domain/dans/user/Barbapappa/presentation/5"),
    )
  }

  it should "return an empty Seq if there are no Presentations" in {
    val parent = <fsxml>
      <video id="video01" />
      <audio id="audio01" />
    </fsxml>

    listPresentations(parent) shouldBe empty
  }

  it should "just return empty Seq if no Springfield xml is passed to it" in {
    val parent = <html>
      <body>This is not springfield</body>
    </html>

    listPresentations(parent) shouldBe empty
  }
}
