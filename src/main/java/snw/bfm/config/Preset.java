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

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import snw.bfm.BattleForMoney;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.PlaceHolderString;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public final class Preset {
    private static final Set<String> players = new HashSet<>();
    private static final Set<String> ninja = new HashSet<>();

    private Preset() {
        throw new UnsupportedOperationException("No snw.rfm.config.Preset instances for you!");
    }

    public static void init() {
        players.clear();
        ninja.clear();

        BattleForMoney rfm = BattleForMoney.getInstance();
        Logger l = rfm.getLogger();
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(rfm.getDataFolder(), "presets.yml"));

        if (conf.getBoolean("IS_TEMPLATE")) {
            l.warning(LanguageSupport.getTranslation("setup.preset.template_warning"));
            return;
        }

        l.info(LanguageSupport.getTranslation("setup.preset.load_start"));
        List<String> runners_ = conf.getStringList("runners");
        List<String> hunters_ = conf.getStringList("ninja");

        if (runners_.isEmpty()) {
            l.warning(LanguageSupport.getTranslation("setup.preset.runners_empty"));
        }
        if (hunters_.isEmpty()) {
            l.warning(LanguageSupport.getTranslation("setup.preset.hunters_empty"));
            if (runners_.isEmpty()) {
                l.warning(LanguageSupport.getTranslation("setup.preset.no_playername_found"));
                return;
            }
        }


        for (String i : runners_) {
            players.add(i.toLowerCase());
        }

        for (String i : hunters_) {
            ninja.add(i.toLowerCase());
        }

        Set<String> invalid = new HashSet<>();
        for (String i : players) {
            if (ninja.contains(i)) {
                l.warning(new PlaceHolderString(LanguageSupport.getTranslation("setup.preset.repeat_playername"))
                        .replaceArgument("playername", i)
                        .toString());
                invalid.add(i);
            }
        }

        for (String s : invalid) { // 防止万恶的 ConcurrentModificationException
            ninja.remove(s);
            players.remove(s);
        }

        l.info("预设加载完成。");
    }

    public static boolean isPresetNinja(Player player) {
        return ninja.contains(player.getName().toLowerCase());
    }

    public static boolean isPresetPlayer(Player player) {
        return players.contains(player.getName().toLowerCase());
    }

}
