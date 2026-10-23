# 归藏电视 · Android 6 定制版

基于 **[op7418/guizang-tv](https://github.com/op7418/guizang-tv) 正式版 v0.3.0** 二次修改，为较老的小米电视增加游戏机入口、月历、天气和可关闭的 Home 键返回服务。维护者：[guehie](https://github.com/guehie)。这是独立定制 Fork，非原作者官方发布。

**[下载本定制版 APK](https://github.com/guehie/guizang-tv-custom/releases/latest)** · [改动、实机结果与恢复方法](CUSTOM.md) · [完整来源与致谢](NOTICE.md) · [MIT 代码许可](LICENSE)

当前版本：`0.3.0-custom.6-public.1`，最低 Android 6.0 / API 23，包名 `io.github.op7418.guizangtv.custom`。以 v0.3.0 为基线，没有合并上游 main 后续的 Codex 对话实验。

## 从哪些项目修改而来

| 项目 / 作者 | 本版的关系 |
| --- | --- |
| [op7418/guizang-tv](https://github.com/op7418/guizang-tv) | 直接修改的基础项目，沿用原生 Java 桌面、设置、列表 / DOCK、壁纸与小米接口；保留上游 Git 历史及 MIT 署名 |
| [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv)，Gathin | 归藏电视更早的衍生基础；保留原始版权，没有打包一键清理和系统应用禁用脚本 |
| [nthnclrk/CleanFrame](https://github.com/nthnclrk/CleanFrame)，Nathan Clark | 上游的界面及分组设置交互参考，没有移植其 Tizen 代码或照片 |
| [SinHom 的 Android 6 兼容 PR #2](https://github.com/op7418/guizang-tv/pull/2) | 参考旧 Android 集合 API 和开机识别的兼容思路；本版自行整合所需实现 |

其他数据与素材：农历 / 节气来自香港天文台，天气来自 Open-Meteo（CC BY 4.0），品牌 / 工具图标为上游已署名的 Simple Icons / Phosphor Icons。详细说明见 [NOTICE.md](NOTICE.md)。

## 本版改了什么

- **五个常用入口**：`NINTENDO SWITCH → HDMI 1`、`NINTENDO SWITCH2 → HDMI 2`、哔哩哔哩、小米投屏、小米原桌面。入口名称可编辑，保存时英文字母统一大写；列表和 DOCK 共用名称。
- **月历替换日期行**：星期按一二三四五六日；半透明大月份作为背景；过去日期淡化，今天和之后日期高亮；今天字号略大，下方附红色短横。
- **日期与天气**：时钟带秒；显示农历与节气（2025–2035）；天气读取电视已有的小米系统定位，30 分钟刷新。首页隐藏定位来源和更新时间，详情仍可查看。公开版不内置个人城市或固定坐标；定位缺失时显示等待定位。
- **Android 6 兼容**：最低 API 降为 23；处理旧版 API 差异和天气 HTTPS 根证书，仍校验证书链与主机名。
- **主页键返回**：可选择启用 HomeKeyService，在已验证的老小米固件上根据系统 Home 通知返回归藏；长按保持原行为。返回键留在归藏，小米原桌面可从专门入口进入。
- **减少空闲广告干扰**：归藏前台保持屏幕唤醒；对应旧固件可通过可恢复的 ADB 设置关闭小米广告屏保、配置开机目标和开机广告开关。安装 APK 本身不会自动完成全部系统配置。
- **壁纸预览内存控制**：首页图片最长边 1920，设置页预览采样至最长边 960；关闭设置释放预览引用，内存不足时保留原图。自用壁纸打包脚本输出优化 JPEG 和 320×180 缩略图。

## 实机与限制

实测电视：机身型号 **L58M5-4A**，MIUI TV **1.3.127**；ADB 内部型号 **MiTV4-ANSM0 / xmen**，Android **6.0.1 / API 23**。

本地自用版本已安装，月历、天气、壁纸连续预览 / 切换和返回首页已验证。此前完整软件重启后，用户确认“没有广告，进入归藏”，实体遥控器 Home 也能返回。公开构建仅去掉个人固定城市和未获公开分发许可的 Apple 图片；没有重新对公开包进行冷启动实机验收。

**不能保证任意小米电视无广告。** Home 可能短暂经过原桌面约 5 秒；原桌面是系统保护进程，本版没有彻底结束它。断电开机、待机唤醒和长期保活未完成验证；HDMI 入口的接线映射已配置，端口切换仍需用户实机验收。不要直接套用其他固件的系统包禁用命令。

## 安装与配置

从本仓库 [Releases](https://github.com/guehie/guizang-tv-custom/releases) 下载公开 APK，或自行构建。使用 ADB 时：

```sh
adb connect <电视IP>:5555
adb install -r guizang-tv-0.3.0-custom.6-public.1-android6.apk
adb shell am start -n io.github.op7418.guizangtv.custom/io.github.op7418.guizangtv.FrameHomeActivity
```

安装前在电视确认调试授权。与官方归藏包名不同，可并存；不要使用上游说明中的官方包名配置本版。入口改名位于「设置 → 首页内容」，主页键开关位于「设置 → 电视与启动」。开机 / 屏保配置、原值备份与恢复原则见 [CUSTOM.md](CUSTOM.md)。

本次公开 APK 是 Debug 签名测试包，不代表正式商店签名。保留签名密钥在维护者本机；其他人自行构建的 Debug 包可能无法覆盖维护者 APK。

## 壁纸与素材

公开代码及 APK 使用上游 5 张已署名的 Unsplash 照片，包括保留的「雾中雪岭」。[壁纸来源和许可](docs/WALLPAPERS.md)。

本地自用曾加入 7 张 Apple 经典 macOS 风景图片，但没有确认其公开分发授权，因此 **Apple 图片、本地 8 图目录及包含这些图片的自用 APK 不上传**。可从 [512 Pixels 壁纸档案](https://512pixels.net/projects/default-mac-wallpapers-in-5k/)了解来源，并在有权使用的前提下自行用 `scripts/prepare_wallpapers.py` 打包。照片不会因代码采用 MIT 而取得 MIT 许可。

## 构建

需要 JDK 17、Android SDK Platform 34 / Build Tools 34.0.0、Gradle（本地使用 8.14.3，上游 CI 使用 8.9）。

```sh
git clone https://github.com/guehie/guizang-tv-custom.git
cd guizang-tv-custom
gradle --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`。首次构建需下载构建依赖。桌面和月历离线可用，天气会联网；不会自动下载视频应用。公开源码不包含电视诊断数据、ADB 密钥或签名私钥。

上游原始说明保存在 [docs/UPSTREAM-README.md](docs/UPSTREAM-README.md)，其中下载地址和官方包名属于上游，不要与本定制版混用。
