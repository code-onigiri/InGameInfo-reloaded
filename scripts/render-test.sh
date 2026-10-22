#!/usr/bin/env bash
# Headless HUD smoke test for a version directory.
#
# Usage (from the repository root, inside a Nix dev shell):
#   nix develop .#mc1201 -c scripts/render-test.sh [VERSION] [WORLD]
#
# VERSION defaults to 1.20.1. WORLD defaults to RenderTest. The world must
# already exist in versions/<VERSION>/run/saves (see docs/RENDER_TEST.md).
#
# Requirements: xvfb-run, xdotool and ImageMagick's import on PATH.
set -euo pipefail

VERSION="${1:-1.20.1}"
WORLD="${2:-RenderTest}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GAME_DIR="$ROOT/versions/$VERSION"
OUT_DIR="$ROOT/docs/render"
LOG="$OUT_DIR/client-$VERSION.log"
SHOT="$OUT_DIR/hud-$VERSION.png"
HUD_DELAY="${HUD_DELAY:-20}"

for tool in xvfb-run xdotool import; do
    if ! command -v "$tool" >/dev/null 2>&1; then
        echo "error: '$tool' is not on PATH. Run this inside 'nix develop .#mc1201'." >&2
        exit 1
    fi
done

if [ ! -d "$GAME_DIR" ]; then
    echo "error: version directory not found: $GAME_DIR" >&2
    exit 1
fi
if [ ! -d "$GAME_DIR/run/saves/$WORLD" ]; then
    echo "error: test world not found: $GAME_DIR/run/saves/$WORLD" >&2
    echo "       create one with a vanilla server or copy an existing world." >&2
    exit 1
fi

mkdir -p "$OUT_DIR"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT/.gradle}"

echo "client log: $LOG"
echo "screenshot: $SHOT"

xvfb-run -a -s "-screen 0 1280x720x24 +extension GLX +render -noreset" \
    bash -c '
        set -u
        cd "$1"
        # --no-daemon is required: a pre-existing Gradle daemon inherits the
        # environment it was started in and may not see this Xvfb DISPLAY.
        ./gradlew --no-daemon runClient --args="--quickPlaySingleplayer $2" > "$3" 2>&1 &
        GPID=$!
        found=0
        for _ in $(seq 1 180); do
            sleep 2
            if xdotool search --name "Minecraft" >/dev/null 2>&1; then
                found=1
                break
            fi
            if ! kill -0 "$GPID" 2>/dev/null; then
                break
            fi
        done
        if [ "$found" = 1 ]; then
            sleep "$4"
            import -window root "$5" || true
            echo "captured: $5"
        else
            echo "Minecraft window did not appear; see $3" >&2
        fi
        kill "$GPID" 2>/dev/null || true
        sleep 3
        pkill -f "quickPlaySingleplayer $2" 2>/dev/null || true
        exit 0
    ' _ "$GAME_DIR" "$WORLD" "$LOG" "$HUD_DELAY" "$SHOT"

if [ -f "$SHOT" ]; then
    echo "ok: $SHOT"
else
    echo "no screenshot produced" >&2
    tail -n 60 "$LOG" >&2 || true
    exit 1
fi
