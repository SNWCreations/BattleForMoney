/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.game;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

// Represents a team in RFM plugin.
public class RFMTeam extends HashSet<String> {
    private final String name;
    private final Flags[] flags;


    public RFMTeam(@NotNull String name, Flags... flags) {
        Validate.notNull(name, "No name?");
        Validate.isTrue(!name.isEmpty(), "Name cannot be empty.");
        Validate.isTrue(name.split(" ").length == 1, "Name cannot have spaces.");
        this.name = name;
        this.flags = flags;
    }


    public enum Flags {
        /* Only hunter can join this team. */
        NO_HUNTER,

        /* Only runner can join this team. */
        NO_RUNNER,

        /* Only "spectator" can join this team. */
        NOT_IN_GAME_ONLY,

        /* The player will leave his team before joining this team. */
        LEAVE_OTHER_TEAM
    }

    @Override
    public boolean add(String s) {
        if ((TeamHolder.getInstance().isRunner(s) && Arrays.stream(flags).anyMatch(IT -> IT == Flags.NO_RUNNER))
          || (TeamHolder.getInstance().isHunter(s) && Arrays.stream(flags).anyMatch(IT -> IT == Flags.NO_HUNTER))
          || (!TeamHolder.getInstance().isNotInGame(s) && Arrays.stream(flags).anyMatch(IT -> IT == Flags.NOT_IN_GAME_ONLY))
        ) {
            return false;
        }
        if (Arrays.stream(flags).anyMatch(IT -> IT == Flags.LEAVE_OTHER_TEAM)) {
            Optional.ofNullable(TeamHolder.getInstance().getTeamByPlayer(s)).ifPresent(IT -> IT.remove(s));
        }
        return super.add(s);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "name='" + name + '\'' +
                ", flags=" + Arrays.toString(flags) +
                '}';
    }
}
