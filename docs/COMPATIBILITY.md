# 1.12.2 との互換性

このプロジェクトは「1.12.2 版と同じ設定ファイルで、同じ HUD になる」ことを第一目標に
しています。ただし 1.20.1 で削除された概念（数値 ID、ブロック metadata、クライアント
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

1.12.2 の `Configuration` は Forge の TOML（`config/ingameinfoxml-client.toml`）に
なりました。キー名と既定値は可能な限り合わせています。

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

1.12.2 の設定 GUI は Forge の Config Screen に置き換えました（Mods 画面 →
InGame Info XML → Config）。GUI の項目名は `config/ingameinfoxml-client.toml` の
コメントとゲーム内言語ファイルで確認できます。

## タグ

1.12.2 のタグ名・エイリアスはすべて登録されています（存在しない値を除く）。
1.20.1 の既定環境（連携 MOD なし）では 547 タグ（エイリアスと Forge Energy 連携を含む）。
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

### MOD 連携タグ（1.20.1 で追加）

次のタグは、対応 MOD が入っている場合だけ登録されます。MOD がない環境では
`{createrpm}` のような未知のタグとして扱われ、HUD にはそのまま残ります
（1.12.2 の未登録タグと同じ挙動）。

- Create: `createrpm`, `createspeed`, `kineticrpm`, `createkinetic`, `kinetic`,
  `createstress`, `createstresscapacity`, `createstresspercent`, `createnetworksize`
- Curios: `curioslots`, `curioslotcount`, `curioitems`, `curios`
- Serene Seasons: `season`, `subseason`, `seasonphase`, `tropicalseason`, `seasonday`,
  `seasonticks`, `seasonprogress`, `seasonpercent`
- Ars Nouveau: `arsmana`, `arsmaxmana`, `arsmanapercent`
- Iron's Spells 'n Spellbooks: `ironsmana`, `ironsspellman`, `ironsmaxmana`,
  `ironsmanapercent`
- Tough As Nails: `thirst`, `hydration`, `temperature`, `bodytemperature`,
  `temperaturelevel`
- Forge Energy（常時）: `energy`, `fe`, `maxenergy`, `maxfe`, `energypercent`, `fepercent`

値の評価はすべてクライアント側で行い、連携 MOD の API が変わって例外になっても
そのタグだけ空文字になります。HUD 全体は描画され続けます。

### HUD エディタ（1.20.1 で追加）

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
- 上記の代わりに、1.20.1 で現役の Create / Curios / Serene Seasons / Ars Nouveau /
  Iron's Spells 'n Spellbooks / Tough As Nails / Forge Energy を任意連携として
  追加しました（MOD が入っている場合のみ有効）。
- 1.12.2 の `mcmod.info`、Forge 1.12 の `Configuration` GUI。
- 1.12.2 のアップデートチェック IMC（配布チャネルが異なるため）。

## 描画の確認

`scripts/render-test.sh` で Xvfb + llvmpipe の実クライアントを起動し、HUD を
スクリーンショットに保存します。確認済みの項目は [RENDER_TEST.md](RENDER_TEST.md)
に記録しています。
