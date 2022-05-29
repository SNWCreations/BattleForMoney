/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import snw.rfm.RunForMoney;
import snw.rfm.group.Group;
import snw.rfm.group.GroupHolder;
import snw.rfm.util.LanguageSupport;
import snw.rfm.util.PlaceHolderString;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public final class Preset {
    private static final Set<String> runners = new HashSet<>();
    private static final Set<String> hunters = new HashSet<>();
    private static final Map<String, Group> player_not_joined_groups = new HashMap<>();

    private Preset() {
        throw new UnsupportedOperationException("No snw.rfm.config.Preset instances for you!");
    }

    public static void init() {
        runners.clear();
        hunters.clear();
        player_not_joined_groups.clear();

        RunForMoney rfm = RunForMoney.getInstance();
        Logger l = rfm.getLogger();
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(new File(rfm.getDataFolder(), "presets.yml"));

        if (conf.getBoolean("IS_TEMPLATE")) {
            l.warning(LanguageSupport.getTranslation("setup.preset.template_warning"));
            return;
        }

        l.info(LanguageSupport.getTranslation("setup.preset.load_start"));
        List<String> runners_ = conf.getStringList("runners");
        List<String> hunters_ = conf.getStringList("hunters");

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
            runners.add(i.toLowerCase());
        }

        for (String i : hunters_) {
            hunters.add(i.toLowerCase());
        }

        Set<String> invalid = new HashSet<>();
        for (String i : runners) {
            if (hunters.contains(i)) {
                l.warning(new PlaceHolderString(LanguageSupport.getTranslation("setup.preset.repeat_playername"))
                        .replaceArgument("playername", i)
                        .toString());
                invalid.add(i);
            }
        }

        for (String s : invalid) { // 防止万恶的 ConcurrentModificationException
            hunters.remove(s);
            runners.remove(s);
        }

        ConfigurationSection groups = conf.getConfigurationSection("groups");
        if (groups == null) {
            l.info(LanguageSupport.getTranslation("setup.preset.no_group_requested"));
        } else {
            Set<String> gk = groups.getKeys(false);
            for (String k : gk) {
                Group groupWillBeCreated = GroupHolder.getInstance().findByName(k);
                if (groupWillBeCreated == null) {
                    groupWillBeCreated = new Group(k);
                    GroupHolder.getInstance().add(groupWillBeCreated);
                }
                List<String> willBeProcessed = groups.getStringList(k);
                for (String v : willBeProcessed) {
                    if (hunters.contains(v)) {
                        player_not_joined_groups.put(v, groupWillBeCreated);
                    }
                }
            }
            l.info(new PlaceHolderString(LanguageSupport.getTranslation("setup.preset.groups_created"))
                    .replaceArgument("count", gk.size())
                    .toString());
        }

        l.info("预设加载完成。");
    }

    public static boolean isPresetHunter(Player player) {
        return hunters.contains(player.getName().toLowerCase());
    }

    public static boolean isPresetRunner(Player player) {
        return runners.contains(player.getName());
    }

    @Nullable
    public static Group getPlayerNotJoinedGroup(Player player) {
        return player_not_joined_groups.get(player.getName());
    }
}
