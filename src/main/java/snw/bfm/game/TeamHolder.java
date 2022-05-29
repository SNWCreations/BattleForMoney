/**
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
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import snw.bfm.group.GroupHolder;
import snw.bfm.util.LanguageSupport;

import java.util.*;
import java.util.stream.Collectors;

public final class TeamHolder {

    private final Map<String, RFMTeam> teams = new HashMap<>();
    private static final TeamHolder INSTANCE = new TeamHolder();

    /* TeamHolder 只能有一个实例，这个唯一实例可以通过 getInstance() 获取。 */
    private TeamHolder() {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException();
        }
    }

    public void init() {
        registerTeam(new RFMTeam("hunter", RFMTeam.Flags.LEAVE_OTHER_TEAM));
        registerTeam(new RFMTeam("runner", RFMTeam.Flags.LEAVE_OTHER_TEAM));
        registerTeam(new RFMTeam("out", RFMTeam.Flags.LEAVE_OTHER_TEAM));
        registerTeam(new RFMTeam("giveup", RFMTeam.Flags.LEAVE_OTHER_TEAM));
    }

    public boolean isRunner(Player player) {
        return getRunners().contains(player.getName());
    }

    public boolean isRunner(String player) {
        return getRunners().contains(player);
    }

    public boolean isHunter(Player player) {
        return isHunter(player.getName());
    }

    public boolean isHunter(String player) {
        return getHunters().contains(player);
    }

    public boolean isNotInGame(Player player) {
        return isNotInGame(player.getName());
    }

    public boolean isNotInGame(String player) {
        return !isHunter(player) && !isRunner(player);
    }

    public void addHunter(Player player) {
        if (isRunner(player)) {
            removeRunner(player);
            player.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.hunter.leave_runner_team"));
        }
        getHunters().add(player.getName());
    }

    public void addRunner(Player player) {
        if (isHunter(player)) {
            removeHunter(player);
            player.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("commands.team.runner.leave_hunter_team"));
        }
        getRunners().add(player.getName());
    }

    public void removeHunter(Player player) {
        getHunters().remove(player.getName());
        // 2022/2/1 修复移除猎人时未将其从所在组移除的错误。
        // 2022/2/2 改用 GroupHolder 内置方法。
        Optional.ofNullable(GroupHolder.getInstance().findByPlayer(player))
                .ifPresent(IT -> IT.remove(player.getName()));
    }

    public void removeRunner(Player player) {
        getRunners().remove(player.getName());
    }

    public boolean isHunterEnabled(Player player) {
        return isHunterEnabled(player.getName());
    }

    public boolean isHunterEnabled(String player) {
        if (!isHunter(player)) {
            return false;
        }
        return InGamePlayer.of(player).hasFlag("HUNTER_ENABLED");
    }

    public boolean isNoHunterFound() {
        return Bukkit.getOnlinePlayers().stream().noneMatch(this::isHunter);
    }

    public boolean isNoRunnerFound() {
        return Bukkit.getOnlinePlayers().stream().noneMatch(this::isRunner);
    }

    public void addEnabledHunter(Player player) {
        addEnabledHunter(player.getName()); // 2022/4/26 修复了未检查玩家是不是猎人的问题
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + LanguageSupport.getTranslation("event.hunter_activated"));
        player.setGameMode(GameMode.ADVENTURE);
    }

    public void addEnabledHunter(String player) {
        if (!isHunter(player)) {
            throw new IllegalStateException(); // you can't enable a non-hunter player.
        }
        InGamePlayer.of(player).addFlag("HUNTER_ENABLED");
    }

    // deprecated method in API v1.6.0, I will remove it in the future.
    public void setGiveUpPlayer(String player) {
        addGiveUpPlayer(player);
    }

    public void addGiveUpPlayer(String player) {
        getGiveUpPlayers().add(player);
        getRunners().remove(player); // 2022/4/23 if you give up, you should not in the runner team.
    }

    public void addOutPlayer(Player player) {
        addOutPlayer(player.getName());
    }

    public void removeOutPlayer(Player player) {
        removeOutPlayer(player.getName());
    }

    public void addOutPlayer(String player) {
        getOutPlayers().add(player);
    }

    public void removeOutPlayer(String player) {
        getOutPlayers().remove(player);
    }

    public void removeEnabledHunter(Player player) {
        removeEnabledHunter(player.getName());
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + LanguageSupport.getTranslation("event.runner_activated"));
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
    }

    public void removeEnabledHunter(String player) {
        InGamePlayer.of(player).removeFlag("HUNTER_ENABLED");
    }

    public void cleanup() {
        teams.values().forEach(RFMTeam::clear); // remove all players from their team
        InGamePlayer.destoryAll(); // destroy all cache
    }

    public RFMTeam getHunters() {
        return getTeamByName("hunter");
    }

    public RFMTeam getRunners() {
        return getTeamByName("runner");
    }

    public RFMTeam getOutPlayers() {
        return getTeamByName("out");
    }

    // deprecated method in API v1.6.0, I will remove it in the future.
    public String getGiveUpPlayer() {
        throw new UnsupportedOperationException();
    }

    public static TeamHolder getInstance() {
        return INSTANCE;
    }

    public void registerTeam(@NotNull RFMTeam team) {
        Validate.notNull(team);
        teams.put(team.getName(), team);
    }

    public RFMTeam getTeamByName(String name) {
        return teams.get(name);
    }

    public RFMTeam getTeamByPlayer(Player player) {
        return getTeamByPlayer(player.getName());
    }

    public RFMTeam getTeamByPlayer(String player) {
        for (RFMTeam team : teams.values()) {
            if (team.contains(player)) {
                return team;
            }
        }
        return null;
    }

    public Collection<String> getAllTeamName() {
        return teams.values().stream().map(RFMTeam::getName).collect(Collectors.toList());
    }

    public Set<String> getGiveUpPlayers() {
        return getTeamByName("giveup");
    }
}
