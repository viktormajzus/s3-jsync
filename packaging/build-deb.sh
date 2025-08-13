#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

chmod +x packaging/deb/postinst 2>/dev/null || true
chmod +x packaging/deb/postrm   2>/dev/null || true

if ! command -v apt >/dev/null 2>&1; then
  echo "This script is for Debian/Ubuntu (apt not found)"; exit 1
fi

sudo apt update -y
sudo apt install -y dpkg-dev fakeroot

./gradlew --no-daemon clean jpackage -Ppkg=deb

artifact="$(ls -1 build/jpackage/*.deb | tail -n 1)"
echo "Built: $artifact"
echo "Install with: sudo apt install \"$artifact\""
