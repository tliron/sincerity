
Sincerity
=========

Sincerity is a tool for installing and bootstrapping software stacks on top of the JVM.
It makes these otherwise tedious tasks easy, simple and fun.

Please see the main Sincerity site for comprehensive documentation:

http://threecrickets.com/sincerity/


Building Sincerity
------------------ 

All you need to build Sincerity is Ant:

http://ant.apache.org/

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
LyX and eLyXer, and configure their paths in "private.properties":

http://www.lyx.org/

http://elyxer.nongnu.org/


Building the Sincerity Eclipse Plugin
-------------------------------------

You will need to install Eclipse and configure its path in "private.properties":

http://www.eclipse.org/


Packaging
---------

You can create distribution packages (zip, deb, rpm, IzPack) using the appropriate
"package-" Ant targets. They will go into the "/build/distribution/" directory.

If you wish to sign the deb and rpm packages, you need to install the "dpkg-sig" and
"rpm" tools, and configure their paths and your keys in "private.properties". 

In order to build the platform installers (for Windows and OS X), you will need to
install InstallBuilder and configure its path in "private.properties":

http://installbuilder.bitrock.com/

BitRock has generously provided the Sincerity project with a free license, available
under "/build/installbuilder/license.xml". It will automatically be used by the build
process.


Deploying to Maven
------------------

You do *not* need Maven to build Sincerity, however you can deploy your build to a Maven
repository using the "deploy-maven" Ant target. To enable this, you must install Maven
and configure its path in "private.properties":

http://maven.apache.org/
