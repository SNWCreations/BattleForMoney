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
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.inventory.ItemStack;
import snw.bfm.BattleForMoney;
import snw.bfm.ItemRegistry;
import snw.bfm.config.GameConfiguration;
import snw.bfm.game.GameController;
import snw.bfm.game.GameProcess;
import snw.bfm.game.TeamHolder;
import snw.bfm.tasks.GameStartTimer;
import snw.bfm.tasks.MainTimer;
import snw.bfm.util.LanguageSupport;

import static snw.bfm.util.CommandUtil.requireGame;
import static snw.bfm.util.CommandUtil.requireNoGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import snw.bfm.util.PlaceHolderString;

import java.util.HashSet;
import java.util.Set;

public class BFMGameCommand {
    private static final Set<String> seeTimerPlayers = new HashSet<>();

    public static Set<String> getSeeTimerPlayers() {
        return seeTimerPlayers;
    }

    public static void register() {
        new CommandAPICommand("bfmgame")
                .withPermission(CommandPermission.OP) // op operations in this command, so only op can use!
                .executes((sender, args) -> {
                    sender.sendMessage(ChatColor.GOLD + "--- BFMGame help ---");
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.bfmgame.help.start"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.bfmgame.help.stop"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.bfmgame.help.pause"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.bfmgame.help.resume"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.bfmgame.help.respawn"));
                })
                .withSubcommand(
                        new CommandAPICommand("start") // equals /start
                                .executes((sender, args) -> {
                                    start(sender, GameConfiguration.getReleaseTime());
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("start")
                                .withArguments(
                                        new IntegerArgument("startTime", 0)
                                        // if 0, the game will start without title and subtitle.
                                        // and translation of "game.process.start.broadcast" will appear.
                                )
                                .executes((sender, args) -> {
                                    start(sender, (int) args[0]);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("stop") // equals /forcestop
                                .executes((sender, args) -> {
                                    requireGame();
                                    BattleForMoney.getInstance().getGameProcess().stop(); // stop the game process
                                    Bukkit.broadcastMessage(ChatColor.RED + LanguageSupport.getTranslation("commands.forcestop.broadcast"));
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("pause") // equals /pause
                                .executes((sender, args) -> {
                                    requireGame();

                                    GameController controller = BattleForMoney.getInstance().getGameController();
                                    if (controller.isPaused()) {
                                        throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $game.status.already_paused$"));
                                    } else {
                                        controller.pause();
                                        sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.operation_success"));
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("resume") // equals /resume
                                .executes((sender, args) -> {
                                    resume(sender, false);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("resume") // equals /forceresume
                                .withArguments(
                                        new BooleanArgument("force")
                                )
                                .executes((sender, args) -> {
                                    resume(sender, (boolean) args[0]);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("item")
                                .withArguments(
                                        new StringArgument("itemName")
                                                .replaceSuggestions(
                                                        ArgumentSuggestions.strings(
                                                                suggestionInfo -> ItemRegistry.getRegisteredItemNames().toArray(new String[]{})
                                                        )
                                                )
                                )
                                .executesPlayer((sender, args) -> {
                                    ItemStack item = ItemRegistry.getRegisteredItemByName((String) args[0]);
                                    if (item != null) {
                                        sender.getInventory().addItem(item);
                                        sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.operation_success"));
                                    } else {
                                        throw CommandAPI.fail(
                                                new PlaceHolderString(
                                                        LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.bfmitem.item_not_found$")
                                                )
                                                        .replaceArgument("itemName", args[0])
                                                        .toString()
                                        );
                                    }
                                })
                )
                .register();

    }


    private static void start(CommandSender sender, int time) throws WrapperCommandSyntaxException {
        requireNoGame();

        TeamHolder holder = TeamHolder.getInstance();
        if (holder.getPlayers().isEmpty()) {
            throw CommandAPI.fail(LanguageSupport.getTranslation("commands.start.no_player_found"));
        } else {

            GameProcess newProcess = new GameProcess();
            GameController controller = new GameController(newProcess, GameConfiguration.getCoinPerSecond());
            if (time > 0) {
                newProcess.setHunterReleaseTimer(new GameStartTimer(time, newProcess));
                newProcess.setHunterNoMoveTime(time);
                Bukkit.broadcastMessage(ChatColor.RED + LanguageSupport.getTranslation("commands.start.starting"));
            }
            newProcess.setMainTimer(new MainTimer(GameConfiguration.getGameTime() * 60, controller));
            newProcess.start();

            BattleForMoney rfm = BattleForMoney.getInstance();
            rfm.setGameProcess(newProcess);
            rfm.setGameController(controller);
            sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.start.success"));
        }
    }

    private static void resume(CommandSender sender, boolean force) throws WrapperCommandSyntaxException {
        requireGame();

        BattleForMoney rfm = BattleForMoney.getInstance();
        GameController controller = rfm.getGameController();
        TeamHolder holder = TeamHolder.getInstance();

        if (holder.getPlayers().isEmpty() && !force) {
            throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.resume.no_player_online$"));
        } else {
            if (controller.isPaused()) {
                controller.resume();
                sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.operation_success"));
            } else {
                throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $game.status.already_running$"));
            }
        }
    }
}
