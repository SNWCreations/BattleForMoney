/*
 * This file is part of BattleForMoney.
 *
 * BattleForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * BattleForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BattleForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.game;

import org.apache.commons.lang.Validate;

public final class GameController {
    private boolean pause = false;
    private final GameProcess gameProcess;
    /** Remove the amount of this variable per second. */
    private final int coinPerSecond;

    public GameController(GameProcess process, int coinPerSecond) {
        Validate.notNull(process, "No process to controll?");
        this.gameProcess = process;
        Validate.isTrue(coinPerSecond != 0);
        this.coinPerSecond = coinPerSecond;
    }


    public int getCoinPerSecond() {
        return coinPerSecond;
    }

    public void pause() {
        if (pause) {
            throw new IllegalStateException();
        }
        pause = true;
        gameProcess.pause();
    }


    public void resume() {
        if (!pause) {
            throw new IllegalStateException();
        }
        pause = false; // 2022/4/2 修复了未把 pause 设为 false 的错误
        gameProcess.resume();
    }


    public boolean isPaused() {
        return pause;
    }

}
