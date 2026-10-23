# 定制版验证与配置说明

## 版本与公开范围

`0.3.0-custom.6-public.1` / versionCode 10，包名 `io.github.op7418.guizangtv.custom`，最低 API 23，目标 / 编译 API 34。基于 op7418/guizang-tv 的 v0.3.0。

功能变化见 [README](README.md)，来源和许可证见 [NOTICE](NOTICE.md)。公开版保留定制桌面、月历、天气、主页键和内存修复；与电视上的本地自用包相比，去掉个人备用城市 / 坐标、Apple 自用图片。定位缺失时不请求天气。签名私钥、原始电视 APK、实机诊断数据及局域网地址均不上传。

## 已验证与未验证

- 本地自用包：在 L58M5-4A / MiTV4-ANSM0 / Android 6.0.1 上安装成功，日期月历、农历节气、天气显示、连续浏览 7 张新增预览、应用壁纸和退出设置已实测；预览内存不足退出已修复。
- 此前用户确认：软件重启没有开机广告并进入归藏；实体 Home 可返回；小米原桌面入口可以进入并返回。
- 构建使用项目原有 Java 单元测试和 lint。最新公开构建结果见本仓库 Release 说明。
- 未验证：公开 APK 重新安装后的冷启动、断电上电、待机唤醒、长期保活；所有其他电视 / 固件；HDMI 1/2 的实际切换及遥控改名后的重启持久化。
- 局限：Home 有可能经过小米桌面约 5 秒；原桌面共享系统 UID 并被小爱和媒体服务绑定，普通后台结束无效。本版没有禁用它，也没有承诺返回后彻底终止其系统进程。

## 开机目标与广告开关

以下设置只在上述老固件读取、分析并验证过。**先读取原值并保存在本机，再更改；不要认为任意固件都会识别这些键。** 不需要 root 或卸载系统桌面。

```sh
adb shell settings get system start_3rd_app
adb shell settings get system boot_ad_switch
adb shell settings get system screen_saver_timeout
```

在归藏「设置 → 电视与启动」打开开机自动进入，然后设置：

```sh
adb shell settings put system start_3rd_app io.github.op7418.guizangtv.custom
adb shell settings put system boot_ad_switch 0
adb shell settings put system screen_saver_timeout -1
```

读回核对，并分别观察软件重启、断电开机、待机唤醒。上述实机原屏保阈值为 480000 毫秒；本版的前台保持唤醒标记只作用于归藏，离开后清除，遥控器电源行为保留。尚未单独完成本版超过原 8 分钟阈值的无人操作实测。

Home 返回服务需要在系统无障碍服务中启用 `io.github.op7418.guizangtv.custom/io.github.op7418.guizangtv.HomeKeyService`。如果固件没有可用菜单，可由熟悉 ADB 的使用者处理；修改 `enabled_accessibility_services` 时必须读取现有列表、仅追加本组件，保留小爱同学和其他服务，**不要用固定字符串覆盖整个列表**。

某些固件的电视管家可能清理服务。本机曾仅向现有 `rt_service_process_white_list` 和 `rt_pkg_white_list` 追加归藏包名，其他项保持原样；长期稳定性未验证。这些系统白名单不由 APK 自动写入，不建议未经核验套用于其他机型。

服务使用系统签名权限限制广播发送方，不读取屏幕内容或过滤其他按键。长按 Home 的 recentapps 通知不触发返回。原桌面入口先检查组件权限，必要时通过无障碍系统 Home 动作放行一次主动访问。

## 恢复

1. 关闭归藏中的开机自动进入和 Home 返回开关。
2. 把 `start_3rd_app`、`boot_ad_switch`、`screen_saver_timeout` 恢复为自己备份的原值；原值为 `null` 时使用 `settings delete` 删除新增键，不要把字符串 null 写入。
3. 若用 ADB 启用了服务，仅从无障碍列表删除归藏组件，保留其他项。系统白名单也只删除本次追加的归藏包名。
4. 确认原桌面、遥控器与开机正常后再卸载；本版没有卸载系统应用。

软件重启可能关闭电视的 ADB 调试；连接拒绝时需在电视重新开启。

## 天气、日历与隐私

天气仅读取小米系统已有的位置设置，坐标保留两位小数后发送到固定的 Open-Meteo HTTPS 地址；没有 IP 定位商或新增 GPS 权限。前台每 30 分钟刷新，失败后至少 5 分钟重试，超过 2 小时不显示旧温度。主页不显示定位说明行，详情页可查看来源和数据时间。公开版无固定城市兜底。

农历 / 节气来自香港天文台公开表格，支持 2025–2035 年并包含相邻年边界，按北京时间日期显示。Android 6 的天气连接仅补充公开的 ISRG Root X1/X2，保留证书链与主机名验证，不修改系统证书。

设置页预览最长边采样至 960 像素，首页最长边 1920；关闭设置释放预览引用，解码失败保留当前图。公开壁纸沿用上游已署名的 Unsplash 素材，自用 Apple 图片不分发。

## 公开构建结果（2026-10-23）

本地离线 assembleDebug、17 项既有 Java 单元测试、lint（0 errors / 11 warnings）通过；4 项既有 Python 壁纸打包检查通过。公开 APK 已核对不含 Apple 本地目录、电视诊断数据或私钥，包含 5 张上游公共壁纸与两份公开 CA 根证书。电视本地自用包保持原状，未为本次发布重新安装或重启电视。
