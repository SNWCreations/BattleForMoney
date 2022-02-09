/**
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import snw.rfm.api.events.GameStopEvent;
import snw.rfm.game.TeamHolder;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Util {
    public static void registerCommand(@NotNull String cmdName, @NotNull CommandExecutor executor) {
        RunForMoney rfm = RunForMoney.getInstance();
        PluginCommand cmd = Bukkit.getPluginCommand(cmdName);
        if (cmd == null) {
            rfm.getLogger().log(Level.SEVERE, "命令 " + cmdName + " 注册失败。插件无法加载。");
            Bukkit.getPluginManager().disablePlugin(rfm);
        } else {
            cmd.setExecutor(executor);
            if (executor instanceof TabCompleter) {
                cmd.setTabCompleter((TabCompleter) executor);
            }
        }
    }

    @NotNull
    public static List<String> getAllTheStringsStartingWithListInTheList(@NotNull String a, @NotNull List<String> b, boolean ignoreCase) {
        return b.stream().filter(IT -> ((ignoreCase) ? IT.toLowerCase() : IT).startsWith((ignoreCase) ? a.toLowerCase() : a)).collect(Collectors.toList());
    }

    @NotNull
    public static List<String> getAllPlayersName() {
        List<String> result = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach((player -> result.add(player.getName())));
        return result;
    }

    // Map的value值降序排序
    public static <K, V extends Comparable<? super V>> Map<K, V> sortDescend(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> {
            int compare = (o1.getValue()).compareTo(o2.getValue());
            return -compare;
        });

        Map<K, V> returnMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            returnMap.put(entry.getKey(), entry.getValue());
        }
        return returnMap;
    }

    public static Location parseXYZStringIntoLocation(String loc) {
        String[] loc_split = loc.split(" ");
        return new Location(Bukkit.getWorld("world"), Integer.parseInt(loc_split[0]), Integer.parseInt(loc_split[1]), Integer.parseInt(loc_split[2]));
    }

    public static void removeAllPotionEffect(Player player) {
        Arrays.stream(PotionEffectType.values()).forEach(player::removePotionEffect);
    }

    public static void ifZeroStop() {
        if (RunForMoney.getInstance().getGameProcess() != null && TeamHolder.getInstance().getRunners().toArray().length == 0) {
            Bukkit.getPluginManager().callEvent(new GameStopEvent());
            RunForMoney.getInstance().getGameProcess().stop();
        }
    }
}
