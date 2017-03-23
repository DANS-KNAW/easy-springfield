import nl.knaw.dans.easy.springfield.ListAction

class ListActionSpec extends TestSupportFixture with ListAction {
  "listUsers" should "return Seq of user names" in {
    val parent = <fsxml>
      <user id="user01"/>
      <user id="user02"/>
    </fsxml>

    listUsers(parent) should be(Seq("user01", "user02"))
  }

  it should "return an empty Seq if there are no users" in {
    val parent = <fsxml>
    </fsxml>

    listUsers(parent) should be(empty)
  }

  it should "ignore properties listed before users" in {
    val parent = <fsxml>
      <properties>
        <depth>10</depth>
        <start>0</start>
        <limit>-1</limit>
        <totalResultsAvailable>46</totalResultsAvailable>
        <totalResultsReturned>46</totalResultsReturned>
      </properties>
      <user id="user01"/>
      <user id="user02"/>
    </fsxml>
    listUsers(parent) should be(Seq("user01", "user02"))
  }

  it should "just return empty Seq if no Springfield xml is passed to it" in {
    val parent = <html>
      <body>This is not springfield</body>
    </html>

    listUsers(parent) should be(empty)
  }

}
