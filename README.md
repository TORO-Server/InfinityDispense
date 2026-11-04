# InfinityDispense
infinity という名前を付け、権限のあるプレイヤーが登録したディスペンサーとドロッパーはアイテムを消費しないようにするプラグイン

config.yml の name という項目で アイテムを消費しないようにする名前を変更することができます。

## 登録

名前が同じだけでは無限になりません。次のどちらかの方法で登録すると、ブロックに印が付きます。

- `infinitydispense.create` を持つプレイヤーが、名前を付けたディスペンサーやドロッパーを設置する
- `infinitydispense.create` を持つプレイヤーが、設置済みのものに `/infinitydispense mark` を実行する (以前のバージョンで作ったものはこの方法で登録します)

## コマンド

- `/infinitydispense mark [x y z [world]]`: 名前が同じものを登録する
- `/infinitydispense unmark [x y z [world]]`: 登録を解除する
- `/infinitydispense info [x y z [world]]`: 登録の状態を表示する

座標を省略すると、見ているブロックが対象になります。コンソールからは座標とワールドを指定します。

## 権限

| 権限 | 既定 | 内容 |
|---|---|---|
| `infinitydispense.create` | op | 設置と登録。`infinitydispense.edit` を含む |
| `infinitydispense.edit` | op | 登録済みのものを開いて中身を変更する |

登録済みのものには、ホッパーなどからもアイテムを出し入れできません。
