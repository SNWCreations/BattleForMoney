/**
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.group;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public final class GroupHolder extends ArrayList<Group> {
    private static final GroupHolder INSTANCE = new GroupHolder();

    private GroupHolder() {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException();
        }
    }

    @Nullable
    public Group findByName(@NotNull String name) {
        Validate.notNull(name, "No name to match...");
        for (Group i : this) {
            if (Objects.equals(i.getName(), name)) {
                return i;
            }
        }
        return null;
    }

    @Nullable
    public Group findByPlayer(@NotNull Player player) {
        Validate.notNull(player, "No player to match...");
        return findByPlayer(player.getName());
    }

    @Nullable
    public Group findByPlayer(@NotNull String player) {
        Validate.notNull(player, "No player to match...");
        for (Group i : this) {
            if (i.contains(player)) {
                return i;
            }
        }
        return null;
    }

    public static GroupHolder getInstance() {
        return INSTANCE;
    }

    public Collection<String> getGroupNames() {
        return stream().map(Group::getName).collect(Collectors.toList());
    }

    @Override
    public boolean add(Group players) {
        if (stream().anyMatch(IT -> Objects.equals(IT.getName(), players.getName()))) {
            throw new IllegalStateException(); // 不允许重名组存在。
        }
        return super.add(players);
    }
}
