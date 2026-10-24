# デバッグ用 MOD の On/Off

開発クライアントで任意の MOD を一時的に有効化するための仕組みです。MOD の jar は
リポジトリに含めず、バージョンごとの `libs/debug-mods/` に置きます。`ext.debugMods` で
true にした jar だけが `run/mods` にコピーされ、前回の選択は次回の起動時に自動で
外れます。

有効化した MOD の連携タグが登録されているかは、ゲーム内で `F8` または `/igi debug` を
押して開く[デバッグ要素インスペクター](DEBUG_SCREEN.md)で確認できます。

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
]
```

```bash
cd versions/1.20.1

# 置いてある jar と現在の ON/OFF を確認
./gradlew listDebugMods

# ext.debugMods が true の jar を run/mods に同期して起動
./gradlew runClient

# 起動せず同期だけ実行（すべて false なら前回の選択を削除）
./gradlew syncDebugMods
```

`runClient` と `runServer` は起動前に `syncDebugMods` を実行します。`ext.debugMods` を
省略した場合や全項目が false の場合は、前回の選択だけが外れます。MOD を増やすときは
`ext.debugMods` に別名のキーを追加し、true/false を設定してください。

## jar の置き場所

```text
versions/1.20.1/libs/debug-mods/
versions/1.21.1/libs/debug-mods/
versions/26.1.2/libs/debug-mods/
```

`*.jar` は `.gitignore` で除外されています。ディレクトリが無い場合は作成してください。
`*_mapped_official_*` は ForgeGradle が生成する deobf 済み jar なので、置き場所の
スキャン対象から除外されます。

## 別名と依存解決

別名は jar の `META-INF/mods.toml` または `META-INF/neoforge.mods.toml` から
`modId` を読み、見つからない場合はファイル名の先頭から作ります。現在の主な別名:

| 別名 | jar の例 |
|---|---|
| `tconstruct` | TConstruct-1.20.1-3.12.1.231.jar |
| `mantle` | Mantle-1.20.1-1.11.117.jar |
| `cobblemon` | Cobblemon-neoforge-1.8.2+1.21.1.jar |
| `mekanism` | Mekanism-1.21.1-10.7.19.85.jar |
| `ae2` | ae2-1.21.1.jar |
| `guideme` | guideme-21.1.20.jar |
| `kotlinforforge` | kotlinforforge-5.12.0-all.jar |

選択した MOD の必須依存（`mandatory = true` または `type = "REQUIRED"`）と、
`modLoader` に指定された言語プロバイダは、jar があれば自動で追加されます。

- `tconstruct` → `mantle`
- `cobblemon` → `kotlinforforge`
- `ae2` → `guideme`

true にした別名が jar に見つからない場合、利用できる別名を添えて Gradle が失敗します。

## Forge と NeoForge の違い

- **1.20.1 / Forge**: 本番用 MOD jar は SRG 名でコンパイルされています。そのまま
  `run/mods` に置くと `NoSuchFieldError: f_256747_` や
  `NoSuchMethodError: ResourceLocation.m_135827_()` になります。`debug-mods.gradle`
  は選択した jar を `fg.deobf` した `*_mapped_official_1.20.1.jar` に変換してから
  `run/mods` にコピーします。1.20.1 の `build.gradle` は `ext.igiForgeDeobf = true`
  を設定しています。
- **1.21.1 / 26.1.2 / NeoForge**: 本番 jar が公式名を使うため、そのままコピーします。

Tinkers' Construct / Mantle の jar には Mixin 設定も refmap も含まれていません
（`unzip -l` で確認）。追加の Mixin 設定ファイルを書く必要はなく、開発実行で問題に
なるのは Mixin ではなく SRG 名の解決です。

## 管理対象の記録

`syncDebugMods` はコピーしたファイル名を `run/mods/.igi-debug-mods` に記録します。
次回の実行時にこの一覧のファイルだけを削除してから、新しい選択をコピーします。
手動で `run/mods` に置いた jar や、他の用途の jar は削除しません。

全項目を false にして `syncDebugMods` を実行すると、記録済みの jar をすべて削除し、
マーカーも消えます。
Gradle の `Copy` タスクは入力が空だと `NO-SOURCE` になり削除処理ごとスキップされる
ため、このタスクは `DefaultTask` として実装しています。

## トラブルシューティング

- `Unknown debug mod`: `ext.debugMods` のキーが `./gradlew listDebugMods` の別名と
  一致するか確認する。
- MOD が読み込まれない: `run/mods` に jar があるか、`run/logs/latest.log` の
  `Mod File ...` を確認する。Forge では `_mapped_official_` が付いているかを見る。
- 前の MOD が残る: `ext.debugMods` をすべて false にして `./gradlew syncDebugMods`
  を実行する。
  それでも残る jar は手動配置扱いなので、自分で削除する。
- Tinkers で SRG のエラー: `ext.igiForgeDeobf = true` と `fg.deobf` の経路を
  通っているか確認する。`run/mods` の jar 名に `_mapped_official_` が付いていれば
  deobf 済み。
- 1.21.1 で Cobblemon が起動しない: `kotlinforforge` の jar も
  `libs/debug-mods/` に置く（`cobblemon: true` なら自動追加）。
