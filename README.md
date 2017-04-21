easy-springfield
================

Tools for managing a Springfield WebTV server


SYNOPSIS
--------

    easy-springfield lsusers 
    easy-springfield rm <path> > springfield-actions.xml
    easy-springfield add ... > springfield-actions.xml
    
    
DESCRIPTION
-----------
[Springfield Web TV] is a platform for delivering A/V media files over the web. Managing
the hosted content of a Springfield instance can be a rather challenging task, due to the
lack of supporting tools. `easy-springfield` provides some commands to ease this task. It
does *not* presume to fully automate Springfield management.

### Springfield paths
Media resources in Springfield are stored in a tree-structure. The service `smithers2` keeps
track of this structure and offers a RESTful API to it. `easy-springfield` uses this RESTful
API. Where the commands require a `path` argument, a path into aformentioned tree is intended.
The structure of such a path is:

    domain/<d>/user/<u>/collection/<c>/presentation/<p>

in which items to the right can be left off.

    domain/dans/user 
    domain/dans/user/getuigen
    domain/dans/user/getuigen/collection/ww2
    domain/dans/user/getuigen/colleciton/ww2/presentation/easy-dataset:12345

[Springfield Web TV]: http://www.noterik.nl/products/webtv_framework/ 


ARGUMENTS
---------

    Subcommand: status - Retrieve the status of content offered for ingestion into Springfield
      -u, --user  <arg>   limit to videos owned by this user
          --help          Show help message
    ---
    
    Subcommand: ls - List items under <path>
          --help   Show help message
    ---
    
    Subcommand: rm - Output Springfield actions to remove the item at <path>
          --help   Show help message
    ---