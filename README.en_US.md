# Run For Money

[Go to Chinese Version](https://github.com/SNWCreations/BattleForMoney/blob/main/README.md) [English version now]

This plugin is my second work. It was made after watching the Minecraft version of the series "Run For Money" by @骑单车の茂茂 (https://space.bilibili.com/8957291).

## This plugin's functions & features

* Basic squad
* Administrators can control the game flow
* ~~There is a simple event mechanism, which can be monitored in the snw.rfm.api.events package.~~ **The API content is now split. See [RFM API](https://github.com/SNWCreations/BattleForMoneyAPI).**
* Strong configurability, the configuration file has detailed comments for easy understanding.
* Hunter grouping, suitable for certain tasks where hunters will be released if they fail, they can be released individually or in groups.
* Rich commands, friendly to administrators.
* Game data query is convenient.

To view the commands of this plugin, please go to the [COMMANDS.md](https://github.com/SNWCreations/BattleForMoney/blob/main/COMMANDS.md) file.

### **Notice:**
* Minecraft version supported by this plugin: **1.16.X** , the author has tested 1.16.5 , in theory all sub-versions of 1.16 can be used.
* The task needs to be implemented by itself.
* ~~**When using this plugin, you need to rename the map used in the game to 'world' (without single brackets).**~~ No longer required since v1.8.0, but not supplying a proper world name will cause the inter-terminator position to not be defined.
* When using this plugin, you need to set the "allow-flight" of the server configuration to true .
* **The /reload command cannot be used, otherwise all existing game data will be lost.**
* If you want to repeat the game in a map, please pay attention to record the content of the B Coin List! Because when starting a new game, the data of the B currency list will be cleared!

## Get the plugin!

### Maven build

The following requires you to have Maven installed.

To clone this repository by yourself, run the command 'mvn clean package' in the root directory of the directory.
After the build is complete, the 'rfm-X.X.X.jar' in the target folder is the finished product.

### Releases

Download it in Releases on the right!

## Explanation of some mechanisms of this plugin

If you still have questions, you can ask them in Issues.

### Teams & Groups

Teams can be understood as factions, in this plugin there are only two teams of hunters and runners.

The group is designed for administrators to better manage hunters, only hunters can become a member of a group.

Also, each hunter can only join one group.

## Some declarations

This work refers to all files and products written by me under this repository.

The content of this section is permanent. When the content of this section is updated, this work is automatically protected by the content of the new section.

In the event of a conflict between the contents of this section and the contents of the GPLv3 license, the contents of this section shall prevail.

**This work is my personal work, not an official work from the "Run For Money" program group, and is not related to the "Run For Money" program.**

This work is licensed to you under the GPLv3 license. See the LICENSE file in this repository for details. I reserve all possible rights to this work.

If you use this work, it means that you agree and agree to abide by this section and all provisions of the GPLv3 license.

## Special thanks to

[NBT API by tr7zw](https://www.spigotmc.org/resources/nbt-api.7939)

[IntelliJ IDEA](https://www.jetbrains.com/idea) by [Jetbrains](https://www.jetbrains.com)
