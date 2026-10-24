# CurseForge 掲載文（日本語）

CurseForge の Summary 欄と Description 欄に貼る日本語文です。英語版は
[CURSEFORGE.md](CURSEFORGE.md) にあります。アップロード時の注意は英語版の末尾にあり、
このファイルには含めていません。

## Summary（Summary 欄、256 文字以内）

1.20.1、1.21.1、26.1.2 向けの自由にカスタマイズできる HUD で、表示行を XML、JSON、TXT で定義してゲーム内で編集でき、他の MOD のタグも任意で追加できます。

## Description（Description 欄）

# InGameInfoXML (Reloaded)

**Minecraft 1.20.1、1.21.1、26.1.2 向けの、自由にカスタマイズできるデータ駆動型 HUD。**

InGameInfoXML は、自分で設計できる情報オーバーレイを追加する MOD です。表示したい行は XML、JSON、TXT で記述します。各行には、通常のテキスト、カラーコード、ゲーム内の値、アイテムアイコン、ポーション効果、防具の耐久値、他の MOD が提供する値を組み合わせられます。

オーバーレイは 9 種類の画面配置に対応し、0.5 倍から 2 倍まで拡大縮小できます。

## このプロジェクトについて

これは Lunatrius による **InGameInfoXML** のフォークです。オリジナルは MIT ライセンスのもとで Minecraft 1.12.2 を対象にしていました。

コードは当初、AI 支援のバイブコーディングで作成しました。その後、人がレビューとデバッグを行いました。

## 1.12.2 版からの変更点

- **対応バージョン**: Forge 1.20.1、NeoForge 1.21.1、NeoForge 26.1.2 に対応しています。
- **HUD 定義の互換性**: XML、JSON、TXT 形式、`{tag}` 構文、9 種類の配置、拡大率の設定を維持しています。
- **ビジュアル HUD エディタ**: `F9` キー（または `/igi editor`）で、行の追加・削除・並べ替え、タグの挿入、配置の切り替え、X / Y オフセットの調整ができます。**Panel** ボタンで、エディタを画面の下端と上端の間で移動できます。
- **タグ一覧とデバッグインスペクター**: `/igi taglist` で検索可能なタグ一覧を開きます。`F8` キー（または `/igi debug`）で、登録されているすべてのタグと現在の値をカテゴリ別に表示するインスペクターを開きます。タグ名をクリックすると `<tag>` がコピーされます。
- **計算式**: `{{ ... }}` で四則演算、括弧、`round`、`min`、`max`、`abs`、`floor`、`ceil`、`format` などの関数を評価できます。
- **MOD 連携**: Create、Curios、Applied Energistics 2、Mekanism、Botania、Tinkers' Construct、Cobblemon、Paragliders などの値を任意のタグとして利用できます。
- **設定方式**: TOML のクライアント設定とゲーム内設定画面が、以前の設定ファイルに代わります。HUD 定義は `config/InGameInfo.xml` にあります。
- **9 言語**: 英語、日本語、ドイツ語、フランス語、ノルウェー語、ポーランド語、ロシア語、簡体字中国語、繁体字中国語。

## 表示できるもの

プレイヤー、ワールド、インベントリ、効果、パフォーマンス、MOD の値を表示できます。

<div class="spoiler">

- **プレイヤー**: 体力、最大体力、吸収、空腹度、満腹度、防御力、息、経験値、レベル、ゲームモード、スコア、チーム、効果、属性。
- **ワールド**: バイオーム、ディメンション、座標、向き、明るさ、時刻、日数、天気、月相、難易度、シード値。
- **インベントリと装備**: メインハンドとオフハンドのアイテム、防具スロット、耐久値、アイテム数、アイテム名、アイコン、カーソルを合わせたアイテムの情報。
- **ポーション効果**: 効果名、強度、残り時間、有効な効果のアイコン。
- **パフォーマンス**: FPS、MSPT、TPS。
- **他の MOD**: エネルギー、AE2 のネットワークとセル、Create のキネティクス、Mekanism の放射線、Botania のマナ、Cobblemon のパーティ情報、Tinkers' のツール性能、Paragliders のスタミナなど。

1.12.2 との互換性のために残した別名を含む全タグの一覧は、`/igi taglist` で確認できます。

</div>

## HUD 定義の例

XML の構造、カラーコード、保存形式を説明します。

<div class="spoiler">

行は `config/InGameInfo.xml` の中で配置ごとにまとめます。

