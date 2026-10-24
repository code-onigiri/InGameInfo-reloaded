# 描画テスト

この文書は、GUI のない環境（SSH・CI・NixOS のヘッドレス機）で InGameInfoXML の HUD と
GUI を実際に描画し、スクリーンショットで確認する手順をまとめたものです。1.20.1 で
確立した手順を 1.21.1 / 26.1.2 でも使い、将来のバージョンにも同じ形で適用できる
ようにしています。共通の準備、機能別の確認、バージョン別の結果、MOD 連携の順に
進みます。

## 対象

- HUD のテキスト（色コード・XML タグ・条件分岐を含む）
- メインハンド / オフハンドのアイテムと耐久値
- ポーション効果の名前・倍率・残り時間・アイコン
- 防具の耐久値とアイコン
- `<icon>`（画像）、`<item>`（アイテム）、`pingicon`
- タグ一覧 GUI（`/igi taglist`）
- 設定 GUI（Forge Config Screen）の全ページ
- HUD エディタ（`F9` / `/igi editor`）。行編集、タグ挿入、保存、オフセットの再起動後の保持
- コマンド（`/igi save`、`/igi reload` など）の入出力

MOD 連携タグそのものは、連携 MOD を入れた環境で別途確認します。SPI の読み込みだけなら
ログの `PluginLoader` 出力で確認できます。実 MOD を入れた確認は下の「MOD 連携」
（Paragliders / AE2 / Mekanism / Cobblemon / Tinkers' Construct / Botania）に
まとめています。

## 前提

- Nix（flake 有効）と、このリポジトリの `flake.nix`
- Xvfb・xdotool・ImageMagick・Mesa（llvmpipe）は `mc1201` シェルに含まれます
- 1.20.1 のテスト用ワールド（下記で作成）
- 実クライアントを起動できる程度のメモリ（2 GB 以上を推奨）

```bash
cd /path/to/InGameInfo-reloaded
nix develop .#mc1201
```

以降のコマンドはリポジトリのルートで実行します。Nix を使わない場合は JDK 17、
Gradle wrapper、Xvfb、xdotool、ImageMagick、Mesa を自分で用意してください。

## テスト用ワールド

描画テストは「ワールドが既にある」ことを前提にしています。ワールドは
`versions/1.20.1/run/saves/RenderTest` に置きます（`run/` は Git 管理外です）。

### 1. ワールドを作る

```bash
cd versions/1.20.1
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew runClient
```

ゲーム内で **Singleplayer → Create New World** を選び、次の設定で作成します。

| 項目 | 値 |
|---|---|
| World Name | `RenderTest` |
| Game Mode | Creative |
| World Type | Default（Flat でも可） |
| Seed | `0`（任意。固定するとスクリーンショットが安定） |
| Allow Cheats | ON |

作成後、ワールドに入って一度終了します。以降は quick play で直接このワールドを開きます。

### 2. テスト用データパックを入れる

`run/saves/RenderTest/datapacks/igi-test/` に次の 3 ファイルを置きます。1.20.1 の
データパック形式（pack_format 15、`data/<namespace>/functions`）です。

`pack.mcmeta`

```json
{"pack":{"pack_format":15,"description":"InGameInfo render test"}}
```

`data/minecraft/tags/functions/tick.json`

```json
{"values":["igi:test"]}
```

`data/igi/functions/test.mcfunction`

```mcfunction
time set noon
weather clear
gamemode creative @a
effect give @a minecraft:speed 999999 2 true
effect give @a minecraft:night_vision 999999 0 true
effect give @a minecraft:strength 999999 1 true
effect give @a minecraft:regeneration 999999 0 true
item replace entity @a armor.head with minecraft:diamond_helmet
item replace entity @a armor.chest with minecraft:diamond_chestplate
item replace entity @a armor.legs with minecraft:diamond_leggings
item replace entity @a armor.feet with minecraft:diamond_boots
item replace entity @a weapon.mainhand with minecraft:diamond_sword
item replace entity @a weapon.offhand with minecraft:shield
```

この関数は tick ごとに実行され、時刻・天候・装備・効果を固定します。HUD の
「Day / Biome / Light / EXP / FPS / Entities / 手持ち / ポーション / 防具」を毎回同じ
状態で比較できるようにするためです。

ワールドを開いた状態でデータパックを追加した場合は、チャットで次を実行します。

```
/reload
/datapack list
/datapack enable "file/igi-test"
/function igi:test
```

`/datapack list` に `igi-test` が enabled と出れば有効です。ワールド作成前に
`datapacks/` へ置いた場合は、ワールド読み込み時に自動で有効になります。

### 3. 表示定義を確認する

初回起動で `run/config/InGameInfo.xml` が生成されます。HUD の既定表示はこのファイルで
決まります。テスト中にカスタム定義へ差し替えた場合は、退避してから削除するか、
元の内容に戻して `/igi reload` してください（既定ファイルは削除しても次回起動時に
再生成されます）。

