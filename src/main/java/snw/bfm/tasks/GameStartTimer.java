/**
 * This file is part of BattleForMoney.
 *
 * BattleForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * BattleForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BattleForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.tasks;

import net.md_5.bungee.api.chat.TextComponent;
import snw.bfm.BattleForMoney;
import snw.bfm.game.GameProcess;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.PlaceHolderString;
import snw.bfm.util.SendingActionBarMessage;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public final class GameStartTimer extends BaseCountDownTimer {
    private final GameProcess process;

    public GameStartTimer(int time, GameProcess process) {
        super(time);
        Validate.notNull(process, "We need a process to bind!");
        this.process = process;
    }

    @Override
    protected void onZero() {
        new SendingActionBarMessage(new TextComponent(ChatColor.DARK_RED + "" + ChatColor.BOLD + LanguageSupport.getTranslation("event.hunter_released"))).start();
        process.setHunterReleaseTimer(null);
        process.getMainTimer().start(BattleForMoney.getInstance());
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + LanguageSupport.getTranslation("game.process.start.broadcast"));
    }

    @Override
    protected void onNewSecond() {
        ChatColor color = null;
        if (secs == 30) {
            color = ChatColor.GREEN;
        } else if (secs == 15) {
            color = ChatColor.YELLOW;
        } else if (secs <= 10) {
            color = ChatColor.DARK_RED;
        }
        if (color != null) {
            new SendingActionBarMessage(
                    new TextComponent(
                            new PlaceHolderString(ChatColor.RED +
                                    LanguageSupport.getTranslation("event.start_timer_message"))
                                        .replaceArgument("time",
                                                color + "" + ChatColor.BOLD + secs + ChatColor.RESET + "" + ChatColor.RED)
                                    .toString())
                    , Bukkit.getOnlinePlayers()
            ).start();
        }
    }

    @Override
    public int getTimeLeft() {
        return super.getTimeLeft();
    }
}
