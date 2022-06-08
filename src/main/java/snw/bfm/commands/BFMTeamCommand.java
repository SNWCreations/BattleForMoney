/*
 * This file is part of BattleForMoney.
 *
 * BattleForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * BattleForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BattleForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import snw.bfm.game.TeamHolder;
import snw.bfm.util.CommandUtil;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.PlaceHolderString;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static snw.bfm.util.CommandUtil.requireNoGame;

import java.util.*;

public class BFMTeamCommand {
    public static void register() {
        new CommandAPICommand("bfmteam")
                .executes(((sender, args) -> {
                    sender.sendMessage(ChatColor.GOLD + "--- BFMTeam help ---");
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.help.join"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.help.leave"));
                }))
                .withSubcommand(new CommandAPICommand("join")
                        .withArguments(new MultiLiteralArgument("player", "ninja"))
                        .executes((sender, args) -> {
                            join(sender, (String) args[0], new String[]{});
                        })
                )
                .withSubcommand(new CommandAPICommand("join")
                        .withPermission(CommandPermission.OP)
                        .withArguments(new MultiLiteralArgument("player", "ninja")
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
                .register();
    }

    private static void join(CommandSender sender, String teamName, String[] players) throws WrapperCommandSyntaxException {
        requireNoGame();

        if (!Arrays.asList("player", "ninja").contains(teamName)) {
            throw CommandAPI.fail("Impossible situation happened!"); // Actually, this statement won't have chance to be executed.
        }

        Set<String> team = (Objects.equals(teamName, "player")) ? TeamHolder.getInstance().getPlayers() : TeamHolder.getInstance().getNinja();
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
                        team.add(i); // I'm stupid. I waste 20 minutes on it.
                        playerWillBeAdded.sendMessage(ChatColor.GREEN + new PlaceHolderString(LanguageSupport.getTranslation("commands.team.batch.success_to_player")).replaceArgument("teamName", teamName).toString());
                    } else {
                        failed.add(i);
                    }
                }
                sender.sendMessage(ChatColor.GREEN + new PlaceHolderString(LanguageSupport.getTranslation("commands.team.batch.success_count")).replaceArgument("count", realArgs.size() - failed.size()).replaceArgument("teamName", teamName).toString());
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
            if (sender instanceof Player player) {
                if (
                        !holder.getPlayers().remove(player.getName()) || !holder.getNinja().remove(player.getName())
                ) {
                    throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.team.leave.not_in_team$"));
                } else {
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
                        if (
                                !holder.getPlayers().remove(playerWillBeRemoved.getName()) || !holder.getNinja().remove(playerWillBeRemoved.getName())
                        ) {
                            sender.sendMessage(ChatColor.YELLOW +
                                    new PlaceHolderString(LanguageSupport.getTranslation("commands.team.leave.not_in_team_multi"))
                                            .replaceArgument("playerName", i).toString()
                            );
                        } else {
                            playerWillBeRemoved.sendMessage(ChatColor.YELLOW +
                                    LanguageSupport.getTranslation("commands.team.leave.batch_to_player")
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
