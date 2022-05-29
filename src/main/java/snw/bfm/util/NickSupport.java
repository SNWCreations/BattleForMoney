/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.util;

import org.bukkit.configuration.file.YamlConfiguration;

import snw.bfm.BattleForMoney;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

// by SNWCreations, 2022/4/4
public final class NickSupport {
    private static final Map<String, String> nickMap = new HashMap<>();

    private NickSupport() {
        throw new UnsupportedOperationException("No snw.rfm.util.NickSupport instances for you!");
    }

    public static void init() {
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(BattleForMoney.getInstance().getDataFolder(), "nickname.yml"));
        for (Map.Entry<String, Object> nameEntry : conf.getValues(false).entrySet()) {
            if (nameEntry.getValue() instanceof String) {
                nickMap.put(nameEntry.getKey(), (String) nameEntry.getValue());
            } else {
                BattleForMoney.getInstance().getLogger().warning("Cannot set " + nameEntry.getKey() + "'s nickname, because the value is not a String.");
            }
        }
    }

    public static String getNickName(String playerName) {
        return nickMap.getOrDefault(playerName, playerName);
    }
}
