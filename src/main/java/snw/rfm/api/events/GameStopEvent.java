/**
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.api.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import snw.rfm.game.TeamHolder;

import java.util.HashSet;
import java.util.Set;

public final class GameStopEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public Set<Player> getWinner() {
        Set<Player> result = new HashSet<>();
        TeamHolder.getInstance().getRunners().forEach(IT -> {
            Player player = Bukkit.getPlayerExact(IT);
            if (player != null) {
                result.add(player);
            }
        });
        return result;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}