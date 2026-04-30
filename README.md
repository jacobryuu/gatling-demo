# gatling-demo

Gatlingベースの性能試験テンプレート。  
ユーザー行動シナリオ、負荷モデル、SLO判定、CI連携、監視連携の指針を一式そろえています。

## 必要要件

- Java 21+
- sbt 1.9.x (`project/build.properties` で固定)
- Gatling 3.11.x / Scala 2.13.x (`build.sbt` で管理、Scala 3対応は後述)

## クイックスタート

```bash
# コンパイル
sbt Test/compile

# Smokeテスト (CIや疎通確認向け)
LOAD_PROFILE=smoke BASE_URL=https://staging.example.com \
  sbt "Gatling/testOnly simulations.MixedWorkloadSimulation"

# レポートは target/gatling/ 配下に生成されます
open target/gatling/*/index.html
```

ローカルで疎通確認するだけなら、付属のスタブサーバーを起動できます:

```bash
python3 ci/mock_server.py 8080 &
BASE_URL=http://127.0.0.1:8080 LOAD_PROFILE=smoke \
  sbt "Gatling/testOnly simulations.MixedWorkloadSimulation"
```

## Simulation 一覧

| クラス | 用途 |
|---|---|
| `simulations.MixedWorkloadSimulation` | search / browse / transaction を `WEIGHT_*` で重み付き並列実行 |
| `simulations.SearchOnlySimulation` | 読み取り経路 / 検索基盤専用 |
| `simulations.CheckoutFlowSimulation` | 書き込み経路 / 冪等性 / 在庫・決済依存先の検証 |

実行:
```bash
sbt "Gatling/testOnly simulations.SearchOnlySimulation"
sbt "Gatling/testOnly simulations.CheckoutFlowSimulation"
sbt "Gatling/test"   # 全 Simulation を実行
```

新しい Simulation を追加するには `src/test/scala/simulations/` に Class を作り、`Scenarios.*` と `LoadProfiles.forProfile(...)` を組み合わせるだけ。

## 環境変数

| 変数 | 説明 | デフォルト |
|---|---|---|
| `BASE_URL` | 対象アプリのベースURL | `https://example.com` |
| `LOAD_PROFILE` | `smoke` / `rampup` / `stress` / `spike` / `soak` | `smoke` |
| `USERS` | 総ユーザー数 (rampup/soak) | `10` |
| `PEAK_USERS` | ストレス時のピーク到達ユーザー数 | `1000` |
| `SPIKE_USERS` | スパイク時の瞬間投入ユーザー数 | `500` |
| `RAMP_DURATION_SECONDS` | Ramp期間 (秒) | `60` |
| `HOLD_DURATION_SECONDS` | 維持期間 (秒) | `60` |
| `SOAK_DURATION_SECONDS` | Soak期間 (秒) | `1800` |
| `WEIGHT_SEARCH` / `WEIGHT_BROWSE` / `WEIGHT_TRANSACTION` | シナリオ比率 | `0.7` / `0.2` / `0.1` |
| `SLO_P95_MS` | p95応答時間SLO (ms) | `1000` |
| `SLO_P99_MS` | p99応答時間SLO (ms) | `2000` |
| `SLO_ERROR_RATE_PCT` | エラー率SLO (%) | `1.0` |
| `AUTH_MODE` | `cookie` / `token` / `none` | `cookie` |
| `USERS_CSV` | usersフィーダーのclasspath相対パス。CIではsecret由来CSVのパスを差し込む | `feeders/users.csv` |

## 負荷モデル

| Profile | 用途 | 形状 |
|---|---|---|
| `smoke` | CI / 疎通確認 | 各シナリオ最少ユーザーで1回ずつ |
| `rampup` | 段階的負荷 | `0 → USERS` を `RAMP_DURATION_SECONDS` で増加 |
| `stress` | 限界性能測定 | `0 → PEAK_USERS/sec` まで増加 |
| `spike` | 瞬間負荷 | 10秒待機後 `SPIKE_USERS` を一括投入 |
| `soak` | 長時間耐久 | `USERS/sec` を `SOAK_DURATION_SECONDS` 維持 |

## ディレクトリ構成

```
build.sbt
project/
  build.properties
  plugins.sbt
src/test/scala/
  simulations/
    MixedWorkloadSimulation.scala
    SearchOnlySimulation.scala
    CheckoutFlowSimulation.scala
  support/
    Config.scala         # 環境変数からの設定一元化
    HttpProtocols.scala  # 共通HTTP設定
    Journeys.scala       # ログイン/検索/詳細/トランザクションのChain
    Scenarios.scala      # 共有シナリオビルダ
    LoadProfiles.scala   # 負荷プロファイル工場
    Slo.scala            # 標準SLO assertions
src/test/resources/
  feeders/{users,search_terms,transaction_data}.csv
  gatling.conf
  logback-test.xml
ci/
  mock_server.py         # CI smoke用スタブHTTPサーバー
.github/workflows/performance.yml
```

## シナリオ設計

`support/Journeys.scala` にログイン、検索、詳細閲覧、トランザクションのChainを定義。  
URL/ペイロードはプレースホルダー (`/login`, `/items`, `/items/{id}`, `/transactions`) なので対象APIに合わせて差し替えてください。

### 動的データ

