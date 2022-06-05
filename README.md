# Battle For Money

[现在是中文版本] [Go to English Version](https://github.com/SNWCreations/BattleForMoney/blob/main/README.en_US.md)

这是 [SNWCreations/RunForMoney](https://github.com/SNWCreations/RunForMoney) 分支 dev/1.9 的 Fork 。

实现了 战斗中 游戏系统。

## 此插件的功能 & 特点

* 基本分队
* 管理员可以掌控游戏流程
* 可配置性强，配置文件有详细的注释，便于理解。
* 命令丰富，对管理员友好。
* 游戏数据查询方便。

查看此插件的命令请移步 [COMMANDS.md](https://github.com/SNWCreations/BattleForMoney/blob/main/COMMANDS.md) 文件。

### **注意:**
* 此插件支持的 Minecraft 版本: **1.16.X** ，作者已测试了 1.16.5 ，理论上 1.16 的所有子版本都可以使用。
* 任务需要自行实现。
* ~~**使用此插件时，需要把被用于游戏的地图重命名为 'world' (不带单括号)。**~~ 自 v1.8.0 起不再强制要求，但不提供合适的世界名会导致终止间位置不能被定义。
* 使用此插件时，需要把服务器配置的 "allow-flight" 设为 true 。
* **不可以使用 /reload 指令，否则已有游戏数据将全部丢失。**
* 如果要在一张地图内重复进行游戏，请注意记录 B币榜 的内容！因为在启动新游戏时 B币榜 的数据将会清空！

## 获取插件！

### Maven 构建

以下内容需要你已经安装了 Maven 。

自行 clone 此仓库，在目录根目录下运行命令 'mvn clean package' 即可。
构建完成后，在 target 文件夹下的 'bfm-X.X.X.jar' 即为成品。

### Releases

在右侧的 Releases 中下载！

## 一些声明

此作品 指 此仓库 下的所有由本人编写的文件及其产物。

此节内容永久有效，当此节内容有更新时，此作品自动受到 新的此节的内容 保护。

如果此节内容与 GPLv3 许可证的内容有冲突，以本节的内容为准。

**此作品 是我的个人作品，不是来自《全员战斗中》节目组官方的作品，且与《全员战斗中》节目没有关联。**

此作品 使用 GPLv3 作为许可证授权给您。详情请见 此仓库 的 LICENSE 文件。我对此作品保留所有可能的权利。

如果您使用 此作品 ，这意味着您同意并愿意遵守此节以及 GPLv3 许可证的所有规定。

## 特别感谢

[tr7zw's NBT API](https://www.spigotmc.org/resources/nbt-api.7939)

[Jetbrains](https://www.jetbrains.com) 提供的 [IntelliJ IDEA](https://www.jetbrains.com/idea)