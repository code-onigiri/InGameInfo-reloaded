# 1.12.2 との互換性

このプロジェクトは「1.12.2 版と同じ設定ファイルで、同じ HUD になる」ことを第一目標に
しています。ただし 1.20.1 以降で削除された概念（数値 ID、ブロック metadata、クライアント
側のワールドファイルサイズなど）は再現できないため、タグ名を維持したまま互換値を
返します。

## 表示定義ファイル

- 既定ファイル名は `InGameInfo.xml`（1.12.2 と同じ）。`config/` 直下に置きます。
- XML の要素名・属性・`$0`〜`$f` / `$r` の色コード・`{ICON|...}` プレースホルダは
  そのまま解釈します。
- JSON / TXT パーサーも移植しています。TXT の `<if[cond[then/else]]>` や
  `<min[a/b[c/d]]>` も同じ文法です。
- `/igi save` は XML / JSON / TXT を拡張子で選びます。XML へ保存すると要素名は
  正規名（`operation` ではなく `op`、エイリアスは展開）になり、元ファイルと
  バイト単位では一致しません。読み込み互換は維持されます。
- 出荷する既定定義は、1.12.2 の 304 行版ではなく、DF11ZomGraves 版で修正済みの
  312 行版です（`mainhandicon` などのプレースホルダ修正、`effectcount` 行を含む）。
  1.12.2 の既定定義をそのまま使いたい場合は `config/InGameInfo.xml` にコピーすれば
  動作します。

## 設定

1.12.2 の `Configuration` は Forge / NeoForge の TOML
（`config/ingameinfoxml-client.toml`）になりました。キー名と既定値は可能な限り合わせて
います。以下の表は 1.20.1 / 1.21.1 / 26.1.2 で共通です。

| 1.12.2 | 1.20.1 | 備考 |
|---|---|---|
| `filename` | `filename` | 既定 `InGameInfo.xml` |
| `replaceDebug` | `replaceDebug` | F3 表示の置き換え。1.12.2 と同じ判定に戻した |
| `showInChat` | `showInChat` | チャット画面でも表示 |
| `showOnPlayerList` | `showOnPlayerList` | Tab 押下中は非表示 |
| `scale` | `scale` | 0.5 / 1.0 / 1.5 / 2.0 のみ有効。他は警告して 1.0 |
| `fileInterval` | `fileInterval` | 1〜60 秒 |
| `showOverlayPotions` | `showOverlayPotions` | バニラのポーションアイコン |
| `showOverlayItemIcons` | `showOverlayItemIcons` | アイテムの耐久バー |
| （なし） | `numericAmplifier` | 追加。ポーション倍率を `II` ではなく `2` で出す |
| （なし） | `sendSeedToChat` | 追加。シードをチャットへ |
| （なし） | `seed` | 追加。マルチでサーバーが応答しない場合の表示用 |
| （なし） | `showSurvivalHUD` | 追加。体力・空腹・酸素などのバニラ HUD |
| （なし） | `healthDigit` ほか | 追加。小数桁数 |
| （なし） | `displayInvisiblePlayer` | 追加。近傍プレイヤーに透明プレイヤーを含める |
| `alignment.*` | `alignment.*` | `"X Y"` 形式。9 か所すべて |

アライメントの値は HUD エディタからも変更できます。保存すると TOML に書き込まれ、
次回起動時に読み込まれます。

### 1.21.1 固有の設定差

- 設定ファイルの場所・キー名・既定値は 1.20.1 と同じです。
- `defaultSeedInServer` だけは、1.21.1 では文字列（`"0"`）として保存されます。
  TOML の整数は 32bit で、NeoForge の設定検証が `Long` と一致せずファイルを毎回
  書き換えてしまうためです。1.20.1 の TOML をそのまま 1.21.1 へコピーすると
  このキーが補正されます（他のキーと HUD 表示には影響しません）。
