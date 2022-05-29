/**
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.game;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import snw.rfm.RunForMoney;
import snw.rfm.api.events.GamePauseEvent;
import snw.rfm.api.events.GameResumeEvent;
import snw.rfm.api.events.GameStopEvent;
import snw.rfm.commands.admin.RFMTimerCommand;
import snw.rfm.tasks.HunterReleaseTimer;
import snw.rfm.tasks.MainTimer;
import snw.rfm.util.LanguageSupport;
import snw.rfm.util.PlaceHolderString;
import snw.rfm.util.SendingActionBarMessage;

import java.util.stream.Collectors;

import static snw.rfm.Util.removeAllPotionEffect;

public final class GameProcess {
    private HunterReleaseTimer hrl;
    private MainTimer mainTimer;
    private int noMoveTime = 0;
    private static TextComponent yes;
    private static TextComponent no;

    public static void init() {
        yes = new TextComponent(LanguageSupport.getTranslation("game.process.stop_choice.yes"));
        yes.setColor(net.md_5.bungee.api.ChatColor.RED);
        yes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LanguageSupport.getTranslation("game.process.stop_choice.yes_hover"))));
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/forcestop"));
        no = new TextComponent(LanguageSupport.getTranslation("game.process.stop_choice.no"));
        no.setColor(net.md_5.bungee.api.ChatColor.GRAY);
        no.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LanguageSupport.getTranslation("game.process.stop_choice.no_hover"))));
        no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellraw @a {\"text\": \"" + LanguageSupport.getTranslation("game.process.stop_choice.no_say_content") + "\", \"color\": \"red\", \"bold\": true}"));
    }

    public void start() {
        RunForMoney rfm = RunForMoney.getInstance();
        rfm.getCoinEarned().clear();
        TeamHolder h = TeamHolder.getInstance();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (h.isHunter(p)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
            } else if (!h.isRunner(p)) {
                p.sendMessage(ChatColor.RED + LanguageSupport.getTranslation("game.process.start.no_team"));
                p.setGameMode(GameMode.SPECTATOR);
            }
        }
        Bukkit.getScheduler().runTaskTimer(rfm, () -> {
            int prev = getHunterNoMoveTime();
            if (prev > 0) {
                setHunterNoMoveTime(prev - 1);
            }
        }, 20L, 20L);

        if (hrl != null) {
            hrl.start(rfm);
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
            mainTimer.start(rfm);
        }

        Bukkit.getScheduler().runTaskTimer(RunForMoney.getInstance(), () -> {
            String sec = String.valueOf(mainTimer.getTimeLeft() % 60);
            new SendingActionBarMessage(
                    new TextComponent(LanguageSupport.getTranslation("game.time_remaining_actionbar") +
                            (mainTimer.getTimeLeft() / 60) + ":" + (sec.length() == 1 ? ("0" + sec) : sec)),
                    Bukkit.getOnlinePlayers().stream()
                            .filter(IT -> RFMTimerCommand.getSeePlayers().contains(IT.getName()))
                            .collect(Collectors.toList()))
                    .start();
        }, (hrl != null) ? (hrl.getTimeLeft() + 1) * 20L : 0L, 20L);
    }

    public void stop() {
        RunForMoney rfm = RunForMoney.getInstance();
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + LanguageSupport.getTranslation("game.process.stop.broadcast"));
        Bukkit.getScheduler().cancelTasks(rfm);
        for (Player p : Bukkit.getOnlinePlayers()) {
            removeAllPotionEffect(p);
            p.setGameMode(GameMode.ADVENTURE);
        }
        TeamHolder.getInstance().cleanup();
        rfm.setGameProcess(null);
        rfm.setGameController(null);
    }

    public void pause() {
        Bukkit.broadcastMessage(ChatColor.RED + LanguageSupport.getTranslation("game.process.pause.broadcast"));
        if (hrl != null) {
            hrl.cancel();
        } else {
            mainTimer.cancel();
        }
        Bukkit.getPluginManager().callEvent(new GamePauseEvent());
    }

    public void resume() {
        Bukkit.broadcastMessage(ChatColor.GREEN + LanguageSupport.getTranslation("game.process.resume.broadcast"));
        if (hrl != null) {
            HunterReleaseTimer nhrl = new HunterReleaseTimer(hrl.getTimeLeft(), this);
            hrl = nhrl;
            nhrl.start(RunForMoney.getInstance());
        } else { // 防止猎人还没放出就开始计算B币
            RunForMoney rfm = RunForMoney.getInstance();
            MainTimer mt = new MainTimer(mainTimer.getTimeLeft(), rfm.getGameController());
            mt.setTasks(mainTimer.getTasks());
            mainTimer = mt;
            mt.start(rfm);
        }
        Bukkit.getPluginManager().callEvent(new GameResumeEvent());
    }

    public void setHunterNoMoveTime(int time) {
        noMoveTime = time;
    }

    public boolean isHunterCanMove() {
        return noMoveTime <= 0;
    }

    public int getHunterNoMoveTime() {
        return noMoveTime;
    }

    public void checkStop() {
        if (TeamHolder.getInstance().getRunners().size() <= 0) {
            if (RunForMoney.getInstance().getConfig().getBoolean("stop_game_on_no_runner_alive", true)) {
                Bukkit.getPluginManager().callEvent(new GameStopEvent());
                stop();
            } else {
                if (hrl != null) {
                    hrl.cancel();
                } else {
                    mainTimer.cancel();
                }
                Bukkit.broadcastMessage(ChatColor.RED + LanguageSupport.getTranslation("game.process.stop_choice.broadcast"));
                Bukkit.getOnlinePlayers().stream()
                        .filter(ServerOperator::isOp)
                        .forEach(IT -> IT.spigot().sendMessage(ChatMessageType.CHAT, yes, no));
            }
        }
    }

    @Nullable
    public HunterReleaseTimer getHunterReleaseTimer() {
        return hrl;
    }

    public void setHunterReleaseTimer(HunterReleaseTimer hrl) {
        this.hrl = hrl;
    }

    public void setMainTimer(@Nullable MainTimer mainTimer) {
        this.mainTimer = mainTimer;
    }

    public MainTimer getMainTimer() {
        return mainTimer;
    }
}
