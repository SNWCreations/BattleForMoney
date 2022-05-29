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
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import snw.bfm.BattleForMoney;
import snw.bfm.api.events.GamePostStartEvent;
import snw.bfm.api.events.GamePreStartEvent;
import snw.bfm.config.GameConfiguration;
import snw.bfm.game.GameController;
import snw.bfm.game.GameProcess;
import snw.bfm.game.TeamHolder;
import snw.bfm.tasks.HunterReleaseTimer;
import snw.bfm.tasks.MainTimer;
import snw.bfm.util.LanguageSupport;

import static snw.bfm.util.CommandUtil.requireGame;
import static snw.bfm.util.CommandUtil.requireNoGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RFMGameCommand {
    public static void register() {
        new CommandAPICommand("rfmgame")
                .withPermission(CommandPermission.OP) // op operations in this command, so only op can use!
                .executes((sender, args) -> {
                    sender.sendMessage(ChatColor.GOLD + "--- RFMGame help ---");
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgame.help.start"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgame.help.stop"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgame.help.pause"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgame.help.resume"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmgame.help.respawn"));
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
                                        new IntegerArgument("hunterReleaseTime", 0)
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

                                    snw.rfm.api.GameController controller = BattleForMoney.getInstance().getGameController();
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
                        new CommandAPICommand("respawn") // equals /rfmrespawn
                                .withArguments(
                                        new PlayerArgument("playerToRespawn")
                                )
                                .executes((sender, args) -> {
                                    requireGame();
                                    BattleForMoney.getInstance().getGameController().respawn((Player) args[0]);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("respawn")
                                .withArguments(
                                        new PlayerArgument("playerToRespawn"),
                                        new LocationArgument("location")
                                )
                                .executes((sender, args) -> {
                                    requireGame();
                                    BattleForMoney.getInstance().getGameController().respawn((Player) args[0]);
                                    ((Player) args[0]).teleport((Location) args[1]);
                                })
                )
                .register();

    }


    private static void start(CommandSender sender, int time) throws WrapperCommandSyntaxException {
        requireNoGame();

        TeamHolder holder = TeamHolder.getInstance();
        if (holder.isNoHunterFound()) {
            throw CommandAPI.fail(LanguageSupport.getTranslation("commands.start.no_hunter_found"));
        } else if (holder.isNoRunnerFound()) {
            throw CommandAPI.fail(LanguageSupport.getTranslation("commands.start.no_runner_found"));
        } else {
            Bukkit.getPluginManager().callEvent(new GamePreStartEvent());

            GameProcess newProcess = new GameProcess();
            GameController controller = new GameController(newProcess, GameConfiguration.getCoinPerSecond());
            if (time > 0) {
                newProcess.setHunterReleaseTimer(new HunterReleaseTimer(time, newProcess));
                newProcess.setHunterNoMoveTime(time);
                Bukkit.broadcastMessage(ChatColor.RED + LanguageSupport.getTranslation("commands.start.starting"));
            }
            newProcess.setMainTimer(new MainTimer(GameConfiguration.getGameTime() * 60, controller));
            newProcess.start();

            BattleForMoney rfm = BattleForMoney.getInstance();
            rfm.setGameProcess(newProcess);
            rfm.setGameController(controller);
            sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.start.success"));

            Bukkit.getPluginManager().callEvent(new GamePostStartEvent());

        }
    }

    private static void resume(CommandSender sender, boolean force) throws WrapperCommandSyntaxException {
        requireGame();

        BattleForMoney rfm = BattleForMoney.getInstance();
        snw.rfm.api.GameController controller = rfm.getGameController();
        TeamHolder holder = TeamHolder.getInstance();

        if (holder.isNoHunterFound() || holder.isNoRunnerFound() && !force) {
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
