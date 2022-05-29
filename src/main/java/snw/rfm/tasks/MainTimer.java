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

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import snw.rfm.RunForMoney;
import snw.rfm.api.GameController;
import snw.rfm.api.events.GameStopEvent;
import snw.rfm.game.TeamHolder;
import snw.rfm.group.GroupHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class MainTimer extends BaseCountDownTimer {
    private final GameController controller;
    private List<ScheduledRFMTaskImpl> tasks = new ArrayList<>();

    public MainTimer(int secs, GameController controller) {
        super(secs);
        Validate.notNull(controller, "No controller?");
        this.controller = controller;
    }

    @Override
    public BukkitTask start(Plugin plugin) {
        TeamHolder.getInstance().getHunters().stream()
                .filter(IT -> GroupHolder.getInstance().findByPlayer(IT) == null)
                .filter(IT -> !TeamHolder.getInstance().isHunterEnabled(IT))
                .forEach(TeamHolder.getInstance()::addEnabledHunter);

        return super.start(plugin);
    }

    @Override
    protected void onZero() {
        Bukkit.getPluginManager().callEvent(new GameStopEvent());
        RunForMoney.getInstance().getGameProcess().stop();
    }

    @Override
    protected void onNewSecond() {
        Map<String, Double> coinEarned = RunForMoney.getInstance().getCoinEarned();
        for (String i : TeamHolder.getInstance().getRunners()) {
            coinEarned.put(i, Math.max(coinEarned.getOrDefault(i, 0.00) + controller.getCoinPerSecond(), 0.00));
        }

        if (controller.getCoinPerSecond() < 0) {
            secs = secs + 2; // 为什么不是 +1 ? 因为 -1 再 +1 不能实现倒流。
        }

        Iterator<ScheduledRFMTaskImpl> iter = tasks.iterator();
        while (iter.hasNext()) {
            ScheduledRFMTaskImpl task = iter.next();
            if (task.isCancelled() || task.isExecuted()) {
                iter.remove();
            } else if (task.getRequiredTime() == secs) {
                try { // 这样可以保证所有计划任务都会被正常执行
                    task.executeItNow();
                } catch (Exception e) { // 2022/3/30 一个程序不应该尝试 catch 一个 Error ，所以从 Throwable 改为 Exception
                    RunForMoney.getInstance().getLogger().warning("A scheduled task generated an exception.");
                    e.printStackTrace();
                }
                iter.remove();
            }
        }
    }

    @Override
    public int getTimeLeft() {
        return super.getTimeLeft();
    }

    public List<ScheduledRFMTaskImpl> getTasks() {
        return tasks;
    }

    public void setTasks(List<ScheduledRFMTaskImpl> tasks) {
        this.tasks = tasks;
    }

    public void setRemainingTime(int remainingTime) {
        secs = remainingTime;
    }

}
