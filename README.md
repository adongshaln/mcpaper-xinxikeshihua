# 信息可视化

Paper 26.3 插件。瞄准方块时显示方块名字、当前能不能采集，以及至少需要什么工具；瞄准生物时显示名字、血条和身上的状态。造成伤害或治疗时，弹出数字。

默认界面由 **Adongcraft 启动器悬浮窗** 绘制，固定在屏幕左上角，不随镜头移动。插件负责判断目标和推送数据，启动器负责画图。

## 两种显示方式

| 方式 | 位置 | 说明 |
|---|---|---|
| `overlay`（默认） | 屏幕左上角，固定 | 启动器透明悬浮窗；物品图标来自玩家自己的客户端资源；只有使用 Adongcraft 启动器的玩家能看到 |
| `world` | 游戏内左上角 | 旧的 `TextDisplay` / `ItemDisplay` 方案，会跟随镜头；`hud.world-enabled: true` 打开 |

伤害数字也分两处：`overlay` 由启动器画在目标头顶；`world` 是游戏内展示实体。`damage-numbers.in-world` 控制后者。

## 方块信息

- 方块图标 + 名字。箱子、木桶等有自定义名字的方块会同时显示原名和方块类型。
- **当前可采集 / 当前工具不符 / 工具等级不足 / 无法破坏**。
- 有采集条件时，多显示一行「需要 石镐 或更好」，左边是对应的工具图标。
- 采集条件按原版标签判断：`needs_stone_tool`、`needs_iron_tool`、`needs_diamond_tool` 和 `mineable/*`。创造模式始终可破坏；旁观模式不可破坏。
- 只判断「这一刀能不能拿到掉落物」，不计算挖掘速度。

## 生物信息

- 名字。玩家显示队伍显示名，命名生物显示自定义名字 + 生物类型。
- 血条：绿色实心是当前血量，金色一段是刚刚掉的血，空心是剩余部分；有伤害吸收时显示 `+x`。
- 血条下方逐条显示状态效果：名称、等级、剩余时间。颜色按效果类型区分。
- 最多显示 6 条，多出来的显示 `+N 种状态`。

## 伤害数字

- 普通伤害红色，暴击金色并带 `!`，火焰金色，中毒绿色，凋零深灰，魔法淡紫，冰冻青色等。
- 治疗数字为绿色 `+x`。
- 数字在目标头顶出现，向上飘并淡出。
- 只记录玩家造成的伤害；治疗数字只记录玩家自己受到的治疗。

## 数据接口

插件在 `overlay.bind:overlay.port` 上启动一个只读 HTTP 接口：

```text
GET /hud?player=<玩家名>&since=<上一条事件 id>
X-Adongcraft-Token: <overlay.token>
```

返回 JSON：

- `camera`：玩家眼睛坐标、yaw、pitch，供启动器把伤害数字投影到目标头顶。
- `target`：`block` 或 `entity`；包含名字翻译键、自定义名、图标键、采集状态、需求工具、血量、状态效果。
- `events`：自 `since` 之后的伤害/治疗事件，带目标坐标和高度。
- `hidden`：玩家用 `/xxksh toggle` 关闭后为 `true`，启动器隐藏面板。

默认 `bind: 0.0.0.0`，`port: 8765`。远程玩家需要能连到这个端口；如果不想公开，改成 `127.0.0.1` 并让启动器通过 SSH 隧道连接。

**一定要改 `overlay.token`**，并让 `launcher_config.py` 里的 `OVERLAY_TOKEN` 与它一致。默认值 `change-me` 只适合本机测试。

## 命令与权限

| 命令 | 说明 |
|---|---|
| `/xinxikeshihua toggle` | 打开或关闭自己的 HUD（同时影响悬浮窗和游戏内 HUD）。别名 `/xxksh` |
| `/xinxikeshihua reload` | 重载配置 |

- `xinxikeshihua.use`：使用 HUD，默认所有玩家。
- `xinxikeshihua.reload`：重载配置，默认 OP。

## 配置

`plugins/mcpaper-xinxikeshihua/config.yml`：

```yaml
enabled: true

hud:
  world-enabled: false
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
  in-world: false

overlay:
  enabled: true
  bind: 0.0.0.0
  port: 8765
  token: "change-me"
  update-interval-ticks: 2
```

`hud.*` 和 `damage-numbers.*` 只影响 `world` 模式。`overlay.update-interval-ticks` 是插件扫描目标并生成快照的间隔，默认 2 刻（10 Hz）。

## 启动器配置

`launcher_config.py`：

```python
SERVER_ADDRESS = "114.66.40.132:25565"
OVERLAY_PORT = 8765
OVERLAY_URL = "http://114.66.40.132:8765"
OVERLAY_TOKEN = "change-me"
```

`OVERLAY_URL` 默认由 `SERVER_ADDRESS` 的主机名 + `OVERLAY_PORT` 生成。改端口或走隧道时手动改 `OVERLAY_URL`。

启动器启动游戏后会创建透明、置顶、鼠标穿透的悬浮窗。**启动器需要保持运行**；关闭启动器会一起关闭悬浮窗。游戏退出后悬浮窗会自动关闭。

## 前端说明

- 悬浮窗是全屏透明窗口，HUD 固定在屏幕左上角；不是 Minecraft 世界内的实体，所以不会随镜头移动。
- 启动器会把窗口提升到 macOS 状态栏层级，并设置 `CanJoinAllSpaces` / `FullScreenAuxiliary`，每 750ms 重新置顶，所以切到游戏或失焦时也会尝试保持在最上层。
- Windows 使用 `SetWindowPos(HWND_TOPMOST)` 反复置顶；独占全屏的游戏仍可能盖住任何外部窗口，这种情况请使用无边框窗口化。
- 物品图标从 `versions/26.3/26.3.jar` 的 `assets/minecraft/textures/block|item/*.png` 读取；中文名从客户端资产里的 `zh_cn.json` 读取。
- 伤害数字用服务端发来的 `camera` 和事件坐标投影到目标头顶，默认 FOV 70；如果玩家改过 FOV，数字位置可能有偏差，可在启动器构造 `HudOverlay` 时传 `fov=`。
- 已验证 macOS 窗口化和全屏都能显示；如果遇到全屏切 Space 后看不到，先把游戏切到窗口化/最大化。
- 悬浮窗覆盖在游戏菜单上时也会显示；这是独立窗口的正常行为。

## 构建

需要 Java 25。Gradle 8.10 本身要跑在 Java 17 上；用 Java 25 启动它会在解析构建脚本时报 `class file major version 69`。

```sh
./gradlew test build
```

产物：`build/libs/mcpaper-xinxikeshihua-1.0.0.jar`。放进服务器的 `plugins/` 目录后重启。

启动器端需要重新构建分发包：`./build_macos.sh` 或 `./build_windows.ps1`。`overlay.py` 会被 PyInstaller 自动收集。

## 公告

插件自带 `gonggao.yml`。如果服务器装了 [mcpaper-gonggao](https://github.com/adongshaln/mcpaper-gonggao)，第一次进入的玩家会收到一本介绍这个功能的书。