設定値は `run/config/ingameinfoxml-client.toml` です。スクリーンショットを既定値で
揃えたい場合は、この TOML も退避してから削除します。

## HUD の自動テスト

`scripts/render-test.sh` が Xvfb を起動し、quick play でワールドに入り、指定秒数後に
ルートウィンドウを PNG に保存します。

```bash
cd /path/to/InGameInfo-reloaded
GRADLE_USER_HOME=/tmp/gradle-home \
  nix develop .#mc1201 --command scripts/render-test.sh 1.20.1 RenderTest
GRADLE_USER_HOME=/tmp/gradle-home \
  nix develop .#mc1211 --command scripts/render-test.sh 1.21.1 RenderTest
GRADLE_USER_HOME=/tmp/gradle-home \
  nix develop .#mc2612 --command scripts/render-test.sh 26.1.2 RenderTest
```

- 出力: `docs/render/hud-<version>.png`
- ログ: `versions/<version>/build/render-test.log`
- 待ち時間: 既定 35 秒。`HUD_DELAY=60` のように延ばせる
- 画面: 1280x720x24、GLX 有効、llvmpipe（ソフトウェア GL）

1.21.1 / 26.1.2 は NeoForge / ModDevGradle なので、Gradle の `--args` ではなく
`-PquickPlayWorld=RenderTest` でワールドを指定します。スクリプトは
`META-INF/neoforge.mods.toml` の有無で自動的に切り替えます。手動で起動する場合は
次のコマンドを実行します。

```bash
cd versions/1.21.1
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew --no-daemon runClient \
  -PquickPlayWorld=RenderTest

cd versions/26.1.2
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew --no-daemon runClient \
  -PquickPlayWorld=RenderTest
```

初回の 1.21.1 起動ではアクセシビリティの案内画面が出ます。`Continue` を押すと
ワールドに入り、次回以降は `options.txt` の `onboardAccessibility:false` で出ません。
26.1.2 で 1.21.1 のワールドをコピーした場合は、`Create a backup before upgrading
this world?` に `I know what I'm doing!`、続く `Upgrading World Completed` に `Yes`
を押します。ワールドは 1 回アップグレードすると 1.21.1 では開けなくなります。
アセットのダウンロードが止まる場合は、PrismLauncher などのキャッシュを
`/tmp/gradle-home/caches/neoformruntime/assets/objects/` へコピーすると再開できます。

スクリプトはクライアントを起動したあと `xdotool search --name "Minecraft"` で
ウィンドウを探し、見つかったら待ってから `import -window root` で撮ります。GPU も
ログインも不要です。

### トラブルシューティング

- **Minecraft ウィンドウが出ない**: ログの末尾を確認します。`glfwInit` や
  `Failed to create display` が出る場合は、Gradle デーモンが Xvfb の DISPLAY を
  引き継いでいないことが原因です。スクリプトは `--no-daemon` を付けています。
  手動で試すときも必ず `--no-daemon` を付けてください。
- **画面が真っ黒**: `LIBGL_ALWAYS_SOFTWARE=1`、`GALLIUM_DRIVER=llvmpipe`、
  `MESA_LOADER_DRIVER_OVERRIDE=llvmpipe` が dev shell で設定されているか確認します。
- **ワールドが見つからない**: `versions/<version>/run/saves/RenderTest` があるか、
  名前が一致しているか確認します。quick play は名前の一致に厳密です。
- **前回のプロセスが残っている**: `pgrep -af 'gradle|Minecraft'` を確認し、
  必要なら `pkill -f 'quickPlaySingleplayer RenderTest'` で止めます。
- **設定が残っていて表示が違う**: `run/config/InGameInfo.xml` と
  `run/config/ingameinfoxml-client.toml` を退避して再実行します。

## 目視確認のポイント

`docs/render/hud-1.20.1.png` で次を確認します。

1. 左上のテキストが 6 行程度表示され、`Day`・`Biome`・`Light`・`EXP`・`FPS`・
   `Entities` の値が入っている。
2. 黄色（`$e`）と白（`$f`）の色分けがされている。ピクセル色は目安として
   `#FCFC54`（黄）と `#FCFCFC`（白）。
3. 左下にメインハンド（ダイヤモンドの剣）とオフハンド（盾）のアイコン・耐久値が出る。
4. 右下に 4 つのポーション効果（Speed II / Night Vision / Strength II / Regeneration）が
   名前・倍率・残り時間・アイコン付きで並ぶ。
5. 防具 4 部位の耐久値とアイコンが重ならない。
6. HUD が二重に描画されていない（同じ行が 2 回出ていない）。

### 追加の描画確認（icon / item / pingicon）

`<icon>` のテクスチャ、`<item>` のアイテム、`pingicon` は既定定義には常時出ないため、
一時的な表示定義で確認します。`run/config/InGameInfo.xml` を退避し、`<icon>`、
`<item>`、`<pingicon>` を含む短い定義を置いて `/igi reload` したあと、HUD を撮ります。
確認後は元の定義に戻して `/igi reload` します。

