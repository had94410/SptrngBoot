# 画像差し替えとブランド運用メモ

## 1. 本番向けファイル構成

ブランド共通の画像は、次のディレクトリにまとめて保管してください。

- ロゴ: `/src/main/resources/static/images/brand/logo.svg`
- ヒーロー画像: `/src/main/resources/static/images/brand/hero-banner.svg`
- 追加アイコン: `/src/main/resources/static/images/brand/favicon.svg`（必要時）
- 店舗画像のプレースホルダー: `/src/main/resources/static/images/placeholders/store-placeholder.svg`
- 女の子プロフィール用プレースホルダー: `/src/main/resources/static/images/placeholders/girl-placeholder.svg`

推奨命名ルール:

- `logo.svg`: ブランドロゴ
- `hero-banner.svg`: トップページ用の大きなバナー
- `favicon.svg`: ブラウザアイコン
- `store-<slug>.jpg` または `store-<slug>.webp`: 店舗別の本番画像
- `girl-<slug>.jpg` または `girl-<slug>.webp`: 女性画像の本番画像

## 2. テンプレート参照ルール

ブランド画像は以下のパスを基準に参照してください。

- `/images/brand/logo.svg`
- `/images/brand/hero-banner.svg`

管理画面のアップロード画像は、店舗ごとのファイル保存先または `upload` 時に保存されたパスを参照します。

## 3. 本番差し替えの標準手順

1. 本番用の画像を準備する
2. 既存のプレースホルダーを上書きするか、同じ命名ルールで新しいファイルを作成する
3. `/src/main/resources/static/images/brand/` を更新する
4. `/images/brand/...` のパスになっているか確認する
5. ブラウザでトップページ、店舗一覧、管理画面を確認する
6. デプロイ後に再度表示確認を行う

## 4. 実運用チェック

- 画像が PNG / JPG / SVG で破損していないか
- ロゴの余白と縦横比が自然か
- ヒーロー画像のコントラストが強すぎないか
- 店舗画像がモバイルでも見やすいか
- 本番デプロイ前に `/`, `/admin`, `/store/{id}` を確認したか

## 5. よくある質問

### Q. ロゴやバナーを差し替えたい
A. `images/brand` 配下の既存ファイルを上書きすれば、テンプレート側はそのまま反映されます。

### Q. 画像が表示されない
A. `src` パス・ファイル名・拡張子・デプロイ後のキャッシュを確認してください。

### Q. 画像を管理画面でアップロードしたい
A. 管理画面の「アップロード」機能を使えば、店舗ごとに画像を差し替えできます。

### Q. 本番環境でパスワードを安全に変えたい
A. `ADMIN_USERNAME` / `ADMIN_PASSWORD` を環境変数で設定してください。

## 6. 運用メモ

- ブランド共通画像は `brand` フォルダにまとめて管理すると運用しやすいです。
- 店舗ごとの実画像はアップロードで差し替えられる設計を維持し、テンプレート側の固定パスに依存しないようにします。
- 画像更新時は、プレースホルダーを残したまま本番画像へ置換する運用が安全です。
