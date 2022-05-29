/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import snw.bfm.Util;
import snw.bfm.game.RFMTeam;
import snw.bfm.game.TeamHolder;
import snw.bfm.util.CommandUtil;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.PlaceHolderString;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static snw.bfm.util.CommandUtil.requireNoGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class RFMTeamCommand {
    public static void register() {
        new CommandAPICommand("rfmteam")
                .executes(((sender, args) -> {
                    sender.sendMessage(ChatColor.GOLD + "--- RFMTeam help ---");
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.help.join"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.help.leave"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.help.list"));
                }))
                // equals /runner, /hunter. but more team were been added.
                .withSubcommand(new CommandAPICommand("join")
                        .withArguments(new StringArgument("teamName")
                                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                                                Util.getAllTheStringsStartingWithListInTheList(
                                                                info.currentArg(), TeamHolder.getInstance().getAllTeamName(), false)
                                                        .toArray(new String[]{})
                                        ))
                        ).executes((sender, args) -> {
                            join(sender, (String) args[0], new String[]{});
                        })
                )
                .withSubcommand(new CommandAPICommand("join")
                        .withPermission(CommandPermission.OP)
                        .withArguments(new StringArgument("teamName")
                                        .replaceSuggestions(ArgumentSuggestions.strings(info ->
                                                Util.getAllTheStringsStartingWithListInTheList(
                                                                info.currentArg(), TeamHolder.getInstance().getAllTeamName(), false)
                                                        .toArray(new String[]{}))
                                        )
                                , new GreedyStringArgument("players")
                                .replaceSuggestions(ArgumentSuggestions.strings(CommandUtil::suggestPlayerName))
                                // it works! :)
                                // but cannot work correctly in (Paper) console! other server implementation
                                // is not tested!
                        ).executes((sender, args) -> {
                            join(sender, (String) args[0], ((String) args[1]).split(" "));
                        })
                )
                .withSubcommand(new CommandAPICommand("leave")
                        .executes((sender, args) -> {
                            leave(sender, new String[]{});
                        })
                )
                .withSubcommand(new CommandAPICommand("leave") // equals /leaveteam
                        .withPermission(CommandPermission.OP)
                        .withArguments(new GreedyStringArgument("players")
                                .replaceSuggestions(ArgumentSuggestions.strings(CommandUtil::suggestPlayerName))
                        )
                        .executes(((sender, args) -> {
                            leave(sender, ((String) args[0]).split(" "));
                        }))
                )
                .withSubcommand(new CommandAPICommand("list") // equals /teamlist
                        .executes((sender, args) -> {
                            sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.list_header") + String.join(", ", TeamHolder.getInstance().getAllTeamName()));
                        })
                ).register();
    }

    private static void join(CommandSender sender, String teamName, String[] players) throws WrapperCommandSyntaxException {
        requireNoGame();

        RFMTeam team = TeamHolder.getInstance().getTeamByName(teamName);
        if (team == null) {
            throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.group.group_not_found$"));
        }

        if (players.length == 0) {
            if (sender instanceof Player) {
                team.add(sender.getName());
                sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.hunter.success"));
            } else {
                throw CommandAPI.fail(LanguageSupport.getTranslation("commands.not_enough_args"));
            }
        } else {
            if (!sender.isOp()) {
                throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.batch.op_required$"));
            } else {
                ArrayList<String> failed = new ArrayList<>();
                HashSet<String> realArgs = new HashSet<>(Arrays.asList(players));
                for (String i : realArgs) {
                    Player playerWillBeAdded = Bukkit.getPlayerExact(i);
                    if (playerWillBeAdded != null) {
                        team.add(i); // i'm stupid. I waste 20 minutes on it.
                        playerWillBeAdded.sendMessage(ChatColor.GREEN + new PlaceHolderString(LanguageSupport.getTranslation("commands.team.batch.success_to_player")).replaceArgument("teamName", team.getName()).toString());
                    } else {
                        failed.add(i);
                    }
                }
                sender.sendMessage(ChatColor.GREEN + new PlaceHolderString(LanguageSupport.getTranslation("commands.team.batch.success_count")).replaceArgument("count", realArgs.size() - failed.size()).replaceArgument("teamName", team.getName()).toString());
                if (!failed.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + new PlaceHolderString(LanguageSupport.getTranslation("commands.batch.failed_not_exists")).replaceArgument("count", failed.size()).toString());
                    sender.sendMessage(ChatColor.RED + LanguageSupport.getTranslation("commands.batch.failed_list_header") + String.join(", ", failed));
                }
            }
        }
    }

    private static void leave(CommandSender sender, String[] players) throws WrapperCommandSyntaxException {
        requireNoGame();
        
        TeamHolder holder = TeamHolder.getInstance(); // 2022/2/2 避免了重复获取。
        if (players.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                RFMTeam team = holder.getTeamByPlayer(player);
                if (team == null) {
                    throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.team.leave.not_in_team$"));
                } else {
                    team.remove(player.getName());
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.leave.success"));
                }
            } else {
                throw CommandAPI.fail(LanguageSupport.getTranslation("commands.player_required"));
            }
        } else {
            if (!sender.isOp()) {
                throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.batch.op_required$"));
            } else {
                ArrayList<String> failed = new ArrayList<>();
                HashSet<String> realArgs = new HashSet<>(Arrays.asList(players));
                for (String i : realArgs) {
                    Player playerWillBeRemoved = Bukkit.getPlayerExact(i);
                    if (playerWillBeRemoved != null) {
                        RFMTeam team = holder.getTeamByPlayer(playerWillBeRemoved);
                        if (team == null) {
                            sender.sendMessage(ChatColor.YELLOW +
                                    new PlaceHolderString(LanguageSupport.getTranslation("commands.team.leave.not_in_team_multi"))
                                            .replaceArgument("playerName", i).toString()
                            );
                        } else {
                            team.remove(playerWillBeRemoved.getName());
                            playerWillBeRemoved.sendMessage(ChatColor.YELLOW +
                                    new PlaceHolderString(LanguageSupport.getTranslation("commands.team.leave.batch_to_player"))
                                            .replaceArgument("teamName", team.getName()).toString()
                            );
                            sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.leave.success"));
                        }
                    } else {
                        failed.add(i);
                    }
                }
                sender.sendMessage(ChatColor.GREEN + new PlaceHolderString(LanguageSupport.getTranslation("commands.team.leave.success_count")).replaceArgument("count", realArgs.size() - failed.size()).toString());
                if (!failed.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + new PlaceHolderString(LanguageSupport.getTranslation("commands.batch.failed_not_exists")).replaceArgument("count", failed.size()).toString());
                    sender.sendMessage(ChatColor.RED + LanguageSupport.getTranslation("commands.batch.failed_list_header") + String.join(", ", failed)); // 2022/2/2 抄自己的代码结果没改。。。
                }
            }
        }
    }
}
