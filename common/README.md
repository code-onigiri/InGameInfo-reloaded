# common

バージョンに依存しない設計・共有コードの置き場です。**現在は空**で、1.20.1 の実装は
`versions/1.20.1` にあります。ここにビルド成果物はありません。

## なぜ空なのか

1.20.1 / 1.21.1 / 26.1.2 では、Forge / NeoForge の API、Mappings、Java のバージョン、
レジストリ API、描画 API が異なります。最初からすべてを共通化すると、バージョン固有の
型（`ResourceLocation`、`GuiGraphics`、`RegistryAccess` など）が共通層に漏れて、
かえって移植が難しくなります。

そこで、**実際に 2 バージョンで同じコードが動いた部分だけ**をここへ抽出する方針です。
1.20.1 の時点では抽出せず、1.21.1 の移植時に差分を見ながら移します。

## 将来ここへ置く候補

- XML / JSON / TXT パーサーとプリンターのうち、バージョン非依存な部分
- `Value` / `Operation` の評価ロジックとタグ名の定数
- MOD 連携 SPI のインターフェース（`InGameInfoPlugin`）
- 表示定義のデータモデル（`Map<Alignment, List<List<Value>>>` 相当）
- バージョン非依存のユーティリティ（色コード、数値丸め、文字列分割）

## ここへ置かないもの

- `Minecraft` / `ClientLevel` / `Player` を直接参照するタグ実装
- `GuiGraphics` や `RenderSystem` を使う描画コード
- `ResourceLocation` やレジストリ API に依存する処理
- `mods.toml` / `neoforge.mods.toml`、Gradle スクリプト、言語ファイル

これらは `versions/<version>/` に置きます。

## 抽出の手順（1.21.1 以降）

1. 1.20.1 と 1.21.1 の両方でビルド・描画テストを通す。
2. 両方で同一のソースが使えるファイルだけを `common/src/main/java/...` へ移す。
3. 各バージョンの Gradle から `common` を `sourceSets` で参照する。
4. バージョン固有の差分は、小さなインターフェース（例: `PlatformAdapter`）に閉じ込める。
5. 移すたびに `docs/ARCHITECTURE.md` と `docs/COMPATIBILITY.md` を更新する。

パッケージ名は `df11zomgraves.ingameinfo` を維持し、SPI の完全修飾名を変えないで
ください。他 MOD の `META-INF/services` が壊れます。

## 関連文書

- [アーキテクチャ](../docs/ARCHITECTURE.md)
- [1.12.2 との互換性](../docs/COMPATIBILITY.md)
- [描画テスト](../docs/RENDER_TEST.md)
