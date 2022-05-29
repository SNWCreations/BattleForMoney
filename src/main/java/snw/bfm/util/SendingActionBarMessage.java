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

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import snw.bfm.BattleForMoney;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class SendingActionBarMessage extends BukkitRunnable {
    private final TextComponent text;
    private final Collection<? extends Player> playersToSend;
    private int ticked = 0;
    private final int ticks;

    public SendingActionBarMessage(@NotNull TextComponent textToSend) {
        this(textToSend, Bukkit.getOnlinePlayers());
    }

    public SendingActionBarMessage(@NotNull TextComponent textToSend, int ticks) {
        this(textToSend, Bukkit.getOnlinePlayers(), ticks);
    }

    public SendingActionBarMessage(@NotNull TextComponent textToSend, @NotNull Collection<? extends Player> playersToSend) {
        this(textToSend, playersToSend, 20);
    }

    public SendingActionBarMessage(@NotNull TextComponent textToSend, @NotNull Collection<? extends Player> playersToSend, int ticks) {

        Validate.notNull(textToSend, "No text to send :(");
        Validate.notNull(playersToSend, "No players can receive this message :(");
        Validate.isTrue(ticks > 0, "The ticks cannot be negative or zero.");
        this.text = textToSend;
        this.playersToSend = playersToSend;
        this.ticks = ticks;
    }

    @Override
    public void run() {
        playersToSend.forEach((IT) -> IT.spigot().sendMessage(ChatMessageType.ACTION_BAR, text)); // 2022/2/6 用 Stream 优化。
        if (ticked++ >= ticks) {
            cancel();
        }
    }

    public void start() {
        super.runTaskTimer(BattleForMoney.getInstance(), 0L, 1L);
    }
}