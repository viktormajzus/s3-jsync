#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

chmod +x packaging/rpm/post    2>/dev/null || true
chmod +x packaging/rpm/postun  2>/dev/null || true

if command -v dnf >/dev/null 2>&1; then
  sudo dnf install -y rpm-build
elif command -v yum >/dev/null 2>&1; then
  sudo yum install -y rpm-build
else
  echo "This script is for RHEL/Rocky/Fedora (dnf/yum not found)"; exit 1
fi

./gradlew --no-daemon clean jpackage -Ppkg=rpm

artifact="$(ls -1 build/jpackage/*.rpm | tail -n 1)"
echo "Built: $artifact"
echo "Install with: sudo dnf install -y \"$artifact\""
