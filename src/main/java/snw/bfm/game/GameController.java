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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.NickSupport;
import snw.bfm.util.PlaceHolderString;

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

    public boolean respawn(Player player) {
        Validate.notNull(player, "Uh, we need a player to respawn!");
        if (!player.isOnline() || TeamHolder.getInstance().isNotInGame(player)) { // 2022/4/8 如果传入猎人怎么办？？？
            return false;
        }
        player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + LanguageSupport.getTranslation("event.respawn"), "", 20, 40, 10);
        player.setGameMode(GameMode.ADVENTURE);
        TeamHolder.getInstance().removeOutPlayer(player);
        TeamHolder.getInstance().addPlayer(player);
        Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD +
                new PlaceHolderString(LanguageSupport.getTranslation("event.respawn_broadcast"))
                        .replaceArgument("playerName", NickSupport.getNickName(player.getName()))
        );
        return true;
    }
}
