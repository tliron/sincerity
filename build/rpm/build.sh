#
# Copyright 2009-2010 Three Crickets LLC.
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
cd $HERE

NAME=sincerity-1.0beta1-0.noarch
OUTPUT=BUILDROOT/$NAME

# Content
rm -rf $OUTPUT
mkdir -p $OUTPUT/usr/lib/sincerity/
mkdir -p $OUTPUT/usr/share/applications/
cp -r ../distribution/content/* $OUTPUT/usr/lib/sincerity/
cp ../../components/media/sincerity.png $OUTPUT/usr/lib/sincerity/
cp sincerity.desktop $OUTPUT/usr/share/applications/

rpmbuild --define "_topdir $HERE" --target noarch -bb --sign SPECS/sincerity.spec

# Cleanup
rm -rf $OUTPUT
mv RPMS/noarch/$NAME.rpm ../distribution/sincerity-1.0-beta1.rpm

echo Done!
