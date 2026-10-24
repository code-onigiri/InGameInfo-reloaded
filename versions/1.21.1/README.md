# InGame Info XML 1.21.1

Minecraft 1.21.1、NeoForge 21.1.264、Java 21 向けの実装です。HUD、設定 GUI、タグ一覧、
コマンド、XML / JSON / TXT の読み書き、計算式、MOD 連携の説明は、ルートの
[README（英語）](../../README.md) / [README_ja（日本語）](../../README_ja.md) と
[docs/README.md](../../docs/README.md) にまとめています。

## ビルド

```bash
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew build --no-daemon
```

成果物は `build/libs/ingameinfoxml-2.9.2.103.jar` です。Nix を使う場合は、リポジトリの
ルートで `nix develop .#mc1211` を実行すると JDK 21 と描画テスト用のツールが入ります。

## 開発

開発クライアントは `./gradlew runClient` で起動します。デバッグ用 MOD の On/Off は
[docs/DEBUG_MODS.md](../../docs/DEBUG_MODS.md)、描画テストは
[docs/RENDER_TEST.md](../../docs/RENDER_TEST.md) を参照してください。

変更履歴は [changelog_en.txt](changelog_en.txt) と
[changelog_zh.txt](changelog_zh.txt) にあります。

## ライセンス

MIT。全文は [LICENSE](LICENSE) にあります。