1.20.1 では次の表示を確認済みです。

- `<icon>` の 8x8 / 16x16 テクスチャがにじまずに出る
- `<item>` で剣・石などのアイテムアイコンが出る
- `pingicon` が接続状態に応じて出る

## GUI の確認

GUI は HUD のスクリプトでは操作しないため、クライアントを手動で起動して確認します。
自動化する場合は `xdotool` でチャットを開き、コマンドを入力して `import` で撮ります。

```bash
cd versions/1.20.1
GRADLE_USER_HOME=/tmp/gradle-home \
  xvfb-run -a -s "-screen 0 1280x720x24 +extension GLX +render -noreset" \
  ./gradlew --no-daemon runClient --args="--quickPlaySingleplayer RenderTest"
```

### タグ一覧 GUI

1. ワールド内で `t` を押してチャットを開く。
2. `/igi taglist` を実行する。
3. 次の点を確認する。
   - 左端の行が画面外にはみ出していない（幅が画面内にクランプされている）
   - タグ名がアルファベット順で、エイリアスの重複表示がない
   - 検索欄にプレースホルダが出る。文字を入れると絞り込める
   - スクロールしても行や背景が欠けない
4. 終了は `Esc`。

![タグ一覧 GUI](render/taglist-1.20.1.png)

### 設定 GUI

1. `Esc` → **Mods** → **InGame Info XML** → **Config** を開く。
2. ページ 1/3 の **filename** 欄が画面内に収まっていることを確認する。
3. `>` で 2/3、3/3 へ進み、`<` で戻る。各ページの値が表示され、コンソールに
   `NullPointerException` が出ないことを確認する。
4. 3/3 のアライメント値（Top Right = `-2 2`、Middle Center = `0 0`、
   Bottom Right = `-2 -2` など）を確認する。
5. **Done** で閉じ、再度開いて値が保持されていることを確認する。

![設定 GUI 1/3](render/config-1.20.1.png)

設定画面の入力欄はページ切り替え時に再生成されます。1.20.1 では、ページ移動時に
古い `EditBox` を参照して `commit()` が NPE になる問題を修正済みです。

### HUD エディタ

1. ワールド内で `F9` を押す（またはチャットで `/igi editor`）。画面下にエディタが開き、
   上の HUD は表示されたままになる。
2. 行一覧で行を選び、テキスト欄を編集する。`Tag...` を押すとタグ一覧が開き、
   タグをクリックするとカーソル位置に `<tag>` が挿入されてエディタへ戻る。
3. 3x3 のアライメントボタンで表示位置を切り替え、`Offset` の X / Y を変えると
   HUD がその場で移動する。
4. `Save` で保存。ステータスが `Saved InGameInfo.xml` になり、
   `run/config/InGameInfo.xml` と `run/config/ingameinfoxml-client.toml` が更新される。
5. `Done` で閉じ、`F9` で開き直すと編集内容が残っている。
6. クライアントを再起動し、オフセットが保持されていることを確認する。

![HUD エディタ](render/editor-1.20.1.png)

![エディタのタグピッカー](render/editor-picker-1.20.1.png)

![オフセット変更後](render/editor-offset-1.20.1.png)

### デバッグ要素インスペクター

1. ワールド内で `F8` を押す（またはチャットで `/igi debug`）。
2. タイトル、`<タグ数> tags / <カテゴリ数> categories`、カテゴリ見出し、タグ名と値が
   表示されることを確認する。
3. 検索欄に `health` や `mek` などを入れて絞り込む。カテゴリ見出しの件数も
   絞り込み後の数になる。
4. ホイールでスクロールし、末尾の `Formatting` カテゴリまで到達できることを確認する。
5. タグをクリックし、クリップボードに `<タグ名>` が入ること、一覧の下に
   「`<...>` をコピーしました」と出ることを確認する。
6. MOD 連携を有効にしている場合は、そのカテゴリと値も表示されることを確認する。
7. `Esc` または `Done` で閉じる。

![デバッグ要素 1.20.1](render/debug-1.20.1.png)

![デバッグ要素 1.21.1](render/debug-1.21.1.png)

![デバッグ要素 26.1.2](render/debug-26.1.2.png)

1.20.1 / 1.21.1 / 26.1.2 のいずれも、Xvfb + llvmpipe の 1280x720 で開いて
スクリーンショットを確認済みです。追加 MOD なしのテスト環境では
`494 tags / 12 categories` でした。1.21.1 で Mekanism と Cobblemon を有効にすると
`632 tags` になり、連携タグが一覧に増えます。

#### xdotool による自動操作

- 1280x720 の Xvfb では、ゲームの描画領域は 854x480（GUI スケール 2）で、ルート
  ウィンドウの `(213, 120)` から始まる。GUI 座標 `(gx, gy)` は
  `(213 + gx * 2, 120 + gy * 2)` になる。
- ワールドが読み込まれるまで時間がかかる。ウィンドウが見つかってから **35 秒待って**
  から `F9` を送る（18 秒では早すぎてエディタが開かないことがある）。
