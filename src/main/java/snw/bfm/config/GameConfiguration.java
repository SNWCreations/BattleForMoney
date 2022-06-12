/*
 * This file is part of BattleForMoney.
 *
 * BattleForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * BattleForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BattleForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import snw.bfm.BattleForMoney;
import snw.bfm.util.LanguageSupport;

import java.util.regex.Pattern;

public final class GameConfiguration {
    private static Location endRoomLocation;

    private GameConfiguration() {
        throw new UnsupportedOperationException("No snw.rfm.config.GameConfiguration instances for you!");
    }

    // 因为此方法中有些地方用到了 BattleForMoney.getInstance() 方法，所以不能作为 static 块。否则会因为 INSTANCE 未被设置从而导致 NullPointerException 。
    public static void init() {

        // region 终止间相关处理
        String el = BattleForMoney.getInstance().getConfig().getString("end_room_location");
        if (el == null) {
            BattleForMoney.getInstance().getLogger().warning(LanguageSupport.getTranslation("setup.config.invalid_endroom_location"));
        } else {
            if (getGameWorld() == null) {
                BattleForMoney.getInstance().getLogger().warning(LanguageSupport.getTranslation("setup.no_gameworld"));
                return;
            }
            try {
                String[] loc_split = Pattern.compile(" ", Pattern.LITERAL).split(el);
                endRoomLocation = new Location(getGameWorld(), Integer.parseInt(loc_split[0]), Integer.parseInt(loc_split[1]), Integer.parseInt(loc_split[2]));
            } catch (Exception e) {
                BattleForMoney.getInstance().getLogger().warning(LanguageSupport.getTranslation("setup.config.invalid_endroom_location"));
            }
        }
        // endregion
    }

    @Nullable
    public static Location getEndRoomLocation() {
        return endRoomLocation;
    }

    public static void setEndRoomLocation(Location END_ROOM) {
        endRoomLocation = END_ROOM;
    }

    public static int getGameStartTime() {
        return BattleForMoney.getInstance().getConfig().getInt("game_start_time", 60);
    }

    public static int getGameTime() {
        return BattleForMoney.getInstance().getConfig().getInt("game_time", 30);
    }

    public static int getCoinPerSecond() {
        int result = BattleForMoney.getInstance().getConfig().getInt("coin_per_second", 100);
        return (result > 0) ? result : 100;
    }

    public static World getGameWorld() {
        return Bukkit.getWorld(BattleForMoney.getInstance().getConfig().getString("gameworld", "world"));
    }

    public static int getDefaultGameCoinAmount() {
        return BattleForMoney.getInstance().getConfig().getInt("default_coin_amount", 180000);
    }

    public static int getKillReward() {
        return BattleForMoney.getInstance().getConfig().getInt("killreward", 50000);
    }

    public static int getExitTime() {
        return BattleForMoney.getInstance().getConfig().getInt("exit_time", 60);
    }
}
