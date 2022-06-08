# BattleForMoney - command manual

In fact, I copied them one by one according to the plugin.yml (laughs). Made some changes, more details.

Some commands with parameters will specify the command format, and those without parameters will not be written.

Parameters wrapped with `<>` are required parameters, and parameters wrapped with `[]` are optional parameters.

## /bfmgame

A command that performs basic operations on the runaway game.

Administrator privileges are required to execute this command and all subcommands of this command.

Usage: `/bfmgame <start/stop/pause/resume/respawn> [arguments..]`

parameter:

    start: start the game.
        startTime: This parameter affects the time before game start. By default, the value specified by the configuration item 'game_start_time' is used.
    stop: Force stop the game.
    pause: Pause the game.
    resume: resume the game.
    respawn: Respawn a player who is no longer in the game.
        playerName: The name of the player who will be resurrected.

## /bfmteam

Team system commands.

With the exception of the list subcommand, the other two commands are unavailable while in-game.

parameter:

    join: join a team.
        teamName: The name of the team that will be joined.
            players: (optional) When provided, all players listed in this parameter will be forced to join the specified team.
    leave: to leave the team.
        players: (optional) When provided, all players listed in this parameter will be forced to leave their team.
    list: List all defined teams.

## /rfmgroup

Orders about the hunter group.

parameter:

    join: Join a group.
        groupName: The name of the group to join.
    leave: leave the group.
    create: Create a group. Admin rights are required.
        groupName: The name of the group that will be created.
    delete: delete the group and notify all hunters in the specified group. Admin rights are required.
        groupName: The name of the group to be deleted.
    activate: activates all hunters in a group.
        groupName: The name of the group that will be activated.
    deactivate: Deactivates all hunters in a group.
        groupName: The name of the group that will be disabled.
    list: List all existing groups.

## /rfmdata

Commands about game data.

parameter:

    coin: Displays the coin leaderboard.
    exportcoin: Export the current coin list data to a file. Admin rights are required.
    playerremaining: Displays the names of all players who are alive and who are no longer in the game.
    timer: enable/disable the remaining time countdown.
    settings: Displays game settings.
    endroom: Set the endroom location
        location: Optional, this location will be used if the value is provided.
    reload: Reload plugin configuration from disk. Admin rights are required.
