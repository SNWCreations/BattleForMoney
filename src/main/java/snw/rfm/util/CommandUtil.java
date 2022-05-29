/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.util;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import snw.rfm.RunForMoney;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommandUtil {
    public static void requireNoGame() throws WrapperCommandSyntaxException {
        if (RunForMoney.getInstance().getGameProcess() != null) {
            throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $game.status.already_running$"));
        }
    }

    public static void requireGame() throws WrapperCommandSyntaxException {
        if (RunForMoney.getInstance().getGameProcess() == null) {
            throw CommandAPI.fail(LanguageSupport.replacePlaceHolder("$commands.operation_failed$ $game.status.not_running$"));
        }
    }

    // original version by JorelAli, revised version by willkroboth. Optimized with IntelliJ IDEA
    // see https://github.com/JorelAli/CommandAPI/issues/275
    public static String[] suggestPlayerName(SuggestionInfo info) {
        String currentArg = info.currentArg();

        // If we end with a space, we prompt for the next player name
        if(currentArg.endsWith(" ")) {
            Set<String> players = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toSet());

            Arrays.asList(currentArg.split(" ")).forEach(players::remove);

            // 'players' now contains a set of all players that are NOT in
            // the current list that the user is typing. We want to return
            // a list of the current argument + each player that isn't
            // in the list (i.e. each player in 'players')
            return players.stream().map(player -> currentArg + player).toArray(String[]::new);
        } else {

            // Auto-complete the current player that the user is typing
            // Remove the last argument and turn it into a string as the base for suggestions
            List<String> currentArgList = new ArrayList<>(Arrays.asList(currentArg.split(" ")));
            String nameStart = currentArgList.remove(currentArgList.size() - 1);
            String suggestionBase = currentArgList.isEmpty() ? "" : String.join(" ", currentArgList) + " ";

            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.getName().startsWith(nameStart))
                    .map(player -> suggestionBase + player.getName())
                    .toArray(String[]::new);
        }
    }
}
