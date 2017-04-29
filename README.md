easy-springfield
================

Tools for managing a Springfield WebTV server.


SYNOPSIS
--------

    easy-springfield list-users [-d, --domain <arg>]
    easy-springfield create-user [-d, --target-domain <arg>]
    easy-springfield create-collection [-t, --title <arg>] [-d, --description <arg>] 
        [--target-domain <arg>] <collection> <target-user>
    easy-springfield create-springfield-actions [-p, --check-parent-items] [-v, --videos-folder <arg>]
        <videos-csv> > springfield-actions.xml   
    easy-springfield status [-u, --user <arg>][-d, --domain <arg>]
    easy-springfield set-require-ticket <springfield-path> {true|false}
    easy-springfield create-ticket [-e,--expires-after-seconds <arg>] [-t, --ticket <arg>] <springfield-path>
    easy-springfield delete-ticket <ticket>
    easy-springfield delete [-r, --with-referenced-items] <springfield-path>
    
    
DESCRIPTION
-----------
[Springfield Web TV] is a platform for delivering A/V media files over the web. Managing
the hosted content of a Springfield instance can be a rather challenging task, due to the
lack of supporting tools. `easy-springfield` provides some commands to ease this task. It
does *not* presume to fully automate Springfield management.

(Note: even though Springfield can also serve audio-only media files, all media files are generall
referred to as "videos" in the Springfield interface. We will do the same below.)

### Springfield paths
Videos in Springfield are stored in a tree-structure. The service `smithers2` keeps
track of this structure and offers a RESTful API to it. `easy-springfield` uses this RESTful
API. Where the commands require a `springfield-path` argument, a path into aforementioned tree is intended.

Some examples:

    domain/dans/user 
    domain/dans/user/getuigen
    domain/dans/user/getuigen/collection/ww2
    domain/dans/user/getuigen/collection/ww2/presentation/easy-dataset:12345
    domain/dans/user/history/presentation/8
    domain/dans/user/history/video/1
    
As you can see in the last two examples, presentations and videos are stored directly under the user
(in this case called "history"). Presentations *reference* videos by springfield path to include
them, and collections *reference* presentations to do the same.

It is possible to configure a *default domain*. This allows you to leave out the domain of the springfield
paths. If you configure the default domain to `dans` the above examples then become:

    user 
    user/getuigen
    user/getuigen/collection/ww2
    user/getuigen/collection/ww2/presentation/easy-dataset:12345
    user/history/presentation/8
    user/history/video/1

Everywhere else where a domain must be specified, it will then also default to the value you configured.

### Examining raw Springfield metadata 
Although `easy-springfield` lets you manage a considerable part of the Springfield repository without your
having to interact with `smithers2` directly, it is often convenient to examine its raw output. You can
construct the smithers-URL of an item as follows:

    <smithers2-base-uri>/<springfield-path>
    
`smithers2-base-uri` can be found in `application.properties`. `springfield-path` must of course include
the domain. To get all the metadata for user `getuigen` in the above example you would therefore open the
following URL in your browser:

    http://yourstreamingserver:8080/smithers2/domain/dans/user/getuigen

This is of course assuming that you have access to port 8080 of the server running Springfield. You may 
have to use an SSH-tunnel if this is not the case:

    ssh -L 8080:localhost:8080 yourstreamingserver
    
    # In a new terminal window:
    http://localhost:8080/smithers2/domain/dans/user/getuigen

The XML that is returned by Springfield will not be documented here, but for the most part is fairly 
easy to understand. Most of the subcommands supported by `easy-springfield` use this XML to implement
their functionality.

### Listing and creating containers
As discussed before, the videos are stored as leaves of a tree structure. The parent elements
of that structure can, in part, be examined and managed by `easy-springfield`. 

Currently it is only possible to list the users (in a given domain). To get a list of the items
in a collection or a presentation, retrieve the raw Springfield metadata as explained in the previous
section.

