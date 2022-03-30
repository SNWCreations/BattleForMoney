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

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GroupHolder extends ArrayList<Group> {
    private static final GroupHolder INSTANCE = new GroupHolder();

    @Nullable
    public Group findByName(@NotNull String name) {
        Validate.notNull(name);
        for (Group i : this) {
            if (Objects.equals(i.getName(), name)) {
                return i;
            }
        }
        return null;
    }

    @Nullable
    public Group findByPlayer(@NotNull Player player) {
        Validate.notNull(player);
        for (Group i : this) {
            if (i.contains(player.getName())) {
                return i;
            }
        }
        return null;
    }

    public static GroupHolder getInstance() {
        return INSTANCE;
    }

    public List<String> getGroupNames() {
        List<String> names = new ArrayList<>();
        this.forEach(e -> names.add(e.getName()));
        return names;
    }

    @Override
    public boolean add(Group players) {
        if (stream().anyMatch(IT -> Objects.equals(IT.getName(), players.getName()))) {
            throw new IllegalStateException(); // 不允许重名组存在。
        }
        return super.add(players);
    }
}
