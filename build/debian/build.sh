#!/bin/bash
#
# Copyright 2009-2012 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.opensource.org/licenses/lgpl-3.0.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

set -e

HERE=$(cd "${0%/*}" 2>/dev/null; echo "$PWD")
cd $HERE/debian

# Content
rm -rf content
cp -r ../../distribution/content .
cp ../sincerity.desktop content/
cp ../../../components/media/sincerity.png content/

# .dsc
cp debian/control-any debian/control
dpkg-buildpackage -S -kC11D6BA2

# .deb
cp debian/control-all debian/control
dpkg-buildpackage -b -kC11D6BA2

# Cleanup
rm -rf content
cd ..
mv sincerity_1.0beta1-1_all.deb ../distribution/sincerity-1.0-beta1.deb

echo Done!