- Config Screen は NeoForge の `IConfigScreenFactory` 拡張ポイントで開きます。
  Mods 画面の Config ボタンから同じ画面になります。

### 26.1.2 固有の設定差

- 設定ファイルの場所・キー名・既定値は 1.21.1 と同じです。`defaultSeedInServer` は
  1.21.1 と同じく文字列（`"0"`）で保存します。
- Config Screen は 1.21.1 と同じ `IConfigScreenFactory` です。
- 画面描画 API が `GuiGraphicsExtractor` に変わったため、カスタム GUI の文字色は
  アルファ込みの ARGB（`0xFFFFFFFF` など）で指定しています。6 桁の RGB 指定は
  透明になり、文字が見えません。
- `pack.mcmeta` は `pack_format` に加えて `min_format` / `max_format` が必須です
  （26.1.2 は 84）。

1.12.2 の設定 GUI は Forge の Config Screen に置き換えました（Mods 画面 →
InGame Info XML → Config）。GUI の項目名は `config/ingameinfoxml-client.toml` の
コメントとゲーム内言語ファイルで確認できます。

## タグ

1.12.2 のタグ名・エイリアスはすべて登録されています（存在しない値を除く）。
1.20.1 / 1.21.1 / 26.1.2 の既定環境（連携 MOD なし）では 547 タグ（エイリアスと
Forge / NeoForge Energy 連携を含む）。
連携 MOD を入れると、その MOD のタグが追加されます。

### 値が変わるタグ

| タグ | 1.12.2 | 1.20.1 | 理由 |
|---|---|---|---|
| `biomeid` | `Biome.getIdForBiome` の数値 | レジストリ ID | 数値バイオーム ID は廃止。ID はワールド／データパックで変わる |
| `dimension` | `DimensionType.getName()` | `overworld` / `the_nether` / `the_end`、MOD はパス | 1.12.2 の内部名に合わせた |
| `dimensionid` | `player.dimension` | バニラは 0 / -1 / 1、MOD は 0 | 数値 dimension ID は廃止 |
| `worldname` | `WorldInfo.getWorldName()` | 統合サーバーのレベル名、マルチはサーバー名、不明は `?` | クライアントはリモートのレベル名を保持しない |
| `worldsize` / `worldsizemb` | `WorldInfo.sizeOnDisk` | 統合サーバーのみ実測（5 秒キャッシュ）、マルチは 0 | クライアントからリモートのディスクサイズは見えない |
| `nextweatherchange` / `nextrain` | 統合サーバーのみ、それ以外 `?` | 同じ。統合サーバーのみ | サーバー側の天候カウンタを参照 |
| `localtemperature` | `Biome.getTemperature(pos) * 100` | バイオームの基準温度 `* 100` | 1.20.1 に位置依存の `getTemperature(pos)` がない |
| `humidity` | `Biome.getRainfall() * 100` | `getModifiedClimateSettings().downfall() * 100` | 気候設定 API へ移行 |
| `mouseoverid` | ブロックの数値レジストリ ID | ブロックのレジストリ ID | 数値 ID は廃止。MOD 追加ブロックでは値が変わる |
| `mouseovermetadata` | ブロック metadata | 常に `0` | BlockState に metadata はない |
| `mouseovername`（ブロック） | pick block の表示名 | `getCloneItemStack(...).getHoverName()` | 同等の表示名を得る API へ移行 |
| `resourcepack` | 最初の選択パック名 | `vanilla` / `mod_resources` を除く最初のパック名、なければ `vanilla` | 1.20.1 のパック構成に合わせた |
| `entitiesrendered` / `entitiestotal` | RenderGlobal のカウント | F3 の `E: n/m` 文字列を解析。取れなければ 0 | 直接アクセスできる API がない |
| `maxairticks` | 常に 300 | 常に 300 | 1.12.2 と同じ固定値 |
| `eating` | 常に false（元実装の TODO） | 常に false | 元実装に合わせた |
| `loadedchunks` | 常に -1（元実装の TODO） | 常に -1 | 元実装に合わせた |
| `potionduration`（無限） | `**:**` | `**:**` | 同じ |
| `temperature` | `getTemperature() * 100` を `%.0f` | 同じ | 同じ |

