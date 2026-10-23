# 信息可视化

Paper 26.3 插件。瞄准方块时，屏幕左上方会显示方块名字、当前能不能采集，以及至少需要什么工具；瞄准生物时显示名字、血条和身上的状态。造成伤害或治疗时，生物头顶会跳出数字。

不依赖资源包，也不需要客户端 Mod。

## 瞄准方块

- 方块图标 + 名字。箱子、木桶等有自定义名字的方块会同时显示原名和方块类型。
- **当前可采集 / 当前工具不符 / 工具等级不足 / 无法破坏**。
- 有采集条件时，多显示一行「需要 石镐 或更好」，左边是对应的工具图标。
- 采集条件按原版标签判断：`needs_stone_tool`、`needs_iron_tool`、`needs_diamond_tool` 和 `mineable/*`。创造模式始终可破坏；旁观模式不可破坏。
- 只判断「这一刀能不能拿到掉落物」，不计算挖掘速度。

## 瞄准生物

- 名字。玩家显示队伍显示名，命名生物显示自定义名字 + 生物类型。
- 血条：实心是当前血量，金色一小段是刚刚掉的血，空心是剩余部分；有伤害吸收时显示 `+x`。
- 血条下方逐条显示状态效果：名称、等级、剩余时间。颜色按效果类型区分，并显示着火 / 冰冻。
- 最多显示 `hud.max-effects` 条，多出来的显示 `+N 种状态`。

## 伤害数字

- 普通伤害红色，暴击金色并带 `!`，火焰金色，中毒绿色，凋零深灰，魔法淡紫，冰冻青色等。
- 治疗数字为绿色 `+x`。
- 数字向上飘并淡出，默认持续 24 刻。
- 附近的玩家都能看到伤害数字；HUD 本身只对本人可见。

## 命令与权限

| 命令 | 说明 |
|---|---|
| `/xinxikeshihua toggle` | 打开或关闭自己的 HUD。别名 `/xxksh` |
| `/xinxikeshihua reload` | 重载配置 |

- `xinxikeshihua.use`：使用 HUD，默认所有玩家。
- `xinxikeshihua.reload`：重载配置，默认 OP。

## 配置

`plugins/mcpaper-xinxikeshihua/config.yml`：

```yaml
enabled: true

hud:
  block-reach: 5.0
  entity-reach: 4.0
  update-interval-ticks: 1
  distance: 0.9
  scale: 0.085
  anchor-x: -0.85
  anchor-y: 0.34
  line-height-factor: 0.25
  background-opacity: 140
  max-effects: 4
  show-block-icon: true
  show-tool-icon: true

damage-numbers:
  enabled: true
  lifetime-ticks: 24
  rise-per-tick: 0.035
  scale: 0.55
  see-through: true
  show-healing: true
  max-active: 80
```

`anchor-x` / `anchor-y` 是 `distance` 处视平面上的偏移，负值向左、正值向上。客户端 FOV 不同，HUD 在屏幕上的位置会略有差异；觉得太靠中间就把 `anchor-x` 调得更负，觉得顶到屏幕外就调回一点。

`line-height-factor` 控制每行文字的高度，工具图标按这个值对齐；如果图标和文字错位，可以在 0.2–0.3 之间微调。

## 前端实现

- HUD 由一块 `TextDisplay` 和两个 `ItemDisplay` 组成，分别是文字、方块图标、所需工具图标。
- 三个展示实体都用 `setVisibleByDefault(false)` 生成，再通过 `Player#showEntity` 只给本人看；其他玩家看不到，也不会被当成瞄准目标。
- 每刻按玩家眼睛的 yaw / pitch 重新计算前、右、上三个方向，把 HUD 放在视线前方 `distance` 格处；文字和图标始终朝向镜头。
- 文字开启 `seeThrough`，贴着墙时文字仍然可见；物品图标没有穿墙属性，极端情况下可能被墙挡住。
- 伤害数字是普通的 `TextDisplay`，只对附近玩家可见，不写进存档。

## 构建

需要 Java 25。Gradle 8.10 本身要跑在 Java 17 上；用 Java 25 启动它会在解析构建脚本时报 `class file major version 69`。

```sh
./gradlew test build
```

产物：`build/libs/mcpaper-xinxikeshihua-1.0.0.jar`。放进服务器的 `plugins/` 目录后重启。

## 公告

插件自带 `gonggao.yml`。如果服务器装了 [mcpaper-gonggao](https://github.com/adongshaln/mcpaper-gonggao)，第一次进入的玩家会收到一本介绍这个功能的书。