- `EditBox` を消すときは `ctrl+a` を使わない（この環境では `a` が入力される）。
  フィールドをクリック → `End` → `BackSpace` を繰り返す。
- エディタの主な座標（画面座標）: 行一覧 1 行目 `(253, 380)`、テキスト欄
  `(533, 460)`、`Tag...` `(561, 500)`、`Save` `(567, 552)`、`Done` `(661, 552)`、
  X `(693, 492)`、Y `(777, 492)`。
- 設定変更後の保存では、TOML の再読み込みイベントが非同期に走る。保存直後に
  アライメントが戻る場合は、`ConfigurationHandler.onLoad` が `Reloading` で
  ライブ値を上書きしていないか確認する（1.20.1 では修正済み）。

## 計算式 `{{ ... }}` の確認

1. ワールドを開く前に `run/config/InGameInfo.xml` をバックアップする。
2. `<topleft>` に次のような行を入れて `/igi reload` する（XML なので `%<s` は
   `%&lt;s` と書く）。

   ```xml
   <str>arith = {{ 1 + 2 * 3 }}</str>
   <str>health+1 = {{ health + 1 }}</str>
   <str>fmt = {{ format("%.2f", 10 / 3) }}</str>
   <str>reuse = {{ format("%s %&lt;s", "A") }}</str>
   <str>tag = {{ biome }}</str>
   <str>fallback = {{ nope + 1 }}</str>
   ```

3. `arith = 7`、`health+1 = 21`（HP 20.00 のとき）、`fmt = 3.33`、
   `reuse = A A`、`tag = Plains` のように表示されることを確認する。
4. `fallback` の行は `{{ nope + 1 }}` のまま残る（式の失敗で行全体が消えない）。
5. 1.20.1 では、さらに四則演算の優先順位・括弧・単項符号・`round` / `min` /
   `max` / `abs` / `floor` / `ceil`・`format` の各変換子（`%05d`, `%,d`,
   `%x`, `%c`, `%e`, `%tY`, `%%` など）・`0` 除算・不正な式を確認する。

1.20.1 の 35 行の確認結果です。数値の既定書式（`10 / 4 = 2.5`、`10 / 3 = 3.333`）、
`format` の固定桁、タグとの加算、失敗時のリテラル保持が読み取れます。

![計算式 1.20.1](render/expressions-1.20.1.png)

1.21.1 と 26.1.2 では、代表的な 6 行で同じ結果を確認しました。

![計算式 1.21.1](render/expressions-1.21.1.png)

![計算式 26.1.2](render/expressions-26.1.2.png)

式の書式・関数・失敗時の仕様は [EXPRESSIONS.md](EXPRESSIONS.md) にまとめています。

## コマンドの確認

ワールド内でチャットから実行します。

```
/igi reload
/igi save roundtrip.xml
/igi save roundtrip.json
/igi save roundtrip.txt
```

`run/config/` に 3 ファイルができ、XML は `<config>`、JSON は `{`、TXT は
`<topleft>` で始まることを確認します。1.20.1 の既定定義では、それぞれ約 8.9 KB /
12.2 KB / 2.1 KB になりました。サイズは定義の内容で変わるため、絶対値ではなく
「空でない」「パースできる」ことを基準にします。

`/igi reload` はチャットに `Reloading...` と `Done!` を出します。F12（既定の
リロードキー）でも同じ処理が走ります。

## バージョン別の結果

### 1.20.1

2026-10-23〜24 時点のヘッドレス環境（Xvfb + llvmpipe、1280x720）で確認しました。

| 項目 | 結果 | スクリーンショット |
|---|---|---|
| 既定 HUD のテキスト・色 | OK | [hud-1.20.1.png](render/hud-1.20.1.png) |
| メイン / オフハンドのアイコンと耐久値 | OK | 同上 |
| ポーション 4 効果の名前・倍率・時間・アイコン | OK | 同上 |
| 防具 4 部位の耐久値・アイコン | OK | 同上 |
| `<icon>` / `<item>` / `pingicon`（一時定義） | OK | 一時ファイルのため未保存 |
| タグ一覧 GUI（検索・スクロール・画面内クランプ） | OK | [taglist-1.20.1.png](render/taglist-1.20.1.png) |
| 設定 GUI 1/3〜3/3 のページ移動 | OK | [config-1.20.1.png](render/config-1.20.1.png) |
| `/igi save` の XML / JSON / TXT 出力 | OK | ファイルサイズで確認 |
| `/igi load` の XML / JSON / TXT 入力 | OK | チャットの `Done!` と HUD で確認 |
| `/igi reload` | OK | チャットの `Reloading... / Done!` |
| HUD エディタの表示・ライブ HUD | OK | [editor-1.20.1.png](render/editor-1.20.1.png) |
| デバッグ要素インスペクター（F8、全タグ・検索・スクロール） | OK（2026-10-24） | [debug-1.20.1.png](render/debug-1.20.1.png) |
| 計算式 `{{ ... }}`（35 行、全関数・`format`・失敗時） | OK（2026-10-24） | [expressions-1.20.1.png](render/expressions-1.20.1.png) |
| タグピッカーから `<energy>` を挿入 | OK | [editor-picker-1.20.1.png](render/editor-picker-1.20.1.png) |
| エディタの `Save` で XML に 1 行保存 | OK | `InGameInfo.xml` の `<var>energy</var>` |
| X=5 / Y=7 の保存と再起動後の保持 | OK | [editor-offset-1.20.1.png](render/editor-offset-1.20.1.png) |
| `Done` で閉じて `F9` で再表示 | OK | 編集行が残ることを確認 |
| 不正な行（閉じていない `<`）で保存中止 | OK | ステータスが赤いエラー表示になる |

