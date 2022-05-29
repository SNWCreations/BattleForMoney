/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.processor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import snw.bfm.BattleForMoney;
import snw.bfm.api.ItemEventListener;
import snw.bfm.game.GameProcess;
import snw.bfm.game.TeamHolder;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.NickSupport;
import snw.bfm.util.PlaceHolderString;

public final class HunterPauseCardProcessor implements ItemEventListener, Listener {
    @Override
    public boolean onPlayerUseRequiredItem(Player player) {
        if (TeamHolder.getInstance().isRunner(player)) { // 排除使用者是猎人从而导致猎人坑队友的情况，哈哈哈哈哈哈哈笑死我了
            GameProcess process = BattleForMoney.getInstance().getGameProcess();
            if (process.getHunterReleaseTimer() == null) {
                int hpctime = BattleForMoney.getInstance().getConfig().getInt("hpc_time", 3);
                process.setHunterNoMoveTime(hpctime);
                Bukkit.broadcastMessage(ChatColor.GREEN +
                        new PlaceHolderString(LanguageSupport.getTranslation("event.hpc_used"))
                                .replaceArgument("playerName", NickSupport.getNickName(player.getName()))
                                .replaceArgument("time", hpctime).toString()
                );
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        GameProcess process = BattleForMoney.getInstance().getGameProcess();
        if (process != null
                && TeamHolder.getInstance().isHunter(event.getPlayer())
                && !process.isHunterCanMove()) {
            event.setCancelled(true);
        }
    }
}
