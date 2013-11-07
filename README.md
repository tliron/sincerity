
Sincerity
=========

Sincerity is a tool for installing and bootstrapping software stacks on top of the JVM.
It makes these otherwise tedious tasks easy, simple and fun.

Please see the main [Sincerity](http://threecrickets.com/sincerity/) site for comprehensive
documentation.


Building Sincerity
------------------ 

All you need to build Sincerity is [Ant](http://ant.apache.org/).

Simply change to the "/build/" directory and run "ant".

During the build process, build and distribution dependencies will be downloaded from an
online repository at http://repository.threecrickets.com/, so you will need Internet access.

The result of the build will go into the "/build/distribution/" directory. Temporary
files used during the build process will go into "/build/cache/", which you are free to
delete.

To avoid the "bootstrap class path not set" warning during compilation (harmless),
configure the "compile.boot" setting in "private.properties" (see below).


Configuring the Build
---------------------

The "/build/custom.properties" file contains configurable settings, along with
some commentary on what they are used for. You are free to edit that file, however
to avoid git conflicts, it would be better to create your own "/build/private.properties"
instead, in which you can override any of the settings. That file will be ignored by git.


Building the Sincerity Manual
-----------------------------

To build the manual, as part of the standard build process, you will need to install
[LyX](http://www.lyx.org/) and [eLyXer](http://elyxer.nongnu.org/), and configure their
paths in "private.properties".


Building the Sincerity Eclipse Plugin
-------------------------------------

You will need to install [Eclipse](http://www.eclipse.org/) and configure its path in
"private.properties".


Packaging
---------

You can create distribution packages (zip, deb, rpm, IzPack) using the appropriate
"package-" Ant targets. They will go into the "/build/distribution/" directory.

If you wish to sign the deb and rpm packages, you need to install the "dpkg-sig" and
"rpm" tools, and configure their paths and your keys in "private.properties". 

In order to build the platform installers (for Windows and OS X), you will need to
install [InstallBuilder](http://installbuilder.bitrock.com/) and configure its path
in "private.properties".

BitRock has generously provided the Sincerity project with a free license, available
under "/build/installbuilder/license.xml". It will automatically be used by the build
process.


Deploying to Maven
------------------

You do *not* need Maven to build Sincerity, however you can deploy your build to a Maven
repository using the "deploy-maven" Ant target. To enable this, you must install
[Maven](http://maven.apache.org/) and configure its path in "private.properties".