### アイテム・アイコンの互換値

- `<icon>` のサイズ 1 / 2 の 2 番目の引数（metadata）は受け付けますが無視します。
  1.20.1 のアイテムに metadata はありません。
- `<itemquantity>` の 2 番目の引数（metadata）も同様に無視します。
- アイテム ID は `minecraft:stone` 形式のレジストリ名を使います。1.12.2 の数値 ID は
  指定できません。
- `<img>` / `<icon>` のテクスチャパスは `ResourceLocation` としてそのまま使えます。

### 追加タグ（1.12.2 にはない）

`absorption`, `armortoughness`, `arrowcount`, `effectcount`, `forgeversion`,
`freezing`, `inventory`, `javaversion`, `lookingblockxyz`, `mcversion`,
`mcpversion`, `modsactive`, `modstotal`, `mouseoverisblock`, `mspt`,
`nearbyplayerabsorption`, `nearbyplayerhealth`, `nearbyplayermaxhealth`,
`playericon`, `targethealth`, `targetmaxhealth`, `tps`。

これらは DF11ZomGraves 版で追加されたものです。1.12.2 の定義ファイルに影響は
ありません（未使用なら無視されます）。

### MOD 連携タグ（1.20.1 / 1.21.1 / 26.1.2 で追加）

次のタグは、対応 MOD が入っている場合だけ登録されます。MOD がない環境では
`{createrpmover}` のような未知のタグとして扱われ、HUD にはそのまま残ります
（1.12.2 の未登録タグと同じ挙動）。クロスヘア先のブロックを優先して読むタグは
末尾に `over` を付けます。旧名は別名として残しているため、既存の設定はそのまま動きます。

- Create: `createrpmover`, `createkineticover`, `createstressover`,
  `createstresscapacityover`, `createstresspercentover`, `createnetworksizeover`
  （旧名 `createrpm`, `createspeed`, `kineticrpm`, `createkinetic`, `kinetic`,
  `createstress`, `createstresscapacity`, `createstresspercent`, `createnetworksize` は別名）
- Curios: `curioslots`, `curioslotcount`, `curioitems`, `curios`
- Serene Seasons: `season`, `subseason`, `seasonphase`, `tropicalseason`, `seasonday`,
  `seasonticks`, `seasonprogress`, `seasonpercent`
- Ars Nouveau: `arsmana`, `arsmaxmana`, `arsmanapercent`
- Iron's Spells 'n Spellbooks: `ironsmana`, `ironsspellman`, `ironsmaxmana`,
  `ironsmanapercent`
- Tough As Nails: `thirst`, `hydration`, `temperature`, `bodytemperature`,
  `temperaturelevel`
- Vampirism: `blood`, `maxblood`, `bloodpercent`, `vampirelevel`
- Cold Sweat: `coldsweat`（体温）, `coldsweatworld`（環境温度）
- Thirst Was Taken: `twtthirst`, `twtquenched`, `twtexhaustion`
  （Tough As Nails と衝突しないよう `twt` 接頭辞）
- Goety: `goetysouls`, `goetyactive`
- Paragliders: `stamina`, `maxstamina`, `staminapencent`, `staminadepleted`
- Botania（1.20.1 のみ）: `botaniamana`, `botaniamaxmana`, `botaniamanapercent`
- Applied Energistics 2: `ae2networkover`, `ae2channelsover`, `ae2channelmodeover`,
  `ae2gridpowerover`, `ae2gridmaxpowerover`, `ae2gridpowerusageover`,
  `ae2gridpowerinjectionover`, `ae2channelpowerover`, `ae2cellused`, `ae2celltotal`,
  `ae2celltypes`, `ae2cellmaxtypes`, `ae2cellcount`, `ae2cellpercent`,
  `ae2cellstatus`, `ae2power`, `ae2maxpower`, `ae2powerpercent`
  （旧名 `ae2network`, `ae2channels`/`ae2channel`, `ae2channelmode`, `ae2gridpower`,
  `ae2gridmaxpower`, `ae2gridpowerusage`, `ae2gridpowerinjection`, `ae2channelpower` は別名）