### 1.21.1（2026-10-23）

NeoForge 21.1.264 でも同じ HUD・GUI を確認しました。設定画面は Esc → `Mods` →
`InGame Info XML` → `Config` で開きます。

| 項目 | 結果 | スクリーンショット |
|---|---|---|
| 既定 HUD（テキスト・アイテム・ポーション・防具） | OK | [hud-1.21.1.png](render/hud-1.21.1.png) |
| タグ一覧 GUI（タイトル・検索・カテゴリ） | OK | [taglist-1.21.1.png](render/taglist-1.21.1.png) |
| 設定 GUI 1/3〜3/3 のページ移動 | OK | [config-1.21.1.png](render/config-1.21.1.png) |
| 設定 GUI の `Done` 後に再度開く | OK | 1/3 に戻り、値も保持 |
| HUD エディタ（背景がぼやけず HUD が読める） | OK | [editor-1.21.1.png](render/editor-1.21.1.png) |
| デバッグ要素インスペクター（`/igi debug`） | OK（2026-10-24） | [debug-1.21.1.png](render/debug-1.21.1.png) |
| 計算式 `{{ ... }}`（代表 6 行） | OK（2026-10-24） | [expressions-1.21.1.png](render/expressions-1.21.1.png) |
| `/igi save quiet.xml` | OK | チャットに `Done!` |
| `defaultSeedInServer` の補正ループ | 修正済み | TOML が `"0"` で安定、`.bak` なし |

1.21.1 では `Screen#render` が自分で背景を描くため、画面側で `renderBackground` を
重ねるとタイトルやラベルがぼやけます。`GuiTags` と `GuiConfigScreen` はウィジェットを
描いたあとに文字を描くよう修正済みです。HUD エディタは背景描画を無効にしています。

### 26.1.2（2026-10-23）

NeoForge 26.1.2.122 でも同じ HUD・GUI を確認しました。設定画面は Esc → `Mods` →
`InGame Info XML` → `Config` で開きます。

| 項目 | 結果 | スクリーンショット |
|---|---|---|
| 既定 HUD（テキスト・アイテム・ポーション・防具） | OK | [hud-26.1.2.png](render/hud-26.1.2.png) |
| タグ一覧 GUI（タイトル・検索・カテゴリ） | OK | [taglist-26.1.2.png](render/taglist-26.1.2.png) |
| 設定 GUI 1/3〜3/3 のページ移動 | OK | [config-26.1.2.png](render/config-26.1.2.png) |
| 設定 GUI の `Done` 後に再度開く | OK | 値も保持 |
| HUD エディタ（背景がぼやけず HUD が読める） | OK | [editor-26.1.2.png](render/editor-26.1.2.png) |
| デバッグ要素インスペクター（F8） | OK（2026-10-24） | [debug-26.1.2.png](render/debug-26.1.2.png) |
| 計算式 `{{ ... }}`（代表 6 行） | OK（2026-10-24） | [expressions-26.1.2.png](render/expressions-26.1.2.png) |
| `/igi save test2612.xml` | OK | チャットに `Done!` |
| 初回参加時の既定 HUD 表示 | 修正済み | `ConfigReloadListener` で解決 |
| `scripts/render-test.sh 26.1.2 RenderTest` の自動撮影 | OK | [hud-26.1.2.png](render/hud-26.1.2.png) |

26.1.2 では `GuiGraphics` が `GuiGraphicsExtractor` になり、`Screen#render` は
`extractRenderState`、選択リストの行は `extractContent` になります。テキスト色は
アルファを見るため、6 桁の RGB 指定だと文字が透明になります。GUI の色はすべて
`0xFFFFFFFF` などの ARGB に変更済みです。

ウィンドウは 854x480、ルート上の位置は (213,120)、GUI スケールは 2 です。
`import -window root` で 1280x720 の画像を撮ると、以前のバージョンと同じ構図に
なります。`import -window <Minecraft のウィンドウ ID>` を使うと 854x480 で撮れます。
Xvfb を再起動せずにクライアントだけ入れ替えると、前回の画面がルートに残って
誤ったスクリーンショットになることがあります。撮る前に必ずウィンドウを確認して
ください。

## MOD 連携

