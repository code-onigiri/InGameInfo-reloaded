# CurseForge 用画像

CurseForge のプロジェクトページへアップロードする画像です。Description に外部画像を直接埋め込まず、Gallery へアップロードしてから、下の英語キャプションを付けてください。

## アバター

| ファイル | サイズ | 用途 |
|---|---|---|
| [avatar-400.png](avatar-400.png) | 400x400 | CurseForge のプロジェクトアバター。現在の 48x48 の `logo.png` を 8 倍し、同色の余白を付けたもの |

## Gallery

アップロード順の例です。1 枚目がプロジェクト一覧で最初に見える画像になります。

| ファイル | 内容 | 英語キャプション |
|---|---|---|
| [01-hud.png](01-hud.png) | XML で組んだ HUD。ワールド情報、経験値、装備の耐久値、防具の耐久値を表示 | A HUD built from XML lines: world information, experience, equipment durability and armor. |
| [02-editor.png](02-editor.png) | ゲーム内エディタ。パネルを上端へ移動し、下側の HUD を隠さず編集 | The in-game HUD editor. The panel can move to the top edge so the lower HUD stays visible. |
| [03-taglist.png](03-taglist.png) | タグ一覧。タグ名と説明を検索できる | Search every registered tag and read its description without leaving the game. |
| [04-config.png](04-config.png) | 設定画面。HUD ファイル、スケール、チャット表示、オーバーレイ | The config screen: HUD file, scale, chat display and overlay options. |
| [05-expressions.png](05-expressions.png) | `{{ ... }}` の計算式。四則演算、書式、失敗時のリテラル表示 | Expressions evaluate arithmetic, functions and Java-style formatting inside a HUD line. |
| [06-botania.png](06-botania.png) | Botania のマナプールをターゲットした表示 | Botania target mana: read the pool, spreader or flower you are looking at. |
| [07-ae2.png](07-ae2.png) | AE2 のネットワークをターゲットした表示 | Applied Energistics 2: read channels, power and cell status for the targeted network. |

## アップロード時の注意

- Gallery の画像は 16:9 の 1280x720 に統一しています。CF 側で自動縮小されます。
- アバターは 400x400 必須です。[avatar-400.png](avatar-400.png) をそのまま使えます。
- Description には Gallery を参照する文だけを入れ、画像の直リンクは貼らないでください。CF のモデレーションポリシーでは外部ファイルの直リンクが制限されています。
- 画像を差し替えたときは、この表のキャプションも更新してください。
