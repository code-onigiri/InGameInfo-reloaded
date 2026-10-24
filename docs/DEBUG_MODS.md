# デバッグ用 MOD の On/Off

開発クライアントで任意の MOD を一時的に有効化するための仕組みです。MOD の jar は
CurseForge Maven から取得し、リポジトリには含めません。`ext.debugMods` で true に
した MOD とその依存だけが `run/mods` にコピーされ、前回の選択は次回の起動時に
自動で外れます。

有効化した MOD の連携タグが登録されているかは、ゲーム内で `F8` または
`/igi debug` を押して開く[デバッグ要素インスペクター](DEBUG_SCREEN.md)で確認できます。

## 使い方

有効化する MOD は、バージョンごとの `build.gradle` にある `ext.debugMods` の
true/false で切り替えます。コマンドライン引数は使いません。

```groovy
// versions/1.20.1/build.gradle
ext.debugMods = [
        tconstruct: true,   // Tinkers' Construct（Mantle は自動）
        cobblemon: false,
        mekanism: false,
        ae2: false,
        botania: false,     // Botania（Patchouli / Curios は自動）
]
```

```bash
cd versions/1.20.1

# カタログの一覧と現在の ON/AUTO/off を確認
./gradlew listDebugMods

# カタログの全 jar を libs/debug-mods に取得
./gradlew downloadDebugMods

# ext.debugMods が true の MOD を run/mods に同期して起動
./gradlew runClient

# 起動せず同期だけ実行（すべて false なら前回の選択を削除）
./gradlew syncDebugMods
```

`runClient` と `runServer` は起動前に `syncDebugMods` を実行します。初回は
`downloadDebugMods` がカタログ内の全 jar を取得するため、Cobblemon など大きな
MOD を含めると時間がかかります。2 回目以降は Gradle のキャッシュを使います。

`ext.debugMods` を省略した場合や全項目が false の場合は、前回の選択だけが外れます。
MOD を増やすときは `gradle/debug-mods.gradle` のカタログに別名と座標を追加し、
`ext.debugMods` にキーを足してください。

## カタログと保存先

カタログは `gradle/debug-mods.gradle` の `catalogByVersion` にあります。座標は
`curse.maven:<descriptor>-<projectId>:<fileId>` で、file ID を固定してテスト済みの
組み合わせを再現します。

| 別名 | 1.20.1 | 1.21.1 | 26.1.2 |
|---|---|---|---|
| `tconstruct` | Tinkers' Construct | - | - |
| `mantle` | Mantle | - | - |
| `cobblemon` | Cobblemon | Cobblemon | - |
| `kotlinforforge` | Kotlin for Forge | Kotlin for Forge | - |
| `mekanism` | Mekanism | Mekanism | - |
| `ae2` | Applied Energistics 2 | Applied Energistics 2 | Applied Energistics 2 |
| `guideme` | GuideME | GuideME | GuideME |
| `botania` | Botania | - | - |
| `patchouli` | Patchouli | - | - |
| `curios` | Curios | - | - |

選択した MOD の依存はカタログの `deps` に従って自動で追加されます。

- `tconstruct` → `mantle`
- `cobblemon` → `kotlinforforge`
- `ae2` → `guideme`
- `botania` → `patchouli`, `curios`

```text
versions/1.20.1/libs/debug-mods/   # 取得した jar（Git 管理外）
versions/1.21.1/libs/debug-mods/
versions/26.1.2/libs/debug-mods/
```

`*.jar` は `.gitignore` で除外されています。`run/mods` にコピーしたファイル名は
`run/mods/.igi-debug-mods` に記録され、次回の同期時にそのファイルだけが削除されます。
手動で置いた jar や他の用途の jar は削除しません。

## Forge と NeoForge の違い

- **1.20.1 / Forge**: 本番用 MOD jar は SRG 名でコンパイルされています。そのまま
  `run/mods` に置くと `NoSuchFieldError: f_256747_` や
  `NoSuchMethodError: ResourceLocation.m_135827_()` になります。`debug-mods.gradle`
  は選択した jar を `fg.deobf` した `*_mapped_official_1.20.1.jar` に変換してから
  `run/mods` にコピーします。1.20.1 の `build.gradle` は
  `ext.igiForgeDeobf = true` を設定しています。
- **1.21.1 / 26.1.2 / NeoForge**: 本番 jar が公式名を使うため、そのままコピーします。

Forge の `fg.deobf` は CurseMaven の座標を直接解決できません。`downloadDebugMods`
が先に `libs/debug-mods` へ jar を置き、そのローカル jar を
`igiDebug:<artifact>:<fileId>` として deobf します。このため `syncDebugMods` は
必ず `downloadDebugMods` に依存します。

Tinkers' Construct / Mantle の jar には Mixin 設定も refmap も含まれていません
（`unzip -l` で確認）。追加の Mixin 設定ファイルを書く必要はなく、開発実行で問題に
なるのは Mixin ではなく SRG 名の解決です。

Mixin を含む MOD（Botania など）は deobf しても refmap が SRG 名のままで、開発実行
では `InvalidAccessorException: No candidates were found matching f_91041_...` の
ようになります。1.20.1 の `build.gradle` は次のプロパティを設定し、
`build/createSrgToMcp/output.srg` を使って refmap を公式名に読み替えます。

```groovy
property 'mixin.env.remapRefMap', 'true'
property 'mixin.env.refMapRemappingFile',
        project.file('build/createSrgToMcp/output.srg').absolutePath
```

Botania 1.20.1-456 はこの設定で開発クライアントに読み込めました。MOD 側の Mixin が
別の理由で失敗する場合は、本番クライアントでの確認に切り替えてください。

## カタログの更新

新しい file ID を試すときは、CurseForge のファイル ID を調べて
`catalogByVersion` の該当行を書き換えます。CurseForge の API はキーが必要ですが、
`https://api.cfwidget.com/minecraft/mc-mods/<slug>` はキーなしで `files` を返すため、
バージョンと file ID の確認に使えます。

ファイル ID を変えたら `./gradlew downloadDebugMods` を実行し、`listDebugMods` と
`run/mods` の内容を確認してください。

## トラブルシューティング

- `Unknown debug mod`: `ext.debugMods` のキーが `listDebugMods` の別名と一致するか
  確認する。
- `Could not find curse.maven:...`: file ID が削除済みか、Minecraft のバージョンに
  対応していない。`downloadDebugMods` を単体で実行し、CurseMaven の応答を確認する。
- MOD が読み込まれない: `run/mods` に jar があるか、`run/logs/latest.log` の
  `Mod File ...` を確認する。Forge では `_mapped_official_` が付いているかを見る。
- 前の MOD が残る: `ext.debugMods` をすべて false にして
  `./gradlew syncDebugMods` を実行する。マーカーに記録された jar が削除される。
  それでも残る jar は手動配置扱いなので、自分で削除する。
- Tinkers で SRG のエラー: `ext.igiForgeDeobf = true` と `fg.deobf` の経路を
  通っているか確認する。`run/mods` の jar 名に `_mapped_official_` が付いていれば
  deobf 済み。
- 1.21.1 で Cobblemon が起動しない: `cobblemon: true` なら `kotlinforforge` も
  自動で追加される。`listDebugMods` で `[AUTO]` になっているか確認する。
- Botania で `InvalidAccessorException` / `No candidates were found matching
  f_91041_`: 1.20.1 の `mixin.env.remapRefMap` 設定と
  `build/createSrgToMcp/output.srg` の生成を確認する。`runClient` は
  `createSrgToMcp` に依存している。
