/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.game;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/* Represents a player in the game. */
public class InGamePlayer {

    /* Player name. */
    private final String player;

    /* The flags added to this player. */
    private final Set<String> flags;

    /*
     *  The already generated instances of this class will be stored in this set.
     *  Destroyed while the game stops.
     */
    private static final Set<InGamePlayer> cache = new HashSet<>();

    private InGamePlayer(String player) {
        Validate.notNull(player, "No player name?");
        this.player = player;
        this.flags = new HashSet<>();
    }

    @NotNull
    public static InGamePlayer of(@NotNull String player) {
        Validate.notNull(player, "No player name?");
        for (InGamePlayer a : cache) {
            if (Objects.equals(a.getName(), player)) {
                return a;
            }
        }
        InGamePlayer result = new InGamePlayer(player);
        cache.add(result);
        return result;
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    @NotNull
    public String getName() {
        return player;
    }

    public void addFlag(String flag) {
        flags.add(flag);
    }

    public void removeFlag(String flag) {
        flags.remove(flag);
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    // DO NOT USE THIS METHOD, IT IS DANGEROUS WHILE THE GAME IS RUNNING
    public static void destoryAll() {
        cache.clear();
    }
}