- Mekanism: `mekanismradiation`, `mekanismradiationlevel`, `mekanismradiationmax`,
  `mekanismradiationseverity`, `mekanismradiationenabled`, `mekanismradiationpercent`
  （個人線量と環境線量。1.21.1 は Attachment、1.20.1 は Capability を自動で使い分け）
- Cobblemon: `cobblemonpartysize`、`cobblemonparty1`〜`cobblemonparty6`
  （コンパクト表示）、`cobblemonpartyNspecies`（別名 `...Nname`）、
  `...Nnickname`, `...Nlevel`, `...Nhp`（別名 `...Nhealth`）,
  `...Nmaxhp`（別名 `...Nmaxhealth`）, `...Nstatus`, `...Nshiny`, `...Ntype`。
  コンパクト表示 `cobblemonpartyN` の別名は `...Nsummary`。
- Tinkers' Construct（1.20.1 のみ）: `tinkersdurability`, `tinkersmaxdurability`,
  `tinkersdurabilitypercent`, `tinkersminingspeed`, `tinkersattackdamage`,
  `tinkersharvestlevel`, `tinkersmodifiers`, `tinkersbroken`
- Forge / NeoForge Energy（常時）: `energy`, `fe`, `maxenergy`, `maxfe`,
  `energypercent`, `fepercent`

1.21.1 の実クライアントで Mekanism と Cobblemon、1.20.1 の本番 Forge クライアントで
Tinkers' Construct の HUD 表示を確認済みです。1.20.1 の Mekanism / Cobblemon と
26.1.2 の 3 連携はビルド確認までです。

Create のタグは対象ブロックがない場合も行を消さず、RPM・ストレス系は `0.0`、
`createkineticover` は `false` を返します。搭乗中の Create 装置がある場合はその中の
運動ブロックを読みます。

値の評価はすべてクライアント側で行い、連携 MOD の API が変わって例外になっても
そのタグだけ空文字になります。HUD 全体は描画され続けます。

Paragliders は 1.20.1 / 1.21.1 の実クライアントで、スタミナ・最大値・割合・
枯渇状態が HUD に出るところまで確認しました。他の連携はコンパイル確認までです。

Applied Energistics 2 はサーバー権威の値（チャンネル数、グリッド電力、使用量）を
HUD に出すため、クライアントから `RequestAe2Packet` を送り、サーバーが
`ResponseAe2Packet` で返す 1 秒周期の往復で更新します。対象は次の順で決まります。

1. クロスヘア先の AE2 ブロック（8 ブロック以内）が属するグリッド
2. クロスヘア先に AE2 グリッドがない場合、インベントリ内のリンク済みワイヤレス端末のグリッド
3. どちらもない場合は `ae2networkover` が `none`、電力系は 0

複数のネットワークが近くにあっても、見ているブロックのグリッドだけを表示します。
周辺のグリッドを勝手に選ぶフォールバックはありません。セル系はメインハンド／
オフハンドのセルアイテムから読みます。

1.21.1 の実クライアントでは、チャンネル数・チャンネルモード・グリッド電力・
平均使用量・セル容量／状態まで HUD で確認済みです。1.20.1 は本番 Forge 47.4.28
クライアントでネットワーク系タグを確認済みで、セル値もログで
`status=EMPTY used=0 total=1024` を確認しましたが、自動操作中にセルがメインハンドに
残る時間が短く、セル行の定常 HUD スクリーンショットは撮れていません。26.1.2 は
コンパイル確認までです。

