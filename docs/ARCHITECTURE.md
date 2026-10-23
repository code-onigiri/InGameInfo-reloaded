# アーキテクチャ

## バージョンごとの独立ビルド

```
versions/1.20.1/   Forge 47.4.0 / Java 17
versions/1.21.1/   NeoForge 21.1.264 / Java 21
versions/26.1.2/   NeoForge 26.1.2.122 / Java 25
```

Minecraft のバージョン間では Forge / NeoForge の API だけでなく、Mappings・Gradle
プラグイン・Java のバージョンも変わります。1 つの Gradle プロジェクトで複数バージョンを
切り替えると壊れやすいため、**バージョンごとに独立した Gradle プロジェクト**にし、
共有できる設計・API だけを `common/` と `docs/` に置いています。

`common/` には将来、次のようなバージョン非依存コードを置く想定です。

- パーサー（XML / JSON / TXT）とプリンター
- `Value` / `Operation` / タグ名の定数
- MOD 連携 SPI のインターフェース

現時点では 1.20.1 / 1.21.1 / 26.1.2 の実装はそれぞれの `versions/<version>/` 内にあり、
`common/` は空です。抽出は、複数バージョンで実際に同じコードが動くと確認できた部分
だけを対象に行います。無理に共有するとバージョン固有の `ResourceLocation` やレジストリ API の差で
複雑になるためです。

## 処理の流れ（1.20.1 / 1.21.1 / 26.1.2）

1. `InGameInfoXML` コンストラクタ
   - ネットワークパケット登録
   - `ConfigurationHandler.SPEC` を CLIENT 設定として登録
   - Config Screen ファクトリ登録
2. `FMLClientSetupEvent`
   - `Ticker` をローダーのイベントバスへ登録（1.21.1 / 26.1.2 の `InGameInfoCore` は
     `@SubscribeEvent` を持たないため登録しない。`Ticker` が直接呼ぶ）
   - `ValueRegistry.init()` → `TagRegistry.init()` → `PluginLoader.load()`
   - `config/InGameInfo.xml` を読み込み（26.1.2 は MOD のリソースパックが揃うのを
     待つため、`ConfigReloadListener` がリソース再読み込み時に実行）
3. 毎フレーム `RenderGuiEvent.Post`（1.21.1 / 26.1.2 も同名）
   - `Ticker.isRunning()` を判定
   - `InGameInfoCore.onTickClient()` がタグを評価して `Info` ツリーを構築
   - `InGameInfoCore.onTickRender(GuiGraphicsExtractor)` が描画（26.1.2）
4. `RenderGuiOverlayEvent.Pre`（1.21.1 / 26.1.2 は `RenderGuiLayerEvent.Pre`。
   26.1.2 のデバッグオーバーレイはレイヤー名がなく、`canRun` 側で判定）
   - バニラの体力・空腹・酸素・ポーションアイコン・デバッグ文字を必要に応じてキャンセル

## HUD エディタ

`client/gui/editor/GuiHudEditor` は画面下だけを使う「ボトムシート」です。上の HUD は
`Ticker.isRunning()` がエディタ表示中も true を返すため、編集内容がそのまま見えます。

- 行データは `InGameInfoCore#getLines(Alignment)` で TXT 書式へシリアライズして読み、
  `setLines` で全行を `TextParser.parseLine` に通してから反映します。1 行でも不正なら
  保存を中止します。
- タグ一覧 `GuiTags` は `Consumer<String>` を受け取るコンストラクタを持ち、エディタから
  開いた場合は「押したタグをカーソル位置へ挿入して戻る」ピッカーとして動きます。
  通常の `/igi taglist` は親もコンシューマも `null` で、従来どおりの一覧です。
- `Save` は XML を保存したあと `ConfigurationHandler.applyConfiguration()` で
  アライメントの X / Y を TOML へ書き込みます。`F9` と `/igi editor` の両方から開けます。
- 1.12.2 の設定ファイルにはエディタ用の保存先がないため、行は XML、オフセットは TOML に
  分けて保存します。

## パーサーと値

- `parser/xml`, `parser/json`, `parser/text` が `Map<Alignment, List<List<Value>>>`
  を生成します。
- `value/ValueRegistry` が要素名（`str`, `var`, `if`, `max`, `icon` ...）から
  `Value` 実装を生成します。