```xml
<config>
  <lines at="topleft">
    <line><str>HP: {health}/{maxhealth}</str></line>
    <line><str>Biome: {biome}</str></line>
    <line><str>Position: {{ round(x, 1) }}, {{ round(z, 1) }}</str></line>
  </lines>
</config>
```

`{tag}` はタグの値に置き換わります。カラーコード `$0` から `$f`、リセットコード `$r` に対応しています。同じ定義は `/igi save <file>` で XML、JSON、TXT として保存できます。拡張子が形式を決めます。

</div>

## 計算式

四則演算、関数、Java 形式の書式を利用できます。

<div class="spoiler">

`{{ ... }}` を使うと、1 行の中に小さな計算を書けます。

```xml
<str>Damage: {{ attackdamage * 2 + 1 }}</str>
<str>Health: {{ format("%.1f", health / maxhealth * 100) }}%</str>
```

- 演算子: `+`、`-`、`*`、`/`、`%`、括弧、単項の `+` / `-`
- 関数: `round(value[, digits])`、`min(...)`、`max(...)`、`abs(value)`、`floor(value)`、`ceil(value)`、`format("java format", value, ...)`
- `format` の引数順は Java の `String.format` と同じです。
- 整数は小数部分を付けずに表示します。それ以外の数値は最大 3 桁に丸め、末尾の 0 を削除します。
- タグが不明な場合、構文が不正な場合、書式の適用に失敗した場合は、元の `{{ ... }}` をそのまま表示します。

</div>

## HUD エディタとインスペクター

- **F9** または `/igi editor`: ビジュアルエディタを開きます。行を選んでテキストを編集し、**Tag...** でタグを挿入し、3x3 の配置を変更し、X / Y オフセットを調整して保存できます。**Panel** で、エディタを画面の下端と上端の間で切り替えられます。
- **F8** または `/igi debug`: タグインスペクターを開きます。タグはカテゴリ別にまとまり、現在の値を表示します。名前をクリックすると `<tag>` がコピーされます。
- **MOD 一覧画面の Config**: HUD ファイル名、拡大率、バニラ HUD の表示、チャットとプレイヤーリストの動作、ポーションとアイテムアイコンのオーバーレイ、小数点以下の桁数などを変更できます。

エディタは HUD 定義を `config/InGameInfo.xml` に、配置オフセットを `config/ingameinforeloaded-client.toml` に書き込みます。

## ギャラリー

Gallery タブでは、HUD、パネルを移動できるゲーム内エディタ、タグ一覧、設定画面、計算式、任意の MOD 連携を表示します。

## コマンド

再読み込み、読み込み、保存、オーバーレイの表示の切り替え、エディタとインスペクターの起動、配置の変更ができます。

<div class="spoiler">

| コマンド | 動作 |
|---|---|
| `/igi reload` | 現在の HUD 定義を再読み込みします |
| `/igi load <file>` | 別の XML、JSON、TXT 定義を読み込みます |
| `/igi save <file>` | 現在の定義を、拡張子に応じた形式で保存します |
| `/igi enable` / `/igi disable` | オーバーレイを一時的に表示または非表示にします |
| `/igi taglist` | 検索可能なタグ一覧を開きます |
| `/igi editor` | HUD エディタを開きます |
| `/igi debug` | タグインスペクターを開きます |
| `/igi alignment get <ALIGN>` | 配置の X / Y オフセットを表示します |
| `/igi alignment set <ALIGN> [x y]` | 配置のオフセットを変更します |
| `/igi config <key> <value>` | 対応しているクライアント設定を変更します |

</div>

## 対応バージョン

| Minecraft | ローダー | Java |
|---|---|---|
| 1.20.1 | Forge 47.4.28 | 17 |
| 1.21.1 | NeoForge 21.1.264 | 21 |
| 26.1.2 | NeoForge 26.1.2.122 | 25 |

## 導入

1. Minecraft のバージョンに合った Forge または NeoForge をインストールします。
2. 対応する `ingameinforeloaded-*.jar` を `mods` フォルダーに置きます。
3. 一度ゲームを起動します。MOD が `config/InGameInfo.xml` と `config/ingameinforeloaded-client.toml` を作成します。
4. `F9` キーでエディタを開くか、`config/InGameInfo.xml` を編集して `/igi reload` を実行します。

## MOD 連携

他の MOD の値を任意のタグとして利用できます。AE2 の値は 1 秒ごとに更新されます。

<div class="spoiler">