1.20.1 Forge の開発実行に本番 jar をそのまま入れると SRG 名の Mixin や
`ResourceLocation` のマッピング差で失敗するため、検証時は本番クライアント
（手動インストールした Forge 47.4.28）を使いました。手順は
[RENDER_TEST.md](RENDER_TEST.md) にあります。

### HUD エディタ（1.20.1 / 1.21.1 / 26.1.2 で追加）

1.12.2 にはない GUI です。`F9` または `/igi editor` で開きます。行は
`config/InGameInfo.xml` に、アライメントの X / Y は
`config/ingameinfoxml-client.toml` に保存します。`Esc` は保存せずに閉じます。
エディタで保存したオフセットは再起動後も保持されます。TOML を直接編集した場合は
ゲームを再起動してください（実行中の `Reloading` はライブのアライメントを上書き
しないため）。

### ポーション

- インデックスは 0〜31（1.12.2 と同じ 32 個）。
- 効果名の倍率接尾辞は ` II` / ` III` / ` IV`（1.12.2 と同じ）。
- `numericAmplifier = true` のときだけ `名前 2` の形式。
- 効果が 32 個を超える場合、33 個目以降は表示できません（元実装と同じ制限）。

## 修正した既知のバグ

1.12.2 と DF11ZomGraves 版にあった次のバグは、見た目が変わることを承知で修正して
います。

- HUD の二重描画（`InGameInfoCore` が行情報を 2 回追加していた）。
- ポーションアイコンの Y 座標の二重加算。
- `mouseoverpowerstrong` が weak power と同じ実装だった。
- `temperature` が 0.8 のような生値のままだった（`* 100` に修正）。
- `gamemode` の翻訳キーが `gameMode.` になっていた（`selectWorld.gameMode.` に修正）。
- 体力・空腹の丸めが `CEILING` だった（`HALF_UP` に修正）。
- タグ一覧 GUI が狭い画面で左にはみ出していた（行幅を画面内にクランプ）。
- タグ一覧 GUI でエイリアスが同名で重複表示されていた（名前で重複排除し、
  アルファベット順に整列）。検索欄にプレースホルダを追加。
- 設定 GUI のファイル名欄が狭い画面で右にはみ出していた（幅をクランプ）。
- `InfoText` の色を ARGB の alpha 255 込みで明示（`0xFFFFFFFF`）。

## 未移植・対象外

- 1.12.2 時代の Blood Magic / Simply Jetpacks / TerraFirmaCraft / Thaumcraft 連携タグ。
  1.20.1 に対応する MOD がない、または API が大きく変わったためです。
  これらは [MOD 連携 SPI](ARCHITECTURE.md) で後から追加できます。
- 上記の代わりに、1.20.1 / 1.21.1 / 26.1.2 で現役の Create / Curios / Serene Seasons /
  Ars Nouveau / Iron's Spells 'n Spellbooks / Tough As Nails / Mekanism / Cobblemon /
  Tinkers' Construct（1.20.1 のみ）/ Forge Energy を任意連携として追加しました
  （MOD が入っている場合のみ有効）。
- 人気度の高い MOD から、プレイヤー固有の資源を表示する連携を追加しました:
  Vampirism（血）, Cold Sweat（体温）, Thirst Was Taken（渇き）,
  Goety（ソウル）, Paragliders（スタミナ）, Botania（マナ、1.20.1 のみ）。
- 1.12.2 の `mcmod.info`、Forge 1.12 の `Configuration` GUI。
- 1.12.2 のアップデートチェック IMC（配布チャネルが異なるため）。

## 描画の確認

`scripts/render-test.sh` で Xvfb + llvmpipe の実クライアントを起動し、HUD を
スクリーンショットに保存します。確認済みの項目は [RENDER_TEST.md](RENDER_TEST.md)
に記録しています。