任意連携タグを実 MOD で確認した手順と結果を、MOD ごとにまとめます。開発クライアント
への MOD の入れ方は [DEBUG_MODS.md](DEBUG_MODS.md) を参照してください。

### Paragliders（1.20.1 / 1.21.1）

連携の実例として Paragliders を入れ、スタミナの 4 タグを HUD に出して確認しました。

- 1.21.1: `/tmp/mods1211/Paraglider-neoforge-21.1.5.jar` を `versions/1.21.1/run/mods/`
  に置くだけで動きます。ログは `Registered 551 tags.`（既定 547 + Paraglider 4）。
- 1.20.1: Forge の開発実行は公式 Mappings、本番 jar は SRG 名なので、そのまま入れると
  `NoSuchMethodError: ResourceLocation.m_135827_()` などで落ちます。一時的な init script で
deobf した jar を作ってから入れます。

```groovy
// /tmp/deobf-paraglider.init.gradle
allprojects { p ->
    p.afterEvaluate {
        if (p.path == ':') {
            p.repositories { flatDir { dirs '/tmp/mods1201' } }
            def cfg = p.configurations.create('runtimeTestMod')
            p.dependencies.add('runtimeTestMod', p.fg.deobf('test:Paraglider-forge:20.1.3'))
            p.tasks.register('copyRuntimeTestMod', Copy) {
                from cfg
                into p.file('run/mods')
            }
        }
    }
}
```

```bash
cd versions/1.20.1
GRADLE_USER_HOME=/tmp/gradle-home nix develop <repo>#mc1201 \
  --command ./gradlew copyRuntimeTestMod --init-script /tmp/deobf-paraglider.init.gradle --no-daemon
# run/mods に残った元の Paraglider-forge-20.1.3.jar を消し、*_mapped_official_1.20.1.jar だけ残す
```

HUD 設定に次の行を足して確認します。

```xml
<var>stamina</var> / <var>maxstamina</var> (<var>staminapencent</var>) depleted=<var>staminadepleted</var>
```

結果は 1.20.1 / 1.21.1 とも `1000 / 1000 (100.0%) depleted=false` で、
[integration-paraglider-1.20.1.png](render/integration-paraglider-1.20.1.png) と
[integration-paraglider-1.21.1.png](render/integration-paraglider-1.21.1.png) に残しています。

Mixin を含む本番 jar（例: Cold Sweat）は deobf しても refmap が SRG のままです。
1.20.1 の `build.gradle` は `mixin.env.remapRefMap` を設定しているため、Botania は
開発実行で読み込めました（後述）。Cold Sweat のように別の Mixin 問題が残る場合は
本番環境（実際の Forge クライアント）で確認します。

### Applied Energistics 2（1.20.1 / 1.21.1）

AE2 は Mixin を含む本番 jar のため、1.20.1 の ForgeGradle 開発実行では
`ae2.mixins.json:GuiGraphicsMixin` の refmap 解決に失敗します
（`m_280405_` が見つからない）。AE2 15.4.10 の Mixin は Forge 47.4.28 で追加された
`GuiGraphics#renderItem(LivingEntity, Level, ItemStack, ...)` を参照するため、
`versions/1.20.1/gradle.properties` も 47.4.28 に上げています。検証は
Minecraft 1.20.1 + Forge 47.4.28 の本番クライアントで行いました。

1.21.1 は NeoForge の開発実行に AE2 19.2.18 と GuideME 21.1.20 を
`versions/1.21.1/run/mods/` へ置くだけで動きます（`Registered 566 tags.` =
既定 547 + AE2 19）。

HUD 設定の例を次に示します。

```xml
net: <var>ae2networkover</var> ch: <var>ae2channelsover</var> mode: <var>ae2channelmodeover</var>
AE: <var>ae2gridpowerover</var>/<var>ae2gridmaxpowerover</var> use: <var>ae2gridpowerusageover</var>
cell: <var>ae2cellused</var>/<var>ae2celltotal</var> types <var>ae2celltypes</var> pct <var>ae2cellpercent</var>
status: <var>ae2cellstatus</var>
```

1.21.1 の確認結果は [ae2-1.21.1.png](render/ae2-1.21.1.png)。チャンネル数 1、
モード `default`、グリッド電力 922.3T/922.3T、使用量、セル 0/1024、`status: empty`
まで描画されています。タグ名を `over` 付きに変えたあとも、
[ae2-over-1.21.1.png](render/ae2-over-1.21.1.png) で新名
（`ae2networkover`, `ae2channelsover`, `ae2gridpowerover`）と旧名
（`ae2network`, `ae2channels`）が同じ値で並ぶことを確認しました。

複数ネットワークの切り分けは [ae2-targeting-1.21.1.png](render/ae2-targeting-1.21.1.png)。
上から、ドライブを含むネットワークを見た状態（`net: block ch: 1`）、周囲に
ネットワークがある状態で空を見た状態（`net: none ch: 0`）、コントローラーだけの
別ネットワークを見た状態（`net: block ch: 0`）です。見ているブロックのグリッドだけが
選ばれ、周辺のグリッドへフォールバックしないことを確認しています。

