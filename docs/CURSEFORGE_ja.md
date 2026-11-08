## Summary（256 文字以内）

1.20.1、1.21.1、26.1.2 向けの自由にカスタマイズできる HUD で、表示行を XML、JSON、TXT で定義してゲーム内で編集でき、他の MOD のタグも任意で追加できます。

## Description

# InGameInfoXML (Reloaded)

**Minecraft 1.20.1、1.21.1、26.1.2 向けの、自由にカスタマイズできるデータ駆動型 HUD。**

InGameInfoXML は、自分で設計できる情報オーバーレイを追加する MOD です。表示したい行は XML、JSON、TXT で記述します。各行には、通常のテキスト、カラーコード、ゲーム内の値、アイテムアイコン、ポーション効果、防具の耐久値、他の MOD が提供する値を組み合わせられます。

オーバーレイは 9 種類の画面配置に対応し、0.5 倍から 2 倍まで拡大縮小できます。レイアウトはゲーム内で編集できます。

```xml
<config>
  <lines at="topleft">
    <line><str>HP: {health}/{maxhealth}</str></line>
    <line><str>Biome: {biome}</str></line>
    <line><str>Position: {{ round(x, 1) }}, {{ round(z, 1) }}</str></line>
  </lines>
</config>
```

## 特長

- **ビジュアル HUD エディタ**: `F9` キー（または `/igi editor`）で、行の追加・削除・並べ替え、タグの挿入、配置の切り替え、X / Y オフセットの調整ができます。**Panel** ボタンで、エディタを画面の下端と上端の間で移動できます。
- **タグ一覧とデバッグインスペクター**: `/igi taglist` で検索可能なタグ一覧を開きます。`F8` キー（または `/igi debug`）で、登録されているすべてのタグと現在の値をカテゴリ別に表示するインスペクターを開きます。タグ名をクリックすると `<tag>` がコピーされます。
- **計算式**: `{{ ... }}` で四則演算、括弧、`round`、`min`、`max`、`abs`、`floor`、`ceil`、`format` などの関数を評価でき、1 行の中に小さな計算を書けます。
- **MOD 連携**: Applied Energistics 2、Ars Nouveau、Botania（1.20.1 のみ）、Cobblemon、Cold Sweat、Create、Curios、Goety、Iron's Spells 'n Spellbooks、Mekanism、Paragliders、Serene Seasons、Thirst Was Taken、Tinkers' Construct（1.20.1 のみ）、Tough As Nails、Vampirism の値を任意のタグとして利用できます。手持ちアイテムの Forge / NeoForge エネルギー用タグは本体に組み込まれています。
- **1.12.2 との互換性**: XML、JSON、TXT 形式、`{tag}` 構文、9 種類の配置、拡大率の設定を維持しており、対応タグだけを使った 1.12.2 の HUD ファイルはそのまま読み込めます。設定ファイルは TOML になりゲーム内設定画面で管理します。HUD 定義は `config/InGameInfo.xml` にあります。
- **9 言語**: 英語、日本語、ドイツ語、フランス語、ノルウェー語、ポーランド語、ロシア語、簡体字中国語、繁体字中国語。

## 対応バージョン

| Minecraft | ローダー | Java |
|---|---|---|
| 1.20.1 | Forge 47.4.28 | 17 |
| 1.21.1 | NeoForge 21.1.264 | 21 |
| 26.1.2 | NeoForge 26.1.2.122 | 25 |

クライアント側のみの MOD です。クライアントに導入してください。

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