- `Value#getReplacedValue()` が `{tagname}` を `TagRegistry` で解決します。未知の
  タグは `{tagname}` のまま残ります（1.12.2 と同じ）。
- `Tag` は `Minecraft.getInstance()` のワールド・プレイヤーを参照します。評価は
  レンダースレッドのみで行ってください。

## MOD 連携 SPI

他 MOD は **コンパイル時依存なし**でタグと値を追加できます。

### インターフェース

`versions/1.20.1/src/main/java/df11zomgraves/ingameinfo/api/InGameInfoPlugin.java`

```java
public interface InGameInfoPlugin {
    String getModId();

    default void registerValues(final ValueRegistry registry) {
    }

    default void registerTags(final TagRegistry registry) {
    }
}
```

`PluginLoader` が `ServiceLoader` で実装を検出し、`ValueRegistry.init()` と
`TagRegistry.init()` の後、表示定義を読む前に呼びます。1 つのプラグインが例外を
投げても他のプラグインと本体の読み込みは継続します。

本体に同梱している任意連携（`integration/`）も同じ考え方です。`IntegrationLoader`
が `ModList.get().isLoaded(...)` で MOD の有無を確認し、入っているものだけ
`CreateIntegration`、`CuriosIntegration`、`SereneSeasonsIntegration`、
`ArsNouveauIntegration`、`IronsSpellbooksIntegration`、`ToughAsNailsIntegration`、
`VampirismIntegration`、`ColdSweatIntegration`、`ThirstWasTakenIntegration`、
`GoetyIntegration`、`ParaglidersIntegration`、`BotaniaIntegration` を登録します。
`EnergyIntegration` は Forge / NeoForge 標準機能なので常に登録します。各連携は
`Reflect` 経由で MOD のクラスを呼ぶため、コンパイル時依存も実行時のクラス
ロードエラーもありません。

### 登録方法

```java
package example.compat;

import df11zomgraves.ingameinfo.api.InGameInfoPlugin;
import df11zomgraves.ingameinfo.tag.Tag;
import df11zomgraves.ingameinfo.tag.TagRegistry;

public final class ExamplePlugin implements InGameInfoPlugin {
    @Override
    public String getModId() {
        return "examplemod";
    }

    @Override
    public void registerTags(final TagRegistry registry) {
        registry.register(new Tag() {
            @Override
            public String getCategory() {
                return "examplemod";
            }

            @Override
            public String getValue() {
                return "hello";
            }
        }.setName("exampletag"));
    }
}
```

`META-INF/services/df11zomgraves.ingameinfo.api.InGameInfoPlugin` に実装クラス名を
1 行で書きます。

```
example.compat.ExamplePlugin
```

### 設計上の注意

- `registerTags` / `registerValues` は起動時に 1 回だけ呼ばれます。重い処理や
  ワールドへのアクセスは禁止です。値の評価は `Tag#getValue()` 内で行います。
- タグ名は小文字で一意にしてください。重複はエラーログを出して無視されます。
- 他 MOD のクラスを直接参照すると、その MOD が入っていない環境で
  `NoClassDefFoundError` になります。連携プラグイン側の JAR を別にし、
  `META-INF/services` をその JAR にだけ入れるか、リフレクションで参照してください。
- 1.20.1 / 1.21.1 / 26.1.2 で同じ SPI 名を維持しています。バージョン固有の API は
  プラグイン側で吸収します。
- 設定の再読み込み（`ModConfigEvent.Reloading`）では、ライブのアライメントを
  TOML の値で上書きしません。保存直後の再読み込みがエディタの変更を消すためです。
  起動時の `Loading` では TOML の値を反映します。TOML を手で編集した場合は
  再起動してください。

## マルチバージョン移植のチェックリスト

1. 対象バージョンの Forge / NeoForge MDK と Java を flake に追加する。
2. `versions/<version>` を作り、ビルドスクリプトと `mods.toml` / `neoforge.mods.toml`
   を合わせる。
3. レジストリアクセス（`BuiltInRegistries`、`RegistryAccess`）を確認する。
4. GUI 描画 API（`GuiGraphics`、`RenderSystem`）の変更を確認する。
5. タグの値が取れない場合は、タグ名を残したまま互換値を返す。
6. `docs/COMPATIBILITY.md` に差分を追記する。
7. `scripts/render-test.sh` でスクリーンショットを撮る。