1.20.1 は本番クライアントで `/setblock` したクリエイティブエネルギーセル・コントローラー・
ドライブをクロスヘアで見て、[ae2-1.20.1.png](render/ae2-1.20.1.png) の
`net: block ch: 1 mode: default` / `AE: 922.3T/922.3T use: 5` を確認しました。
セル値は `/give TestPlayer ae2:item_storage_cell_1k` 後にメインハンドへ切り替えて
ログで `status=EMPTY used=0 total=1024` を確認していますが、自動操作ではセルが
メインハンドに残るのが 1 フレームだけのため、セル行の定常スクリーンショットは
撮れていません。手動でセルを持てば同じ値が HUD に出ます。

画像の色を機械的に確認する例を次に示します。

```bash
magick docs/render/hud-1.20.1.png -format %c histogram:info:- | sort -nr | head -20
```

`#FCFC54` と `#FCFCFC` が上位に出れば、少なくとも黄色・白の文字が描画されています。

エディタのスクリーンショットでは、下のパネルが画面外にはみ出さず、上に HUD が
残っていること、`Save` 後に `ingameinfoxml-client.toml` の `topleft` が
`"5 7"` になっていることを確認します。

### Mekanism / Cobblemon / Tinkers' Construct

ここで使う MOD の jar はリポジトリに含めません。`versions/<version>/libs/debug-mods/`
に置き、`build.gradle` の `ext.debugMods` を true にして開発クライアントへ読み込みます。
依存 MOD の自動解決や
On/Off の仕組みは [DEBUG_MODS.md](DEBUG_MODS.md) を参照してください。

#### 1.21.1（Mekanism + Cobblemon）

`versions/1.21.1/libs/debug-mods/` に次の 3 つを置きます。Cobblemon は Kotlin
ランタイムを同梱しないため、Kotlin for Forge も必要です（`cobblemon: true` なら
自動で一緒に読み込まれます）。

- `Mekanism-1.21.1-10.7.19.85.jar`
- `Cobblemon-neoforge-1.8.2+1.21.1.jar`
- `kotlinforforge-5.12.0-all.jar`

`build.gradle` の `ext.debugMods` を `cobblemon: true`, `mekanism: true` にしてから
起動します。

```bash
cd versions/1.21.1
./gradlew runClient
```

ログは `Registered 632 tags.`（Cobblemon + Mekanism + Kotlin for Forge の構成）。
ワールド内で `/clearparty @s` のあと
`/pokegive @s pikachu level=25` と `/pokegive @s charmander level=12` を実行すると、
HUD は次の値を表示しました（Cobblemon の `/pokegive` はレベルをプロパティとして
受け取るため、`/pokegive @s pikachu 25` ではレベル 1 になります）。

```text
mek: 1.00E-07 env 1.00E-07/1.00E-07 sev none on true pct 100.0
party 2
p1 Pikachu Lv.25 57/57
p1s Pikachu lv 25 hp 57/57 st none sh false ty electric
p1n Pikachu
```

Mekanism の個人線量・環境線量・最大値、深刻度、有効状態、割合と、Cobblemon の
パーティ数・種族・レベル・HP・状態・色違い・タイプがすべて描画されています。
Cobblemon のパーティ選択オーバーレイが画面左端に、`Change Party Selection` の
ツールチップが右上に出るため、この撮影だけ GUI スケールを 2、HUD の `topleft` を
`90 80` にずらしています。オーバーレイと重ならず、4 行すべて読み取れます。
確認画像: [integration-mods-1.21.1.png](render/integration-mods-1.21.1.png)。

#### 1.20.1（Tinkers' Construct）

Tinkers' Construct 3.12.1.231 と Mantle 1.11.117 の本番用 jar は SRG 名で
コンパイルされているため、そのまま `run/mods` に置くと `ResourceLocation.m_135827_()`
や `f_256747_` の解決に失敗します（AE2 と同じ制約）。Tinkers / Mantle の jar には
Mixin 設定も refmap も含まれていないため（`unzip -l` で確認）、追加の Mixin 設定
ファイルは不要です。原因は Mixin ではなく SRG 名です。ForgeGradle の `fg.deobf`
を通す `debug-mods.gradle` の経路で読み込みます。

`build.gradle` の `ext.debugMods` を `tconstruct: true` にしてから起動します。

```bash
cp TConstruct-1.20.1-3.12.1.231.jar Mantle-1.20.1-1.11.117.jar \
  versions/1.20.1/libs/debug-mods/
cd versions/1.20.1
./gradlew runClient --args="--quickPlaySingleplayer RenderTest"
```

`syncDebugMods` が `TConstruct-1.20.1-3.12.1.231_mapped_official_1.20.1.jar` と
`Mantle-1.20.1-1.11.117_mapped_official_1.20.1.jar` を `run/mods` にコピーし、
ログに `[igi] Debug mods: tconstruct, mantle` を出します。ゲーム内で
`/give @s tconstruct:pickaxe` を持った状態の HUD は次のとおりです。

