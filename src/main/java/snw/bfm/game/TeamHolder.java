/**
 * This file is part of BattleForMoney.
 *
 * BattleForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * BattleForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BattleForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.game;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public final class TeamHolder {
    private final Set<String> players = new HashSet<>();
    private final Set<String> outPlayers = new HashSet<>();
    private final Set<String> ninja = new HashSet<>();
    private final Set<String> giveUpPlayers = new HashSet<>();
    private static final TeamHolder INSTANCE = new TeamHolder();

    public static TeamHolder getInstance() {
        return INSTANCE;
    }

    private TeamHolder() {}

    public void addPlayer(Player player) {
        addPlayer(player.getName());
    }

    public void addPlayer(String player) {
        players.add(player);
        removeOutPlayer(player);
    }

    public void removePlayer(Player player) {
        removePlayer(player.getName());
    }

    public void removePlayer(String player) {
        players.remove(player);
    }

    public void addOutPlayer(Player player) {
        addOutPlayer(player.getName());
    }

    public void addOutPlayer(String player) {
        outPlayers.add(player);
    }

    public void removeOutPlayer(Player player) {
        removeOutPlayer(player.getName());
    }

    public void removeOutPlayer(String player) {
        outPlayers.remove(player);
    }
    public void addNinja(Player player) {
        addNinja(player.getName());
    }

    public void addNinja(String player) {
        ninja.add(player);
    }

    public void removeNinja(Player player) {
        removeNinja(player.getName());
    }

    public void removeNinja(String player) {
        outPlayers.remove(player);
    }

    public void addGiveUpPlayer(Player player) {
        addGiveUpPlayer(player.getName());
    }

    public void addGiveUpPlayer(String player) {
        giveUpPlayers.add(player);
    }

    public void removeGiveUpPlayer(Player player) {
        removeGiveUpPlayer(player.getName());
    }

    public void removeGiveUpPlayer(String player) {
        giveUpPlayers.remove(player);
    }

    public Set<String> getPlayers() {
        return players;
    }

    public Set<String> getOutPlayers() {
        return outPlayers;
    }

    public Set<String> getNinja() {
        return ninja;
    }

    public void cleanup() {
        players.clear();
        outPlayers.clear();
        ninja.clear();
        giveUpPlayers.clear();
    }

    public boolean isNotInGame(Player player) {
        return isNotInGame(player.getName());
    }

    public boolean isNotInGame(String player) {
        return !players.contains(player) && !outPlayers.contains(player) && !ninja.contains(player);
    }

    public Set<String> getGiveUpPlayers() {
        return giveUpPlayers;
    }
}