| MOD | タグとして公開するデータ | 対応バージョン | 確認状況 |
|---|---|---|---|
| Applied Energistics 2 | ネットワークのチャンネル数とモード、グリッド電力、平均使用量、セルの容量と状態 | 全バージョン | HUD で確認済み |
| Mekanism | 個人と環境の放射線、深刻度、有効状態、割合 | 全バージョン | HUD で確認済み |
| Botania | カーソルを合わせたプール、スプレッダー、花のマナ（最大値と割合を含む） | 1.20.1 | HUD で確認済み |
| Tinkers' Construct | ツールの耐久値、採掘速度、攻撃ダメージ、採取レベル、修飾子、破損状態 | 1.20.1 | 公式 API 経由で HUD 確認済み |
| Cobblemon | パーティの数と、各スロットの種類、ニックネーム、レベル、HP、状態、色違い、タイプ | 全バージョン | HUD で確認済み |
| Paragliders | スタミナ、最大スタミナ、割合、疲労状態 | 全バージョン | HUD で確認済み |
| Create | カーソルを合わせたブロックまたは搭乗中の装置の回転速度、ストレス、装置の値 | 全バージョン | コンパイル確認のみ |
| Curios | Curio スロットの情報 | 全バージョン | コンパイル確認のみ |
| Serene Seasons | 季節と関連するワールドの値 | 全バージョン | コンパイル確認のみ |
| Ars Nouveau | マナと呪文に関する値 | 全バージョン | コンパイル確認のみ |
| Iron's Spells 'n Spellbooks | マナと呪文に関する値 | 全バージョン | コンパイル確認のみ |
| Tough As Nails | 気温と渇きの値 | 全バージョン | コンパイル確認のみ |
| Vampirism | 吸血鬼レベルと関連する値 | 全バージョン | コンパイル確認のみ |
| Cold Sweat | 気温と体の値 | 全バージョン | コンパイル確認のみ |
| Thirst Was Taken | 渇きの値 | 全バージョン | コンパイル確認のみ |
| Goety | ソウルエネルギーと関連する値 | 全バージョン | コンパイル確認のみ |
| Forge / NeoForge Energy | 手持ちアイテムのエネルギー、最大エネルギー、割合 | 全バージョン | 本体に実装済み |

</div>

## 1.12.2 の HUD ファイルとの互換性

維持したもの、変更したもの、互換値を返すタグを説明します。

<div class="spoiler">

- ファイル構造、`{tag}` 構文、配置名、カラーコードは維持しています。
- 現在のバージョンでも取得できる値があるタグは、元の名前を維持しています。よく使われる別名もエイリアスとして登録しています。
- 数値のアイテム ID、アイテムの metadata、クライアント側のワールドサイズなど、削除された概念に依存するタグは、互換値またはレジストリ ID を返します。
- 設定ファイルは TOML になり、設定画面で管理します。HUD 定義は XML、JSON、TXT のままです。
- `{{ ... }}` の計算式は新しく追加した機能で、1.12.2 では解釈されません。

対応しているタグだけを使った 1.12.2 の HUD ファイルは、そのまま読み込めるはずです。

</div>

## 対応言語

インターフェースとタグの説明は次の言語で利用できます。

- 英語（`en_us`）
- 日本語（`ja_jp`）
- ドイツ語（`de_de`）
- フランス語（`fr_fr`）
- ノルウェー語（`no_no`）
- ポーランド語（`pl_pl`）
- ロシア語（`ru_ru`）
- 簡体字中国語（`zh_cn`）
- 繁体字中国語（`zh_tw`）

## 既知の制限

ビルド確認のみの連携と、26.1.2 の対応範囲について説明します。

<div class="spoiler">

- 一部の任意連携はコンパイル確認のみで、すべてのバージョンで値の確認はしていません。
- 一部のサードパーティ MOD は 26.1.2 向けのビルドを公開していません。

</div>

## ライセンス

このプロジェクトは MIT ライセンスで提供されています。

## クレジット

- Minecraft 1.12.2 向けのオリジナル **InGameInfoXML**: **Lunatrius**
- 以前の **InGameInfo** の作者: **DaftPVF**、**bspkrs**
- 出発点として使用した 1.20.1 Forge フォーク: **DF11ZomGraves**
- 1.20.1 / 1.21.1 / 26.1.2 への移植と保守: **code-onigiri**

## リンク

- ソースコードとイシュートラッカー: https://github.com/code-onigiri/InGameInfo-reloaded
- ドキュメント: https://github.com/code-onigiri/InGameInfo-reloaded/tree/master/docs
- ライセンス: https://github.com/code-onigiri/InGameInfo-reloaded/blob/master/LICENSE

InGameInfoXML は Mojang Studios、Microsoft、Forge、NeoForge、原作者とは提携していません。Minecraft は Mojang Studios の商標です。
