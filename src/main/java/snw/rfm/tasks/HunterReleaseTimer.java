/**
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.tasks;

import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import snw.rfm.RunForMoney;
import snw.rfm.game.GameProcess;
import snw.rfm.game.TeamHolder;
import snw.rfm.group.GroupHolder;
import snw.rfm.util.LanguageSupport;
import snw.rfm.util.PlaceHolderString;
import snw.rfm.util.SendingActionBarMessage;

public final class HunterReleaseTimer extends BaseCountDownTimer {
    private final GameProcess process;

    public HunterReleaseTimer(int time, GameProcess process) {
        super(time);
        Validate.notNull(process, "We need a process to bind!");
        this.process = process;
    }

    @Override
    protected void onZero() {
        TeamHolder holder = TeamHolder.getInstance();
        GroupHolder gh = GroupHolder.getInstance();

        // 2022/2/6 用 Stream 优化。
        holder.getHunters().stream().filter(IT -> gh.findByPlayer(IT) != null).forEach(holder::addEnabledHunter);

        new SendingActionBarMessage(new TextComponent(ChatColor.DARK_RED + "" + ChatColor.BOLD + LanguageSupport.getTranslation("event.hunter_released"))).start();
        process.setHunterReleaseTimer(null);
        process.getMainTimer().start(RunForMoney.getInstance());
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
                                    LanguageSupport.getTranslation("event.hunter_release_timer_message"))
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
