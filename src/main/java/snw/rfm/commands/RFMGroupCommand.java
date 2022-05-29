/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;
import snw.rfm.game.TeamHolder;
import snw.rfm.group.Group;
import snw.rfm.group.GroupHolder;
import snw.rfm.util.LanguageSupport;
import snw.rfm.util.PlaceHolderString;

import java.util.stream.Collectors;

import static snw.rfm.util.CommandUtil.requireGame;

public class RFMGroupCommand {
    public static void register() {
        new CommandAPICommand("rfmgroup")
                // if no subcommand specified, this statement will be executed
                .executes((sender, args) -> {
                    sender.sendMessage(ChatColor.GOLD + "--- RFMGroup help ---");
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgroup.help.join"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgroup.help.leave"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgroup.help.activate"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgroup.help.deactivate"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgroup.help.create"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgroup.help.delete"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgroup.help.list"));
                })
                .withSubcommand(
                        new CommandAPICommand("join")
                                .withArguments(
                                        new StringArgument("groupName")
                                                .replaceSuggestions(ArgumentSuggestions.strings(info -> GroupHolder.getInstance().getGroupNames().toArray(new String[]{})
                                                ))
                                )
                                .executesPlayer((sender, args) -> {
                                    requireGame();

                                    if (!TeamHolder.getInstance().isHunter(sender)) {
                                        throw CommandAPI.fail(LanguageSupport.getTranslation("commands.group.not_hunter"));
                                    } else {
                                        Group group = GroupHolder.getInstance().findByName((String) args[0]);
                                        if (group == null) {
                                            throw CommandAPI.fail(LanguageSupport.getTranslation("commands.group.not_found"));
                                        } else {
                                            Group anotherGroup = GroupHolder.getInstance().findByPlayer(sender);
                                            if (anotherGroup != null) {
                                                throw CommandAPI.fail(new PlaceHolderString(LanguageSupport.getTranslation("commands.group.join.already_in_a_group")).replaceArgument("groupName", anotherGroup.getName()).toString());
                                            } else {
                                                group.add(sender.getName());
                                                sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.operation_success"));
                                            }
                                        }
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("leave")
                                .executesPlayer((sender, args) -> {
                                    if (!TeamHolder.getInstance().isHunter(sender.getName())) {
                                        throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.group.not_hunter$"));
                                    } else {
                                        Group willLeave = GroupHolder.getInstance().findByPlayer(sender);
                                        if (willLeave == null) {
                                            throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.group.leave.not_in_a_group.single$"));
                                        } else {
                                            willLeave.remove(sender.getName());
                                            sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.operation_success"));
                                        }
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("create")
                                .withPermission(CommandPermission.OP) // only op can do this
                                .withArguments(
                                        new StringArgument("groupName")
                                )
                                .executes((sender, args) -> {
                                    String groupName = (String) args[0];
                                    GroupHolder groups = GroupHolder.getInstance();
                                    if (groups.findByName(groupName) != null) {
                                        throw CommandAPI.fail(ChatColor.RED + LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.group.new.already_exists$"));
                                    } else {
                                        groups.add(new Group(groupName));
                                        sender.sendMessage(ChatColor.GREEN + new PlaceHolderString(LanguageSupport.getTranslation("commands.group.new.success")).replaceArgument("groupName", groupName).toString());
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("delete")
                                .withPermission(CommandPermission.OP) // only op can do this
                                .withArguments(
                                        new StringArgument("groupName")
                                                .replaceSuggestions(ArgumentSuggestions.strings(info -> GroupHolder.getInstance().getGroupNames().toArray(new String[]{})))
                                )
                                .executes((sender, args) -> {
                                    GroupHolder holder = GroupHolder.getInstance();
                                    Group group = holder.findByName((String) args[0]);
                                    if (group == null) {
                                        sender.sendMessage(ChatColor.RED + LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.group.group_not_found$"));
                                    } else {
                                        group.clear();
                                        holder.remove(group);
                                        sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.operation_success"));
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("list")
                                .executes((sender, args) -> {
                                    GroupHolder holder = GroupHolder.getInstance(); // 2020/1/30 重构 GroupHolder
                                    int length = holder.size();
                                    if (length == 0) {
                                        sender.sendMessage(ChatColor.RED + LanguageSupport.getTranslation("commands.grouplist.empty"));
                                    } else {
                                        sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.grouplist.header") + holder.stream().map(Group::getName).collect(Collectors.joining(", ")));
                                        sender.sendMessage(ChatColor.GREEN + new PlaceHolderString(LanguageSupport.getTranslation("commands.grouplist.count")).replaceArgument("count", length).toString());
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("activate")
                                .withPermission(CommandPermission.OP)
                                .withArguments(
                                        new StringArgument("groupName")
                                                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                                                        GroupHolder.getInstance()
                                                                .stream()
                                                                .filter(Group::isActivated)
                                                                .map(Group::getName)
                                                                .toArray(String[]::new))
                                ))
                                .executes((sender, args) -> {
                                    String groupName = (String) args[0];

                                    Group groupWillBeActivated = GroupHolder.getInstance().findByName(groupName);
                                    if (groupWillBeActivated != null) {
                                        groupWillBeActivated.activate();
                                        sender.sendMessage(ChatColor.GREEN + new PlaceHolderString(LanguageSupport.getTranslation("commands.group.activate.success")).replaceArgument("groupName", groupWillBeActivated.getName()).toString());
                                    } else {
                                        throw CommandAPI.fail(new PlaceHolderString(LanguageSupport.getTranslation("commands.group.activate.failed")).replaceArgument("groupName", groupName).replaceTranslate().toString());
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("deactivate")
                                .withPermission(CommandPermission.OP)
                                .withArguments(
                                        new StringArgument("groupName")
                                                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                                                        GroupHolder.getInstance()
                                                                .stream()
                                                                .filter(IT -> !IT.isActivated())
                                                                .map(Group::getName)
                                                                .toArray(String[]::new))
                                                ))
                                .executes((sender, args) -> {
                                    String groupName = (String) args[0];

                                    Group groupWillBeDeactivated = GroupHolder.getInstance().findByName(groupName);
                                    if (groupWillBeDeactivated != null) {
                                        groupWillBeDeactivated.deactivate();
                                        sender.sendMessage(ChatColor.GREEN + new PlaceHolderString(LanguageSupport.getTranslation("commands.group.deactivate.success")).replaceArgument("groupName", groupWillBeDeactivated.getName()).toString());
                                    } else {
                                        throw CommandAPI.fail(new PlaceHolderString(LanguageSupport.getTranslation("commands.group.deactivate.failed")).replaceTranslate().toString());
                                    }
                                })
                )
                .register();
    }
}
