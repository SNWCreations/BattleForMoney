package snw.bfm.tasks;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import snw.bfm.config.GameConfiguration;
import snw.bfm.game.TeamHolder;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.PlaceHolderString;
import snw.bfm.util.SendingActionBarMessage;

import java.util.Optional;

public class ExitTimer extends BaseCountDownTimer {
    private final String name;

    public ExitTimer(int secs, String name) throws IllegalArgumentException {
        super(secs);
        this.name = name;
    }

    @Override
    protected void onZero() {
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD +
                new PlaceHolderString(LanguageSupport.getTranslation("event.exit_message")).replaceArgument("playerName", name)
        );
        Optional.ofNullable(Bukkit.getPlayer(name)).ifPresent(IT -> {
            if (GameConfiguration.getEndRoomLocation() != null) {
                IT.teleport(GameConfiguration.getEndRoomLocation());
            }
        });
        TeamHolder.getInstance().addGiveUpPlayer(name);
    }

    @Override
    protected void onNewSecond() {
        ChatColor color = null;
        if (secs == 30) {
            color = ChatColor.GREEN;
        } else if (secs == 15) {
            color = ChatColor.YELLOW;
        } else if (secs <= 10) {
            color = ChatColor.DARK_RED;
        }
        if (color != null) {
            new SendingActionBarMessage(
                    new TextComponent(
                            new PlaceHolderString(ChatColor.RED +
                                    LanguageSupport.getTranslation("event.exit_warning"))
                                    .replaceArgument("playerName", name)
                                    .replaceArgument("time",
                                            color + "" + ChatColor.BOLD + secs + ChatColor.RESET + "" + ChatColor.RED)
                                    .toString())
                    , Bukkit.getOnlinePlayers()
            ).start();
        }
    }
}
