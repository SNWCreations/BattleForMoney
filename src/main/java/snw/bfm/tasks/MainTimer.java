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

import org.apache.commons.lang.Validate;
import snw.bfm.BattleForMoney;
import snw.bfm.game.GameController;
import snw.bfm.game.TeamHolder;

import java.util.Map;

public final class MainTimer extends BaseCountDownTimer {
    private final GameController controller;

    public MainTimer(int secs, GameController controller) {
        super(secs);
        Validate.notNull(controller, "No controller?");
        this.controller = controller;
    }

    @Override
    protected void onZero() {
        BattleForMoney.getInstance().getGameProcess().stop();
    }

    @Override
    protected void onNewSecond() {
        Map<String, Double> coinEarned = BattleForMoney.getInstance().getCoinEarned();
        for (String i : TeamHolder.getInstance().getPlayers()) {
            coinEarned.put(i,
                    Math.max(
                            coinEarned.getOrDefault(i, 0.00) - controller.getCoinPerSecond()
                            , 0.00)
            );
        }

        // removed reverse feature.

        // we won't add the task feature. unless somebody wants.
    }

    @Override
    public int getTimeLeft() {
        return super.getTimeLeft();
    }

    public void setRemainingTime(int remainingTime) {
        secs = remainingTime;
    }

}
