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

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import snw.rfm.RunForMoney;
import snw.rfm.config.GameConfiguration;

public final class PlayerExitRFMEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private double coin = Double.NaN;

    public PlayerExitRFMEvent(Player whoExited) {
        super(whoExited);
    }

    @Deprecated
    public Player getWhoExited() {
        return getPlayer();
    }

    public double getCoinEarned(boolean multiplier) {
        if (isModified()) return coin;
        double result = RunForMoney.getInstance().getCoinEarned().get(getPlayer().getName());
        return (multiplier) ? (result * GameConfiguration.getCoinMultiplierOnBeCatched()) : result;
    }

    public void setCoinEarned(double coin) {
        Validate.isTrue(!Double.isNaN(coin), "You cannot set player's coin to NaN");
        Validate.isTrue(coin > 0, "The coin amount cannot be negative");
        this.coin = coin;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isModified() {
        return !Double.isNaN(coin);
    }
}
