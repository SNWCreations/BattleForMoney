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
import dev.jorel.commandapi.arguments.LocationArgument;
import snw.bfm.ItemRegistry;
import snw.bfm.BattleForMoney;
import snw.bfm.commands.admin.RFMTimerCommand;
import snw.bfm.config.GameConfiguration;
import snw.bfm.config.Preset;
import snw.bfm.game.GameProcess;
import snw.bfm.game.TeamHolder;
import snw.bfm.processor.EventProcessor;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.NickSupport;
import snw.bfm.util.PlaceHolderString;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import static snw.bfm.Util.sortDescend;
import static snw.bfm.util.CommandUtil.requireGame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class RFMDataCommand {

    // SDF copied from ExportListCommand class.
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");

    public static void register() {
        new CommandAPICommand("rfmdata")
                // if no subcommand specified, this statement will be executed.
                .executes((sender, args) -> {
                    sender.sendMessage(ChatColor.GOLD + "--- RFMData help ---");
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmdata.help.coin"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmdata.help.exportcoin"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmdata.help.playerremaining"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmdata.help.timer"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmdata.help.settings"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmdata.help.endroom"));
                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmdata.help.reload"));
                })
                .withSubcommand(
                        new CommandAPICommand("coin") // equals /coinlist
                                .executes((sender, args) -> {
                                    Map<String, Double> coinEarned = sortDescend(BattleForMoney.getInstance().getCoinEarned());
                                    if (coinEarned.size() == 0) {
                                        throw CommandAPI.fail(LanguageSupport.getTranslation("commands.coinlist.empty"));
                                    } else {
                                        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + LanguageSupport.getTranslation("commands.coinlist.header"));
                                        int a = 0;
                                        for (Map.Entry<String, Double> keypair : coinEarned.entrySet()) {
                                            sender.sendMessage(ChatColor.GREEN + "" + ++a + ". " + keypair.getKey() + ": " + keypair.getValue());
                                        }
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("exportcoin") // equals /exportcoinlist
                                .withPermission(CommandPermission.OP) // only op can do this
                                .executes((sender, args) -> {
                                    BattleForMoney rfm = BattleForMoney.getInstance();
                                    Map<String, Double> coinEarned = rfm.getCoinEarned();
                                    if (coinEarned.size() == 0) {
                                        throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.coinlist.empty$"));
                                    } else {
                                        String date = SDF.format(new Date());
                                        String fileName = rfm.getDataFolder().getAbsolutePath() + File.separator + date + ".txt";
                                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) { // 2022/2/7 改用 try-with-resource 结构
                                            // region 写入头
                                            writer.write(LanguageSupport.getTranslation("commands.coinlist.header"));
                                            writer.newLine();
                                            writer.write(LanguageSupport.getTranslation("commands.exportlist.created_time") + date);
                                            writer.newLine();
                                            writer.newLine();
                                            // endregion
                                            int a = 0;
                                            for (Map.Entry<String, Double> e : coinEarned.entrySet()) {
                                                writer.write(++a + "." + e.getKey() + ": " + e.getValue()); // 写入排号，玩家名，B币数量
                                                writer.newLine(); // 换行，否则数据会成一大坨。。
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $commands.exportlist.unable_to_export$"));
                                        }
                                        sender.sendMessage(ChatColor.GREEN + LanguageSupport.replacePlaceHolder("$commands.operation_success$ $commands.exportlist.unable_to_export$"));
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("playerremaining") // equals /playerremaining
                                .executes((sender, args) -> {
                                    requireGame();

                                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.prc.runner_header") + String.join(", ", TeamHolder.getInstance().getRunners()));
                                    String nigText = String.join(",", TeamHolder.getInstance().getOutPlayers());
                                    Set<String> giveUp = TeamHolder.getInstance().getGiveUpPlayers();
                                    if (!giveUp.isEmpty()) {
                                        nigText = nigText + ", " + String.join(", ", giveUp);
                                    }
                                    sender.sendMessage(ChatColor.RED + LanguageSupport.getTranslation("commands.prc.not_in_game_header") + nigText);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("timer") // equals /rfmtimer
                                .executesPlayer((sender, args) -> {  // only player can do this
                                        if (RFMTimerCommand.getSeePlayers().contains(sender.getName())) {
                                            RFMTimerCommand.getSeePlayers().remove(sender.getName());
                                            sender.sendMessage(ChatColor.RED + LanguageSupport.getTranslation("commands.rfmtimer.disabled"));
                                        } else {
                                            RFMTimerCommand.getSeePlayers().add(sender.getName());
                                            sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rfmtimer.enabled"));
                                        }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("settings") // equals /rsq
                                .executes((sender, args) -> {
                                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rsq.hunter_release_time_default") + GameConfiguration.getReleaseTime());
                                    Location erl = GameConfiguration.getEndRoomLocation();
                                    //noinspection ConstantConditions
                                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rsq.endroom_location") +
                                            (erl != null ?
                                                    new PlaceHolderString(LanguageSupport.getTranslation("commands.rsq.endroom_part2"))
                                                            .replaceArgument("x", erl.getBlockX())
                                                            .replaceArgument("y", erl.getBlockY())
                                                            .replaceArgument("z", erl.getBlockZ())
                                                            .replaceArgument("worldName", erl.getWorld().getName())
                                                            .toString()
                                                    : LanguageSupport.getTranslation("commands.rsq.not_set_yet")));
                                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rsq.game_time") + GameConfiguration.getGameTime());
                                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rsq.coin_per_second") + GameConfiguration.getCoinPerSecond());
                                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.rsq.multiplier") + GameConfiguration.getCoinMultiplierOnBeCatched());
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("endroom")
                                .withPermission(CommandPermission.OP)
                                .executesPlayer((sender, args) -> {
                                    GameConfiguration.setEndRoomLocation(sender.getLocation());
                                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.operation_success"));
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("endroom")
                                .withPermission(CommandPermission.OP)
                                .withArguments(
                                        new LocationArgument("location")
                                )
                                .executesConsole(((sender, args) -> {
                                    GameConfiguration.setEndRoomLocation((Location) args[0]);
                                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.operation_success"));
                                }))
                )
                .withSubcommand(
                        new CommandAPICommand("reload") // equals /rfmreload
                                .withPermission(CommandPermission.OP)
                                .executes((sender, args) -> {
                                    BattleForMoney.getInstance().reloadConfig();
                                    LanguageSupport.loadLanguage(BattleForMoney.getInstance().getConfig().getString("language", "zh_CN"));
                                    GameConfiguration.init();
                                    Preset.init();
                                    NickSupport.init();
                                    ItemRegistry.unregisterItem("ep");
                                    ItemRegistry.unregisterItem("hpc");
                                    BattleForMoney.getInstance().registerInternalItems();
                                    EventProcessor.init();
                                    GameProcess.init();
                                    sender.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.operation_success"));
                                })
                )
                .register();
    }
}
