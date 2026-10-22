# InGameInfoXML (reloaded)

[InGameInfoXML](https://github.com/Lunatrius/InGame-Info-XML) を現行の Minecraft / Forge /
NeoForge へ移植するリポジトリです。1.12.2 版の HUD 書式・設定・コマンドを基準に、
「同じ設定ファイルで同じ表示になる」ことを目標にしています。

## 現在の状態

| バージョン | ローダー | 状態 |
|---|---|---|
| 1.20.1 | Forge 47.4.0 / Java 17 | 実装・ビルド・HUD 描画テスト済み |
| 1.21.1 | NeoForge 21.1.x / Java 21 | 計画中（環境のみ用意） |
| 26.1.2 | NeoForge 26.1.2.x / Java 25 | 計画中（環境のみ用意） |

1.20.1 版は次を移植済みです。

- HUD 描画（テキスト、アイコン、アイテム、ポーション効果、9 アライメント、スケール）
- 既定の `ingameinfo.xml`、XML / JSON / TXT パーサー
- XML / JSON / TXT プリンター（`/igi save`）
- 設定画面（Forge Config Screen）と `/igi config`
- タグ一覧 GUI（`/igi taglist`）
- `/igi reload`、`load`、`save`、`enable`、`disable`
- 9 言語（en_us, ja_jp, de_de, fr_fr, no_no, pl_pl, ru_ru, zh_cn, zh_tw）
- 1.12.2 の全タグ名（値が再現できないものは互換値。詳細は
  [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md)）
- 他 MOD 連携用の ServiceLoader SPI（[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)）

## リポジトリ構成

```
.
├── flake.nix                  # JDK / Mesa / Xvfb / ImageMagick 入り dev shell
├── common/                    # バージョン非依存の設計メモ（将来の共有ソース置き場）
├── docs/
│   ├── COMPATIBILITY.md       # 1.12.2 との差異と再現方法
│   ├── ARCHITECTURE.md        # マルチバージョン構成と MOD 連携 SPI
│   └── RENDER_TEST.md         # ヘッドレス描画テストの手順
├── scripts/render-test.sh     # Xvfb + llvmpipe で HUD をスクリーンショット
└── versions/
    └── 1.20.1/                # Forge 1.20.1 の実装
```

`InGame-Info-XML/` は比較用の 1.12.2 原本で、別 Git リポジトリです（このリポジトリでは
管理しません）。

## 必要環境

Nix 2.18 以降（flake 有効）を推奨します。JDK・Gradle 用 Java・GUI 実行用の
ネイティブライブラリ・Xvfb・Mesa は flake が用意します。

```bash
nix develop .#mc1201     # 1.20.1: JDK 17
# nix develop .#mc1211   # 1.21.1: JDK 21（将来）
# nix develop .#mc2612   # 26.1.2: JDK 25（将来）
```

Nix を使わない場合は JDK 17 と Gradle 8.8 を用意してください。Gradle は同梱の
wrapper を使います。

## ビルド

```bash
nix develop .#mc1201 -c bash -c 'cd versions/1.20.1 && ./gradlew build'
```

成果物は `versions/1.20.1/build/libs/ingameinfoxml-*.jar` です。Forge 47.4.0 の
クライアント・サーバーにそのまま入れられます。

## クライアント起動

```bash
nix develop .#mc1201 -c bash -c 'cd versions/1.20.1 && ./gradlew runClient'
```

## ヘッドレス描画テスト

Xvfb（仮想 X サーバー）と Mesa の llvmpipe（ソフトウェア GL）でクライアントを起動し、
HUD を PNG に保存します。GPU もログインも不要です。

```bash
nix develop .#mc1201 -c scripts/render-test.sh 1.20.1 RenderTest
```

出力は `docs/render/hud-1.20.1.png` と `docs/render/client-1.20.1.log`。テスト用
ワールドの作り方は [docs/RENDER_TEST.md](docs/RENDER_TEST.md) を参照してください。
`HUD_DELAY=60` のように待ち時間を延ばせます。

## 設定

初回起動時に Forge の設定ファイル `config/ingameinfoxml-client.toml` と、表示定義
`config/InGameInfo.xml` が生成されます（表示定義は 1.12.2 と同じファイル名・書式）。

- `/igi reload` — 表示定義を再読み込み
- `/igi load <file>` — 別の表示定義を読み込み
- `/igi save <file.xml|file.json|file.txt>` — 現在の表示定義を保存
- `/igi enable` / `/igi disable` — HUD の一時的な有効・無効
- `/igi taglist` — 登録タグの一覧 GUI
- `/igi alignment get|set <ALIGN> [x y]` — アライメントのオフセット
- `/igi config <key> <value>` — 設定の変更

## 1.12.2 との違い（要約）

レガシーな数値 ID・ブロック metadata・クライアント側のワールドファイルサイズなど、
1.20.1 に存在しない概念があります。タグ名と出力形式は維持し、再現できない値は
「-1」「0」「?」などの互換値にしています。また、明確な描画バグ（HUD の二重描画、
ポーションアイコンの Y 二重加算）は修正しています。全リストは
[docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) を参照してください。

## MOD 連携

他 MOD はコンパイル時依存なしでタグ・値を作れます。詳しくは
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) を参照してください。

## ライセンス

MIT。元実装 Copyright 2014-2016 Jadran "Lunatrius" Kotnik、1.20.1 移植の土台は
Copyright 2025 DF11ZomGraves。`LICENSE` を参照してください。
