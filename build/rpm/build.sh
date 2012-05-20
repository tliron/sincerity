#!/bin/bash

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
