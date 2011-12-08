
Building Sincerity
==================

Ant
---

All you need to build Sincerity is Ant. The latest version is available here:

	http://ant.apache.org/

The resulting build will go into the "/build/distribution/" directory. Temporary files
used during the build process will go into "/build/cache/", which you are free to
delete.

During the build process, dependencies will be downloaded from online repositories,
so you do need Internet access.


Maven Integration
-----------------

If you need to integrate the build into a Maven project, you can use the POMs under
the "/build/maven/" directory. They are simple wrappers over the Ant build mentioned
above.
