# versions

Minecraft のバージョンごとに独立した Gradle プロジェクトを置きます。バージョン間で
Gradle プラグイン・Mappings・Java・ローダー API が違うため、1 つのプロジェクトで
切り替えるのではなく、**ディレクトリを分けて独立にビルド**します。

## 一覧

| ディレクトリ | Minecraft | ローダー | Java | 状態 |
|---|---|---|---|---|
| `1.20.1/` | 1.20.1 | Forge 47.4.0 | 17 | 実装・ビルド・描画テスト済み |
| `1.21.1/` | 1.21.1 | NeoForge 21.1.x | 21 | 計画中（未作成） |
| `26.1.2/` | 26.1.2 | NeoForge 26.1.2.x | 25 | 計画中（未作成） |

1.21.1 と 26.1.2 は、1.20.1 の実装と描画テストが安定してから着手します。バージョン
ごとの flake シェル（`mc1201` / `mc1211` / `mc2612`）は用意済みですが、実際に
ビルド・描画まで確認したのは 1.20.1 だけです。

## 各バージョンの構成

```
versions/1.20.1/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradle/wrapper/
├── src/main/java/df11zomgraves/ingameinfo/
│   ├── api/            # MOD 連携 SPI
│   ├── client/         # 設定 GUI・タグ一覧 GUI
│   ├── command/        # /igi コマンド
│   ├── gui/overlay/    # HUD の描画要素
│   ├── handler/        # 設定・イベント・キー
│   ├── parser/         # XML / JSON / TXT
│   ├── printer/        # XML / JSON / TXT 出力
│   ├── tag/            # タグ実装
│   ├── util/
│   └── value/          # 値・演算
└── src/main/resources/
    ├── META-INF/mods.toml
    ├── META-INF/services/
    └── assets/ingameinfo/lang/
```

`run/` は開発用の作業ディレクトリで、Git 管理外です。テスト用ワールド・設定・
スクリーンショットのログがここに出ます。

## ビルド

```bash
# リポジトリのルートで
nix develop .#mc1201 -c bash -c 'cd versions/1.20.1 && ./gradlew build'
```

成果物は `versions/1.20.1/build/libs/ingameinfoxml-<version>.jar` です。Forge 47.4.0
のクライアント・サーバーにそのまま入れられます。開発用クライアントは次で起動します。

```bash
nix develop .#mc1201 -c bash -c 'cd versions/1.20.1 && ./gradlew runClient'
```

Nix を使わない場合は JDK 17 と Gradle 8.8 を用意し、`GRADLE_USER_HOME` を書き込み
可能な場所に向けてください。

## 新しいバージョンを追加するとき

1. 対象バージョンの Forge / NeoForge MDK を取得し、`versions/<version>/` に
   `build.gradle`、`gradle.properties`、`settings.gradle`、wrapper を置く。
2. `flake.nix` の `mc<version>` シェルを、そのバージョンが要求する JDK に合わせる。
3. `mods.toml`（NeoForge は `neoforge.mods.toml`）の `modId` は
   `ingameinfoxml` のまま、バージョンだけ変える。
4. `docs/COMPATIBILITY.md` の表をコピーし、そのバージョンの差分を追記する。
5. `scripts/render-test.sh` 用のワールドを `versions/<version>/run/saves/RenderTest`
   に用意し、HUD と GUI を確認する。
6. 2 バージョンで同じコードが動く部分だけを `common/` へ移す。

バージョン間で共有するのは設計と SPI の完全修飾名だけです。`common/` の扱いは
[common/README.md](../common/README.md) を参照してください。

## バージョン固有の注意

- **1.20.1**: 公式 Mappings、Forge 47.4.0、Java 17。`ModLoadingContext.get()` は
  非推奨だが動作する（ビルド時に警告）。Config Screen は Forge の
  `ConfigScreenHandler.ConfigScreenFactory`。
- **1.21.1**: NeoForge 21.1.x、Java 21。Mappings とイベントバス API が変わる。
  データパックの関数ディレクトリが単数形（`function`）になる。
- **26.1.2**: NeoForge 26.1.2.x、Java 25。GUI 描画・レジストリ API の変更を確認する。

## テスト

描画テストの手順は [docs/RENDER_TEST.md](../docs/RENDER_TEST.md) にあります。各
バージョンで `hud-<version>.png` を撮り、比較します。

```bash
nix develop .#mc1201 -c scripts/render-test.sh 1.20.1 RenderTest
```

## 関連文書

- [リポジトリ README](../README.md)
- [アーキテクチャ](../docs/ARCHITECTURE.md)
- [1.12.2 との互換性](../docs/COMPATIBILITY.md)
- [描画テスト](../docs/RENDER_TEST.md)