- セッション管理: Gatlingが自動でCookieを保持
- 認証トークン: `AUTH_MODE=token` でログインJSONレスポンスの `$.token` を抽出し、後続リクエストの `Authorization: Bearer #{authToken}` に注入 (Gatling EL はリクエスト時評価)
- 一覧→詳細: 一覧レスポンスから `$.items[0].id` を抽出 (optional)
- 詳細フォールバック: itemIdが取れない場合は `/items/1` にフォールバック

### Feeder

- `users.csv`: テスト用認証情報 (本番秘密情報を入れないこと)
- `search_terms.csv`: 検索キーワード
- `transaction_data.csv`: 数量と冪等性キー

CIで本物の認証情報を使う場合は `USERS_CSV` 環境変数で別パスを指定し、CSVは secret から `src/test/resources/feeders/` 配下に展開してください。

## SLO判定 (`Slo.scala`)

`setUp(...).assertions(Slo.all: _*)` で以下を機械判定し、違反時はビルド失敗:

- `global` p95 < `SLO_P95_MS` (`percentile(95.0)`)
- `global` p99 < `SLO_P99_MS` (`percentile(99.0)`)
- `global` 成功率 > `100 - SLO_ERROR_RATE_PCT` %
- `forAll` リクエスト失敗率 < `SLO_ERROR_RATE_PCT` %

> **重要**: `percentile3` / `percentile4` の順序インデックスは Gatling 設定変更で意味が変わるため使わず、明示的に `percentile(95.0)` / `percentile(99.0)` を使用しています。

## 監視連携

性能試験中はGatlingレポートに加えてアプリ/インフラ側のメトリクスを必ず突き合わせます。

### Prometheusで収集する推奨メトリクス

- `process_cpu_usage`, `system_cpu_usage`
- `jvm_memory_used_bytes`, `jvm_gc_pause_seconds`
- `http_server_requests_seconds_count|sum|max` (APIレイテンシ/エラー率)
- DB接続数 (`hikaricp_connections_*` など)
- 5xx率、タイムアウト率

### Grafana

- 試験windowを示すアノテーション (Gatling開始/終了時刻)
- API別 p95/p99パネル
- エラー率パネル
- DB / CPU / メモリ / GCパネル

Gatling本体から時系列DBに直接送る場合は `gatling-graphite` 連携の追加導入を検討してください。

## CI/CD

`.github/workflows/performance.yml`:

- **PR時**: `Test / compile` + 同梱の `ci/mock_server.py` に対してsmoke実行 → 実シミュレーションコードが動作することを保証
- **`workflow_dispatch`**: simulation (`MixedWorkloadSimulation` / `SearchOnlySimulation` / `CheckoutFlowSimulation` / `all`)、profile、base_url、users を選んで手動実行
- **Nightly schedule** (UTC 18:00): smoke実行
- ターゲットURLは入力 → `vars.PERF_BASE_URL` → mock サーバーの順で解決
- Gatlingレポートは `gatling-reports-<sim>-<profile>` artifactとして14日保持

⚠️ 重い負荷試験 (stress/spike/soak) はGitHub Hostedランナーでは負荷源として不十分なため、ステージング近傍の専用ランナーで実行することを推奨します。

## 結果分析の進め方

1. Gatlingレポートで全体およびリクエスト別のp95/p99/エラー率を確認
2. SLO違反箇所を特定
3. アプリ/DB/ネットワーク側メトリクスと時刻相関し、ボトルネックを切り分け
   - APIレイテンシ ≫ DBレイテンシ → アプリ側
   - DB接続待ち / クエリ遅延 → DB / N+1 / インデックス
   - ネットワーク / TLS handshake増加 → コネクションプール / Keep-Alive
4. 修正してからプロファイルを再実行 (再現性のため同条件で)

## 改善サイクル

1. テスト実行
2. ボトルネック分析 (Gatlingレポート + Prometheus/Grafana)
3. 改善 (コード/インフラ/設定)
4. 再テスト

1回で終わらせず、SLO達成と性能リグレッション防止のため継続的に最適化してください。

## Scala 3 への移行

Gatling 3.11+ は Scala 3 にクロスビルドされていますが、現状 (3.11.5 / 3.15.0 とも) DSL の暗黙変換に Scala 3 で曖昧解決バグが残っています:

```
both stringIsNeitherValidableNorString1 and stringIsNeitherValidableNorString2
match type io.gatling.core.NeitherValidableNorString.DoesNotContain[String]
```

回避するには `queryParam("q", "#{searchTerm}".asInstanceOf[Expression[String]])` のように明示型注釈を入れるか、Gatling 公式が当該実装を整理した版を待つ必要があります。本テンプレートでは安定運用のため Scala 2.13 を採用しています。

Scala 3 を試したい場合の最小変更:

```scala
// build.sbt
scalaVersion := "3.3.3"
scalacOptions := Seq("-encoding", "UTF-8", "-release", "11", "-deprecation", "-feature", "-unchecked")
// libraryDependencies は同じ (`_3` artifacts に自動解決)
```

それに加えて `Journeys.scala` 等の DSL 呼び出しで暗黙変換の曖昧性が出る箇所を逐次修正してください。

## 安全に実行するための注意

- 本番環境への直接負荷は禁止。必ずステージング (本番相当) で実行
- 認証情報は `users.csv` に直接書かず、`USERS_CSV` で CI secrets 由来のCSVを差し込む
- CIで重プロファイルを動かす場合は手動承認 (`environment` protection rules) を推奨