Users and collections can be created with the subcommands `create-user` and `create-collection`. In
contrast to the command to add videos to Springfield - discussed below - these subcommands will
*not* create Springfield actions XML, but in fact create the specified items in Springfield directly.

### Adding videos 
Since Springfield can only add videos that are placed in its inbox, it is not possible to add them
directly via a subcommand. Instead, the subcommand `create-springfield-actions` generates the XML
that must be placed with the video files in the inbox. The input for this subcommand is a CSV file
containing the required metadata about the videos. The columns are defined by the following headers:

* `SRC-VIDEO` - the relative path to the video file in `springfield-inbox` 
* `DOMAIN` - (optional) the domain under which to add the video (must exist); if not specified: the default domain
* `USER` - the user under which to add the video (must exist)
* `COLLECTION` - the collection under which to add the video (must exist)
* `PRESENTATION` - the presentation under which to add the video (**must not** exist; will be created)
* `TARGET-VIDEO` - (optional) the name of the video in Springfield; if not specified: the base name of `SRC-VIDEO`
* `REQUIRE-TICKET` - whether an authorization ticket is required to play the video (`true` of `false`) 

The subcommand will print the XML directly to the standard output, so to use it you will have to redirect STDOUT
to a file. The OK/error messages will not interfere, as they will be printed on STDERR rather than STDOUT.

The subcommand can optionally check if the hierarchy in which to store the videos exists, as this is a
precondition for subsequent successful processing by Springfield. Likewise, it can check if the videos
actually exist in a specified folder. 

In summary, to add a folder with videos to Springfield you would perform the following steps:

1. Create a CSV-file (or you might start with a spreadsheet).
2. Add the column headers mentioned above (at least the mandatory ones).
3. Add to the `SRC-VIDEO` header: the paths of the videos relative to the *parent* of the folder that you plan to move 
  to the springfield-inbox.
4. Fill in the other columns to specify the desired situation in Springfield.
5. Save/export the CSV-file, say, to myvideos.csv
6. Run `easy-springfield -v /path/to/videos -p myvideos.csv > videos01.xml`. Now, if `/path/to/videos` contains one subdirectory
   called `videos01` and all the paths in `SRC-VIDEO` start with `videos01/...`  the tool will check if you are all
   set to move `videos01` to the springfield-inbox. (You may of course want to run this command first *without*
   redirecting the output to `videos01.xml`, to check if the generated XML looks OK.)
7. To be sure that there are no ownership issues `chmod 777` everything in `videos01` and also `videos01.xml`.
8. Move (or copy) the directory `videos01` to the springfield-inbox.
9. Move (or copy) the Springfield actions file `videos01.xml` to the springfield-inbox.

The Springfield service `uter` checks the inbox once a minute for new files. Processing the videos may take a while,
so at this point you might go off and take care of some other task. To check the status you can run the `status`
subcommand from time to time (see next section).

To debug any problems, it is best to examine `uter`'s  log file at `/var/log/tomcat/uter/uter.log`. 
 
### Status report
Processing the videos in the inbox takes a while. The `status` command generates a report that 
lists the A/V items and their current status (`DONE`, `WAITING` or `FAILED`).

### Changing require-ticket
You can change videos from public to private and vice versa with the `set-require-ticket` subcommand. This command
changes all the videos under a given parent, so you can change collections and users at a time. Before making the
actual changes you are requested to confirm.

### Deleting items
You can delete videos, presentations and collections with the `delete` subcommand. Users cannot currently be 
deleted, even if they contain no resources anymore. This seems a bug in Springfield, as, over time it will inevitably
result in an increasing number of unused "user" resources. The `-r` option lets you include all the referenced resources
in a delete action. This way you can delete a whole collection without first having to manually delete all the 
resources referenced by it. Again, the user is asked to confirm such a delete action.

[Springfield Web TV]: http://www.noterik.nl/products/webtv_framework/ 

ARGUMENTS
---------