```text
tool: 1/1 (100.0%)
speed 1.00 atk 0.00 harvest 0
mods 0 broken false
```

8 タグすべてが描画され、耐久値・最大耐久値・割合・採掘速度・攻撃力・採掘レベル・
修飾子数・破損状態が Tinkers の `ToolStack` から読めています。`/give` で作った
ツールは素材データを持たないため数値は最小値です。素材入りのツールでの値の確認は
今後の課題です。本番用クライアント（`.prism-test/mcprod`）でも同じ表示を確認して
います。確認画像は次のとおりです。
[integration-tinkers-1.20.1.png](render/integration-tinkers-1.20.1.png)。

### Botania（1.20.1）

Botania のクロスヘア先ブロックのマナを読む 9 タグを、1.20.1 の ForgeGradle 開発
クライアントで確認しました。対象はマナプール、マナスプレッダー、生成花／機能花です。

`versions/1.20.1/libs/debug-mods/` に次の 3 jar を置き、`build.gradle` の
`ext.debugMods` を `botania: true` にします。Patchouli と Curios は Botania の必須
依存として自動で追加されます。

```text
Botania-1.20.1-446-FORGE.jar
Patchouli-1.20.1-84-FORGE.jar
curios-forge-5.9.1+1.20.1.jar
```

```bash
cd versions/1.20.1
./gradlew listDebugMods
./gradlew runClient --args="--quickPlaySingleplayer RenderTest"
```

`listDebugMods` は `[ON] botania`、`[AUTO] patchouli`、`[AUTO] curios` を表示し、
ログは `[igi] Debug mods: botania, patchouli, curios`、`Registered 563 tags.`
（既定 547 + Botania 12 + Curios 4）になります。デバッグ要素インスペクターでは
`508 tags / 14 categories` と Botania カテゴリの 12 タグを確認できます。

本番 jar の Mixin refmap は SRG 名のため、そのままでは
`InvalidAccessorException: No candidates were found matching f_91041_...` で起動に
失敗します。1.20.1 の `build.gradle` は `mixin.env.remapRefMap` と
`mixin.env.refMapRemappingFile` を設定済みで、`build/createSrgToMcp/output.srg` を
使って公式名に読み替えます。jar を書き換える必要はありません。

HUD 設定は次のとおりです。

```xml
pool: <var>botaniamanapoolover</var>/<var>botaniamanapoolmaxover</var> (<var>botaniamanapoolpercentover</var>%)
spreader: <var>botaniaspreadermanaover</var>/<var>botaniaspreadermaxmanaover</var> (<var>botaniaspreaderpercentover</var>%)
flower: <var>botaniaflowermanaover</var>/<var>botaniaflowermaxmanaover</var> (<var>botaniaflowerpercentover</var>%)
```

テスト用ブロックを置き、クロスヘアがブロックに当たるよう視点を合わせます。

```mcfunction
/setblock ~3 ~1 ~ botania:mana_pool{mana:500000,manaCap:1000000}
/setblock ~3 ~1 ~ botania:mana_spreader{mana:400}
/setblock ~3 ~1 ~ botania:endoflame{mana:100}
/tp @s ~ ~ ~ -90 8
```

マナプールはブロックの高さに合わせて pitch を 8 前後にしないとクロスヘアが外れ、
`0/0 (0.0%)` になります。スプレッダーと花も、十字カーソルがブロックに当たって
いることを確認してください。

確認結果は次のとおりです。

```text
pool: 500000/1000000 (50.0%)
spreader: 400/1000 (40.0%)
flower: 100/300 (33.3%)
```

3 つの画面を上下に並べた確認画像を次に示します。
[botania-tags-1.20.1.png](render/botania-tags-1.20.1.png)。上からマナプール、
マナスプレッダー、エンドフレイム。各画面とも対象ブロックにクロスヘアが当たり、
対応する行だけが値を持ち、他の行は `0/0 (0.0%)` です。

## 新しいバージョンへの展開

- バージョンごとに `versions/<version>/run/saves/RenderTest` を用意する
  （ワールドはバージョン間で共有しない。データパックの形式も変わる。コピーして
  アップグレードする場合は元のワールドのバックアップを残す）
- 1.21.1 以降はデータパックのディレクトリが `data/<ns>/function`（単数形）に変わる。
  そのバージョンの `pack_format` に合わせる
- `scripts/render-test.sh` の第 1 引数にバージョンを渡す
  （例: `scripts/render-test.sh 26.1.2 RenderTest`）
- 出力は `docs/render/hud-<version>.png` に分かれる
- GUI の描画 API・Config Screen の実装が変わるため、GUI の確認はバージョンごとに
  やり直す

## 参考

- [アーキテクチャ](ARCHITECTURE.md)
- [1.12.2 との互換性](COMPATIBILITY.md)
- [リポジトリ README（英語）](../README.md) / [日本語版](../README_ja.md)
