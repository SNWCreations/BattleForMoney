/**
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.group;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import snw.rfm.game.TeamHolder;
import snw.rfm.util.LanguageSupport;
import snw.rfm.util.PlaceHolderString;

import java.util.HashSet;
import java.util.Optional;

public final class Group extends HashSet<String> {
    private final String name;
    private boolean activated = false;

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void activate() {
        if (activated) return; // we should not do anything when this group is already activated.
        TeamHolder holder = TeamHolder.getInstance();
        for (String i : this) {
            Player player = Bukkit.getPlayerExact(i);
            if (player != null) {
                holder.addEnabledHunter(player);
            } else {
                holder.addEnabledHunter(i);
            }
        }
        activated = true;
    }

    public void deactivate() {
        if (!activated) return;
        TeamHolder holder = TeamHolder.getInstance();
        for (String i : this) {
            Player player = Bukkit.getPlayerExact(i);
            if (player != null) {
                holder.removeEnabledHunter(player);
            } else {
                holder.removeEnabledHunter(i);
            }
        }
        activated = false;
    }

    @Override
    public void clear() {
        for (String i : this) {
            Optional.ofNullable(Bukkit.getPlayerExact(i))
                    .ifPresent(IT ->
                            IT.sendMessage(ChatColor.RED +
                                    new PlaceHolderString(LanguageSupport.getTranslation("commands.team.runner.leave_group"))
                                            .replaceArgument("groupName", getName())
                                            .toString()));
        }
        super.clear();
    }

    public boolean isActivated() {
        return activated;
    }
}
