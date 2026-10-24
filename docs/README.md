# ドキュメント

このディレクトリには、InGameInfoXML の使い方・互換性・開発手順を目的別に置いています。
まず [リポジトリの README（英語）](../README.md) または
[README_ja（日本語）](../README_ja.md) で概要とビルド方法を確認し、必要な文書を選んでください。

## 読む順番

### 利用者

1. [README（英語）](../README.md) / [README_ja（日本語）](../README_ja.md): 対応バージョン、ビルド、コマンド、表示定義の基本
2. [1.12.2 との互換性](COMPATIBILITY.md): 設定キー、タグ、値が変わる項目
3. [計算式 `{{ ... }}`](EXPRESSIONS.md): 四則演算、関数、書式
4. [デバッグ要素インスペクター](DEBUG_SCREEN.md): タグ名と現在値の確認

### 開発者

1. [アーキテクチャ](ARCHITECTURE.md): バージョンごとの構成、処理の流れ、MOD 連携 SPI
2. [デバッグ用 MOD の On/Off](DEBUG_MODS.md): 開発クライアントへ任意の MOD を入れる
3. [描画テスト](RENDER_TEST.md): Xvfb + llvmpipe で HUD と GUI を確認する

### 移植者

1. [アーキテクチャ](ARCHITECTURE.md) の移植チェックリスト
2. [1.12.2 との互換性](COMPATIBILITY.md): 値が変わるタグと互換値
3. [描画テスト](RENDER_TEST.md): テスト用ワールドとスクリーンショットの手順
4. [versions/README.md](../versions/README.md): バージョン固有の API 差

## 一覧

| 文書 | 内容 |
|---|---|
| [COMPATIBILITY.md](COMPATIBILITY.md) | 1.12.2 の設定・タグ・表示との違い、修正したバグ、未移植の項目 |
| [EXPRESSIONS.md](EXPRESSIONS.md) | `{{ ... }}` の文法、関数、Java の書式、失敗時の挙動 |
| [DEBUG_SCREEN.md](DEBUG_SCREEN.md) | 全タグと現在値を一覧するデバッグ画面の操作 |
| [DEBUG_MODS.md](DEBUG_MODS.md) | `ext.debugMods` で開発用 MOD を切り替える手順 |
| [RENDER_TEST.md](RENDER_TEST.md) | ヘッドレス描画テストの準備、確認項目、バージョン別の結果 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 独立ビルドの構成、パーサーと値の流れ、MOD 連携 SPI |
| [render/](render/) | 確認に使ったスクリーンショット |

## スクリーンショット

`render/` の画像は、Xvfb と Mesa の llvmpipe で実際のクライアントを起動して撮った
ものです。画像を更新したときは、対応する [RENDER_TEST.md](RENDER_TEST.md) の結果も
合わせて確認してください。
