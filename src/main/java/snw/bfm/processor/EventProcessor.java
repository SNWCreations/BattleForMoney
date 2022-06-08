/*
 * This file is part of BattleForMoney.
 *
 * BattleForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * BattleForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BattleForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm.processor;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import snw.bfm.BattleForMoney;
import snw.bfm.ItemRegistry;
import snw.bfm.config.GameConfiguration;
import snw.bfm.config.Preset;
import snw.bfm.game.GameController;
import snw.bfm.game.GameProcess;
import snw.bfm.game.TeamHolder;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.NickSupport;
import snw.bfm.util.PlaceHolderString;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static snw.bfm.util.Util.removeAllPotionEffect;

public final class EventProcessor implements Listener {
    private static TextComponent mcbbsHomeText;
    private static TextComponent bilibiliHomeText;

    public static void init() {
        // 2022/2/19 增加亿点有关我的内容
        bilibiliHomeText = new TextComponent(LanguageSupport.getTranslation("event.join.bilibili"));
        bilibiliHomeText.setUnderlined(true);
        bilibiliHomeText.setColor(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
        bilibiliHomeText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://space.bilibili.com/57486712"));
        bilibiliHomeText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LanguageSupport.getTranslation("event.join.bilibili_hover"))));

        mcbbsHomeText = new TextComponent(LanguageSupport.getTranslation("event.join.mcbbs"));
        mcbbsHomeText.setUnderlined(true);
        mcbbsHomeText.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        mcbbsHomeText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.mcbbs.net/home.php?mod=space&uid=2190885"));
        mcbbsHomeText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LanguageSupport.getTranslation("event.join.mcbbs_hover"))));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BattleForMoney rfm = BattleForMoney.getInstance();
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + LanguageSupport.getTranslation("event.join.welcome"));
        player.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("event.join.plugin_info") + rfm.getDescription().getVersion());
        player.sendMessage(ChatColor.GOLD + LanguageSupport.getTranslation("event.join.author"));
        player.spigot().sendMessage(ChatMessageType.CHAT, bilibiliHomeText);
        player.spigot().sendMessage(ChatMessageType.CHAT, mcbbsHomeText);
        player.sendMessage("");

        GameProcess process = rfm.getGameProcess();
        if (process != null) { // 如果游戏正在进行
            if (TeamHolder.getInstance().isNotInGame(player)) { // 如果既不是逃走队员也不是猎人
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC +
                        new PlaceHolderString(LanguageSupport.getTranslation("event.join.new_player_ingame"))
                                .replaceArgument("status",
                                (rfm.getGameController().isPaused() ?
                                        LanguageSupport.getTranslation("game.status.already_paused")
                                        : LanguageSupport.getTranslation("game.status.already_running")
                                ))
                );
                player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
                player.setGameMode(GameMode.SPECTATOR);
                removeAllPotionEffect(player);
            }
        } else {
            // region 预设部分
            if (Preset.isPresetNinja(player)) {
                player.performCommand("bfmteam join player");
            } else if (Preset.isPresetPlayer(player)) { // 2022/2/6 避免喜欢恶作剧的用代码玩这个插件。。我真是操碎了心啊。。
                player.performCommand("bfmteam join ninja");
                player.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("event.join_preset_as_runner"));
            }
            // endregion
            player.setGameMode(GameMode.ADVENTURE);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1, 0); // 感觉没什么用
        }
    }

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent event) {
        pauseIfNoPlayerFound();
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        pauseIfNoPlayerFound();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (BattleForMoney.getInstance().getGameProcess() == null) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            // region 2022/2/10 改用最稳定的方法
            boolean remove = false;
            for (Function<Player, Boolean> iep : ItemRegistry.getProcessorByItem(item)) {
                try { // 2022/3/12 保证所有此接口的实现都能被正常调用
                    if (iep.apply(player) // 调用方法实现
                            && !remove) {
                        remove = true;
                    }
                } catch (Exception e) { // 2022/3/29 一个程序不应该尝试 catch 一个 Error ，所以从 Throwable 改为 Exception
                    BattleForMoney.getInstance().getLogger().warning("An function of an item generated an exception.");
                    e.printStackTrace();
                }
            }
            if (remove) {
                item.setAmount(item.getAmount() - 1);
            }
            // endregion
        }

    }

    @EventHandler
    public void onSnowballDamage(EntityDamageByEntityEvent event) {
        if (BattleForMoney.getInstance().getGameProcess() == null) {
            return;
        }

        if (!(event.getEntity() instanceof final Player out) ||
                !(event.getDamager() instanceof final Snowball snowball) ||
                !(snowball.getShooter() instanceof final Player damager) || // if the shooter is not a player?
                !TeamHolder.getInstance().isNotInGame(out) ||
                !TeamHolder.getInstance().isNotInGame(damager)
        ) return;
        event.setDamage(0);

        final Location loc = out.getLocation();
        if (GameConfiguration.getEndRoomLocation() != null) {
            out.teleport(GameConfiguration.getEndRoomLocation());
        }

        TeamHolder.getInstance().removePlayer(out);
        for (ItemStack itemStack : out.getInventory().getContents()) {
            if (itemStack == null) continue;
            Objects.requireNonNull(loc.getWorld()).dropItem(loc, itemStack); // it is impossible to fail...
        }
        out.getInventory().clear();

        Bukkit.broadcastMessage(ChatColor.RED +
                new PlaceHolderString(LanguageSupport.getTranslation("event.player_out"))
                        .replaceArgument("playerName", NickSupport.getNickName(out.getName()))
                        .replaceArgument("damager", NickSupport.getNickName(damager.getName()))
                        .toString()
        );

        damager.setCooldown(Material.SNOWBALL, BattleForMoney.getInstance().getConfig().getInt("fightball_cooldown", 5) * 20);

        // kill reward
        Map<String, Double> killReward = BattleForMoney.getInstance().getKillReward();
        killReward.put(damager.getName(), killReward.get(damager.getName()) + GameConfiguration.getKillReward());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (BattleForMoney.getInstance().getGameProcess() == null) {
            return;
        }
        event.setDeathMessage(null);

        TeamHolder.getInstance().removePlayer(event.getEntity());
        event.getEntity().spigot().respawn();
        if (GameConfiguration.getEndRoomLocation() != null) {
            event.getEntity().teleport(GameConfiguration.getEndRoomLocation());
        }
        Bukkit.broadcastMessage(ChatColor.RED +
                new PlaceHolderString(LanguageSupport.getTranslation("event.player_out"))
                        .replaceArgument("playerName", NickSupport.getNickName(event.getEntity().getName()))
                        .replaceArgument("damager", "UNKNOWN")
                        .toString()
        );
    }

    @EventHandler
    public void onUnusedDamageHappen(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            boolean cancel = switch (event.getCause()) {
                case FALL, FIRE, LAVA, DROWNING -> true;
                default -> event.isCancelled(); // if some plugins cancel this event for some reason?
            };
            event.setCancelled(cancel);
        }
    }

    private void pauseIfNoPlayerFound() {
        GameController gameController = BattleForMoney.getInstance().getGameController();

        if (!(gameController == null) &&
                !gameController.isPaused() &&
                TeamHolder.getInstance().getPlayers().stream().noneMatch(IT -> Bukkit.getPlayer(IT) != null)) {
            gameController.pause();
        }
    }
}
