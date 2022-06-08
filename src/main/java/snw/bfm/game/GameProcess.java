/**
 * This file is part of BattleForMoney.
 *
 * BattleForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * BattleForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BattleForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.game;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import snw.bfm.BattleForMoney;
import snw.bfm.ItemRegistry;
import snw.bfm.commands.BFMGameCommand;
import snw.bfm.config.GameConfiguration;
import snw.bfm.tasks.GameStartTimer;
import snw.bfm.tasks.MainTimer;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.PlaceHolderString;
import snw.bfm.util.SendingActionBarMessage;

import java.util.stream.Collectors;

import static snw.bfm.util.Util.removeAllPotionEffect;

public final class GameProcess {
    private GameStartTimer hrl;
    private MainTimer mainTimer;
    private int noMoveTime = 0;

    public void start() {
        BattleForMoney bfm = BattleForMoney.getInstance();
        bfm.getCoinEarned().clear();
        TeamHolder h = TeamHolder.getInstance();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (h.isNotInGame(p)) {
                p.sendMessage(ChatColor.RED + LanguageSupport.getTranslation("game.process.start.no_team"));
                p.setGameMode(GameMode.SPECTATOR);
            } else {
                p.getInventory().addItem(ItemRegistry.getRegisteredItemByName("fightball"));
                bfm.getCoinEarned().put(p.getName(), (double) GameConfiguration.getDefaultGameCoinAmount()); // init the default game coin amount
            }
        }
        Bukkit.getScheduler().runTaskTimer(bfm, () -> {
            int prev = getHunterNoMoveTime();
            if (prev > 0) {
                setHunterNoMoveTime(prev - 1);
            }
        }, 20L, 20L);

        if (hrl != null) {
            hrl.start(bfm);
            Bukkit.getOnlinePlayers().forEach(IT ->
                    IT.sendTitle(ChatColor.RED + "" + ChatColor.BOLD +
                                    LanguageSupport.getTranslation("game.process.start.title"),
                    ChatColor.DARK_RED + "" + ChatColor.BOLD +
                            new PlaceHolderString(
                                    LanguageSupport.getTranslation("game.process.start.subtitle"))
                                    .replaceArgument("time", hrl.getTimeLeft()),
                            20, 60, 10)
            );
        } else {
            mainTimer.start(bfm);
        }

        Bukkit.getScheduler().runTaskTimer(BattleForMoney.getInstance(), () -> {
            String sec = String.valueOf(mainTimer.getTimeLeft() % 60);
            new SendingActionBarMessage(
                    new TextComponent(LanguageSupport.getTranslation("game.time_remaining_actionbar") +
                            (mainTimer.getTimeLeft() / 60) + ":" + (sec.length() == 1 ? ("0" + sec) : sec)),
                    Bukkit.getOnlinePlayers().stream()
                            .filter(IT -> BFMGameCommand.getSeeTimerPlayers().contains(IT.getName()))
                            .collect(Collectors.toList()))
                    .start();
        }, (hrl != null) ? (hrl.getTimeLeft() + 1) * 20L : 0L, 20L);
    }

    public void stop() {
        BattleForMoney bfm = BattleForMoney.getInstance();
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + LanguageSupport.getTranslation("game.process.stop.broadcast"));
        Bukkit.getScheduler().cancelTasks(bfm);
        for (Player p : Bukkit.getOnlinePlayers()) {
            removeAllPotionEffect(p);
            p.setGameMode(GameMode.ADVENTURE);
            p.getInventory().remove(Material.SNOWBALL);
        }
        TeamHolder.getInstance().cleanup();
        bfm.setGameProcess(null);
        bfm.setGameController(null);
    }

    public void pause() {
        Bukkit.broadcastMessage(ChatColor.RED + LanguageSupport.getTranslation("game.process.pause.broadcast"));
        if (hrl != null) {
            hrl.cancel();
        } else {
            mainTimer.cancel();
        }
    }

    public void resume() {
        Bukkit.broadcastMessage(ChatColor.GREEN + LanguageSupport.getTranslation("game.process.resume.broadcast"));
        if (hrl != null) {
            GameStartTimer nhrl = new GameStartTimer(hrl.getTimeLeft(), this);
            hrl = nhrl;
            nhrl.start(BattleForMoney.getInstance());
        } else { // 防止猎人还没放出就开始计算B币
            BattleForMoney bfm = BattleForMoney.getInstance();
            MainTimer mt = new MainTimer(mainTimer.getTimeLeft(), bfm.getGameController());
            mainTimer = mt;
            mt.start(bfm);
        }
    }

    public void setHunterNoMoveTime(int time) {
        noMoveTime = time;
    }

    public int getHunterNoMoveTime() {
        return noMoveTime;
    }

    public void setHunterReleaseTimer(GameStartTimer hrl) {
        this.hrl = hrl;
    }

    public void setMainTimer(@Nullable MainTimer mainTimer) {
        this.mainTimer = mainTimer;
    }

    public MainTimer getMainTimer() {
        return mainTimer;
    }
}
