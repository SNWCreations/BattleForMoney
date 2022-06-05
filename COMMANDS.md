# BattleForMoney - 命令说明书

其实是我一个个照着 plugin.yml 抄的 (笑) 。做了些修改，更详细。

一些有参数的命令会特别写明命令格式，没参数的不会写。

用 `<>` 包裹的参数为必填参数，用 `[]` 包裹的参数为可选参数。

## /bfmgame

一个对逃走中游戏进行基本操作的命令。

需要管理员权限来执行此命令以及此命令的所有子命令。

用法: `/bfmgame <start/stop/pause/resume/respawn> [arguments..]`

参数:

    start: 启动游戏。
        startTime: 此参数会影响猎人释放的时间。默认使用配置项 'hunter_release_time' 指定的值。
    stop: 强制停止游戏。
    pause: 暂停游戏。
    resume: 继续游戏。
    respawn: 复活某个已不在游戏中的玩家。
        playerName: 将被复活的玩家的名称。

## /rfmteam

队伍系统命令。

除了 list 子命令外，其他两个命令在游戏时不可用。

参数:

    join: 加入某队伍。
        teamName: 将加入的队伍的名称。
            players: (可选) 提供时，此参数内所有已列出的玩家将被强制加入指定的队伍。
    leave: 离开所在队伍。
        players: (可选) 提供时，此参数内所有已列出的玩家将强制从所在的队伍离开。
    list: 列出所有已定义的队伍。

## /rfmgroup

关于猎人组的命令。

参数:

    join: 加入某个组。
        groupName: 将要加入的组的名称。
    leave: 离开所在组。
    create: 创建组。需要管理员权限。
        groupName: 将要创建的组的名称。
    delete: 删除组，并且通知指定组内的所有猎人。需要管理员权限。
        groupName: 将要删除的组的名称。
    activate: 激活某个组的所有猎人。
        groupName: 将要激活的组的名称。
    deactivate: 禁用某个组的所有猎人。
        groupName: 将要禁用的组的名称。
    list: 列出所有已存在的组。

## /rfmdata

有关游戏数据的命令。

参数:

    coin: 显示硬币排行榜。
    exportcoin: 导出现在的硬币榜数据到一个文件。需要管理员权限。
    playerremaining: 显示所有存活的和已不在游戏中的玩家的名称。
    timer: 启用/禁用 剩余时间倒计时。
    settings: 显示游戏配置。
    endroom: 设置终止间位置。
        location: 可选参数，如果此参数被提供，则使用此参数的值。
    reload: 从磁盘重新加载插件配置。需要管理员权限。
