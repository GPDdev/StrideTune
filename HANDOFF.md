# 已完成：StrideTune 首版与 GitHub 发布

最终状态：首版 App 和 GitHub 发布已完成，无待办。源码、README 和截图已推送到 https://github.com/GPDdev/StrideTune ；v0.1.0 试用版 Release 已发布。构建检查通过，远端附件摘要与本地一致；模拟器验证来自此前开发，本次未重跑。

## 1. 当前目标
已在 `D:\Desktop\runmusic` 完成 Android 8.0+ 原生音乐播放器首版：本地音频 / 网易云公开歌单导入、简洁竖屏界面、单指调速、手势 / 灵敏度 / 上下限设置、后台播放。默认 Android 和变速不变调。项目名称 StrideTune，源码和试用版 APK Release 已上传到 GPDdev/StrideTune。无 iOS、自动测步频或自动 BPM 匹配。

## 2. 完成条件
已满足首版交付：`outputs/StrideTune-0.1.0.apk`、源码、README、真实截图和验证摘要均已生成；远程 main 已同步，v0.1.0 Release 的 APK、校验文件和验证摘要均已上传并核对。保留原音频、不收集账号或凭据、不承诺会员歌曲可播放。移除曲目只改曲库，不删除音频。

## 3. 已确认事实
- Java 原生 View、Media3 1.8.0、AGP 8.9.1、Gradle 8.11.1、compile / target SDK 35、min SDK 26。
- 公开网易云歌单 `3778678` 在 App 内导入 199 首，公开外链歌曲已在最终 APK 播放。没有账号登录、授权解锁或第三方服务器。
- 本地文件通过 ACTION_OPEN_DOCUMENT 和持久 URI 权限导入；唯一同名标题可自动匹配，重名须手工绑定。
- 倍速与进度在独立 `playback` SharedPreferences 保存，曲库及设置在 `runmusic` 保存，滑动不会重写曲库。
- 发布 APK SHA-256：`0e5ced6e3baa20fbba206bdb75ff4f0caa59d2100fee7db4f62dec84793ce86e`。

## 4. 相关文件与命令
- `app/src/main/java/org/hachile/runmusic` 为实现，`app/src/test` 为 6 个核心测试。
- `python scripts/bootstrap.py` 在 `.tools` 安装并校验官方 SDK / Gradle；`./scripts/build.ps1 -Check` 构建、JUnit、Lint 并复制交付 APK。两次构建通过，最后一次为状态存储优化之后。
- `apksigner verify --verbose outputs/RunMusic-0.1.0.apk` 通过 v2 签名校验，APK 4,619,012 字节。
- Android 15 模拟器 `emulator-5556` 已验证：本地文件选择 / 播放、左右滑动、手势锁、双击复位、自定义上下限、纵向手势、重启保存、后台 / 熄屏播放及网易云导入 / 播放。没有 AndroidRuntime 崩溃日志。
- `docs/interface.png` 为仓库内真实界面；`outputs/verification.json` 为验证摘要。`.tools` 中临时 smoke 脚本和日志是本次验证辅助，不属于 App 依赖。
- 本地 Git 分支 `main` 跟踪 `origin/main`，origin 为 https://github.com/GPDdev/StrideTune.git 。已核对远程源码；标签 `v0.1.0` 指向 `c67be66`，main 另含交接完成记录。输出、SDK、缓存及密钥不进入 Git；三个 Release 附件均为 uploaded，GitHub APK 摘要与上述值一致。

## 5. 已否决方案
- 不用网页代替 APK，不接管网易云 App 的播放，不支持 NCM 加密缓存解密，不提供会员限制绕过。
- 不自动把标题重名的本地文件绑定到网易云歌曲，避免匹配错误。
- SDK 官方清单的 35-ext15 曾被误选为基础 35，已修复选择逻辑并使用基础 `platform-35_r02.zip` 成功构建。

## 6. 风险与待确认项
未验证实体手机、蓝牙耳机、长时间续航、户外跑步和听感质量。网易云未登录公开接口没有稳定保证，可能部分返回、播放受限或仅试听。首次版本为调试签名 APK，应用商店发行需另做正式签名。Lint 有 11 条非阻断警告，具体在 README / 报告。无阻塞，无待办。Release 为 prerelease，链接为 https://github.com/GPDdev/StrideTune/releases/tag/v0.1.0 。

## 7. 下一步动作
无，任务已完成。
