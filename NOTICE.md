# 来源与许可

本定制版直接基于 [op7418/guizang-tv](https://github.com/op7418/guizang-tv) 正式版 v0.3.0（基线提交 `2c51b096d5fc34de6cff1add14bb79a60b5bdce0`），由 [guehie](https://github.com/guehie) 发布二次修改。保留该项目和 Gathin 的 MIT 版权，不将整个项目宣称为从零编写。以下还包括上游原有的参考和素材来源。

- 初始工程参考与衍生基础：Gathin23/cleanyourtv，MIT License。
  - https://github.com/Gathin23/cleanyourtv
  - 本地定制基线：`69c93b66a75c70a9e386adff327b6a51c12addc5`
  - 原始版权：Copyright (c) 2026 Gathin，保留于根目录 LICENSE。
- 设计与设置交互参考：nthnclrk/CleanFrame，Nathan Clark。
  - https://github.com/nthnclrk/CleanFrame
  - 参考版本：`fa537d1892b71d2de2bf94c24cbff3812e9f70d5`
  - 参考其分组、首页预览、焦点与已应用值分离、确认应用和逐层返回的交互。
  - 本项目没有打包其 Tizen 代码、字体或照片。
  - 图标采用下述开源 SVG，不打包 Android 应用的原始图标。
- 默认渐变、布局示意及本项目绘制代码采用 MIT 许可。
- 本地自用版 Android 6 兼容实现参考 SinHom 提交的 [上游 PR #2](https://github.com/op7418/guizang-tv/pull/2)：替换旧 Android 不具备的 Java 集合和函数接口，并用 `/proc/stat` 的 `btime` 识别完整开机。本地实现保留无法确认启动时跳过的保护；这部分兼容移植没有引入该 PR 中的天气等扩展。
- custom.5 的农历月首与节气日期来自香港天文台[公历与农历日期对照表](https://www.hko.gov.hk/tc/gts/time/conversion.htm)，原始年度文本例如 [2026 年](https://www.hko.gov.hk/tc/gts/time/calendar/text/files/T2026c.txt)。`calendar.tsv` 仅保存月首与节气日期，转换为简体标签，支持显示 2025–2035 年并包含相邻年边界数据；资料来源为香港天文台，不表示天文台认可或担保本应用。
- custom.5 天气来自 [Open-Meteo](https://open-meteo.com/)，数据按 [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) 使用；应用自行将 WMO 天气代码转换为中文、温度四舍五入显示。天气端点用于个人非商业场景，服务使用需遵守 Open-Meteo 条款；本版未包含广告 SDK。
- Android 6 天气连接使用的公开 ISRG Root X1 / X2 证书来自 [Let's Encrypt 官方证书页面](https://letsencrypt.org/certificates/)，仅补充该应用天气连接的信任锚点，保留证书链与主机名验证，不修改电视系统证书。
- 品牌 SVG：Simple Icons，CC0-1.0；工具 SVG：Phosphor Icons Fill（勾选与箭头为 Regular），MIT。原 SVG、精确提交来源与许可证位于 `app/src/main/assets/icons/`；Android VectorDrawable 是同一路径数据的格式转换。商标权不因图标的开源许可而转移。
- 使用 Android 系统字体，不打包 Outfit 字体。
- Android Gradle Plugin、Gradle 与 JUnit 是构建或测试依赖，各自适用其许可证；不代表被重新许可为 MIT。
- 各产品与商标归其权利人所有。本项目不隶属于小米、Sony、Apple 或哔哩哔哩。

v0.1.1 起，内置五张经原页面核对的 Unsplash 照片，适用 **Unsplash License**，不适用代码的 MIT 许可。逐张署名、原始链接和处理方式见 [壁纸说明](docs/WALLPAPERS.md)，许可通知也随 APK 打包。

用户自行加入的照片不因放入本工程而自动获得 MIT 许可。其他未授权公开的私人试用素材、实机诊断数据、签名密钥均不在公开源代码或官方 APK 中。

本次 GitHub 公开版不分发 Apple 图片。custom.6 的本地自用壁纸包曾包含 7 张 Apple 经典 macOS 风景图片，取自 [512 Pixels macOS Wallpaper Archive](https://512pixels.net/projects/default-mac-wallpapers-in-5k/)。图片版权归 Apple，档案版本可能经过尺寸整理，不适用代码 MIT 许可；个人构建素材放在 Git 忽略目录，未公开上传。具体来源和处理规格随本地包保存在 APPLE-NOTICE.txt 与 catalog.json 中。
