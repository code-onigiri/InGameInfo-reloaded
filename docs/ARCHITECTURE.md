# アーキテクチャ

## バージョンごとの独立ビルド

```
versions/1.20.1/   Forge 47.4.0 / Java 17
versions/1.21.1/   NeoForge 21.1.x / Java 21（計画）
versions/26.1.2/   NeoForge 26.1.2.x / Java 25（計画）
```

Minecraft のバージョン間では Forge / NeoForge の API だけでなく、Mappings・Gradle
プラグイン・Java のバージョンも変わります。1 つの Gradle プロジェクトで複数バージョンを
切り替えると壊れやすいため、**バージョンごとに独立した Gradle プロジェクト**にし、
共有できる設計・API だけを `common/` と `docs/` に置いています。

`common/` には将来、次のようなバージョン非依存コードを置く想定です。

- パーサー（XML / JSON / TXT）とプリンター
- `Value` / `Operation` / タグ名の定数
- MOD 連携 SPI のインターフェース

ただし 1.20.1 の実装はまだ `versions/1.20.1` 内にあり、`common/` への抽出は
1.21.1 移植時に、実際に 2 バージョンで共有できる部分だけを対象に行います。無理に
共有するとバージョン固有の `ResourceLocation` やレジストリ API の差で複雑になるためです。

## 1.20.1 の処理の流れ

1. `InGameInfoXML` コンストラクタ
   - ネットワークパケット登録
   - `ConfigurationHandler.SPEC` を CLIENT 設定として登録
   - Config Screen ファクトリ登録
2. `FMLClientSetupEvent`
   - `InGameInfoCore` と `Ticker` を Forge イベントバスへ登録
   - `ValueRegistry.init()` → `TagRegistry.init()` → `PluginLoader.load()`
   - `config/InGameInfo.xml` を読み込み
3. 毎フレーム `RenderGuiEvent.Post`
   - `Ticker.isRunning()` を判定
   - `InGameInfoCore.onTickClient()` がタグを評価して `Info` ツリーを構築
   - `InGameInfoCore.onTickRender(GuiGraphics)` が描画
4. `RenderGuiOverlayEvent.Pre`
   - バニラの体力・空腹・酸素・ポーションアイコン・デバッグ文字を必要に応じてキャンセル

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
- 1.21.1 / 26.1.2 でも同じ SPI 名を維持する予定です。バージョン固有の API は
  プラグイン側で吸収します。

## マルチバージョン移植のチェックリスト

1. 対象バージョンの Forge / NeoForge MDK と Java を flake に追加する。
2. `versions/<version>` を作り、ビルドスクリプトと `mods.toml` / `neoforge.mods.toml`
   を合わせる。
3. レジストリアクセス（`BuiltInRegistries`、`RegistryAccess`）を確認する。
4. GUI 描画 API（`GuiGraphics`、`RenderSystem`）の変更を確認する。
5. タグの値が取れない場合は、タグ名を残したまま互換値を返す。
6. `docs/COMPATIBILITY.md` に差分を追記する。
7. `scripts/render-test.sh` でスクリーンショットを撮る。
