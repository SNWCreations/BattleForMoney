/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.processor;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import snw.rfm.ItemRegistry;
import snw.rfm.RunForMoney;
import snw.rfm.api.GameController;
import snw.rfm.api.ItemEventListener;
import snw.rfm.api.events.HunterCatchPlayerEvent;
import snw.rfm.config.GameConfiguration;
import snw.rfm.config.Preset;
import snw.rfm.game.GameProcess;
import snw.rfm.game.TeamHolder;
import snw.rfm.group.Group;
import snw.rfm.util.LanguageSupport;
import snw.rfm.util.NickSupport;
import snw.rfm.util.PlaceHolderString;

import java.util.Objects;
import java.util.Optional;

import static snw.rfm.Util.removeAllPotionEffect;

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
        RunForMoney rfm = RunForMoney.getInstance();
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + LanguageSupport.getTranslation("event.join.welcome"));
        player.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("event.join.plugin_info") + rfm.getDescription().getVersion());
        player.sendMessage(ChatColor.GOLD + LanguageSupport.getTranslation("event.join.author"));
        player.spigot().sendMessage(ChatMessageType.CHAT, bilibiliHomeText);
        player.spigot().sendMessage(ChatMessageType.CHAT, mcbbsHomeText);
        player.sendMessage("");

        GameProcess process = rfm.getGameProcess();
        if (process != null) { // 如果游戏正在进行
            TeamHolder holder = TeamHolder.getInstance();
            if (holder.isNotInGame(player)) { // 如果既不是逃走队员也不是猎人
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
                removeAllPotionEffect(player); // 2022/2/6 移除药水效果，但是改进了; 2022/2/20 改用 Util 类内置方法。
            }
        } else {
            // region 预设部分
            if (Preset.isPresetHunter(player)) {
                player.performCommand("hunter");
                player.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("event.join.preset_as_hunter"));
                Group playerWillBeJoined = Preset.getPlayerNotJoinedGroup(player);
                if (playerWillBeJoined != null) {
                    playerWillBeJoined.add(player.getName());
                    player.sendMessage(ChatColor.GREEN +
                            new PlaceHolderString(LanguageSupport.getTranslation("event.join.preset_join_group"))
                                    .replaceArgument("groupName", playerWillBeJoined.getName())
                                    .toString()
                    );
                }
            } else if (Preset.isPresetRunner(player)) { // 2022/2/6 避免喜欢恶作剧的用代码玩这个插件。。我真是操碎了心啊。。
                player.performCommand("runner");
                player.sendMessage(ChatColor.GREEN + LanguageSupport.getTranslation("event.join_preset_as_runner"));
            }
            // endregion
            player.setGameMode(GameMode.ADVENTURE);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1, 0); // 感觉没什么用
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            GameProcess process = RunForMoney.getInstance().getGameProcess();
            TeamHolder holder = TeamHolder.getInstance();

            Entity entity = event.getEntity();
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

            // 检查
            if (process == null || !(entity instanceof Player) || !(damager instanceof Player)) {
                return;
            }

            Player player = (Player) entity;
            Player hunter = (Player) damager;
            event.setDamage(0);
            if (holder.isRunner(player) && holder.isHunterEnabled(hunter) && (process.getHunterNoMoveTime() <= 0)) {

                int player_remaining = holder.getRunners().size() - 1;
                HunterCatchPlayerEvent catchPlayerEvent = new HunterCatchPlayerEvent(player, hunter, player_remaining);
                Bukkit.getPluginManager().callEvent(catchPlayerEvent);
                if (catchPlayerEvent.isCancelled()) { // 2022/3/1 修复未对 HunterCatchPlayerEvent#isCancelled 方法的返回值做出处理的错误
                    return;
                }

                holder.addOutPlayer(player);
                player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());

                RunForMoney.getInstance().getCoinEarned().put(player.getName(), catchPlayerEvent.getCoinEarned(true)); // 2022/3/13 省的我再算一遍了 hhhhc

                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD +
                        new PlaceHolderString(LanguageSupport.getTranslation("event.catch_message")).replaceArgument("playerName", NickSupport.getNickName(player.getName()))
                );
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD +
                        new PlaceHolderString(LanguageSupport.getTranslation("game.player_remaining")).replaceArgument("remaining", player_remaining)
                );

                Bukkit.getScheduler().runTaskLater(RunForMoney.getInstance(),
                        () -> Optional.ofNullable(GameConfiguration.getEndRoomLocation()).ifPresent(player::teleport), 1L);

                process.checkStop();
            }
        } else {
            if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);
            }
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
        if (RunForMoney.getInstance().getGameProcess() == null) {
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
            for (ItemEventListener iep : ItemRegistry.getProcessorByItem(item)) {
                try { // 2022/3/12 保证所有此接口的实现都能被正常调用
                    if (iep.onPlayerUseRequiredItem(player) // 调用方法实现
                            && !remove) {
                        remove = true;
                    }
                } catch (Exception e) { // 2022/3/29 一个程序不应该尝试 catch 一个 Error ，所以从 Throwable 改为 Exception
                    RunForMoney.getInstance().getLogger().warning("An ItemEventListener generated an exception.");
                    e.printStackTrace();
                }
            }
            if (remove) {
                item.setAmount(item.getAmount() - 1);
            }
            // endregion
        }

    }


    private void pauseIfNoPlayerFound() {
        GameController gameController = RunForMoney.getInstance().getGameController();
        TeamHolder holder = TeamHolder.getInstance();

        if (!(gameController == null) && !gameController.isPaused() && (holder.isNoRunnerFound() || holder.isNoHunterFound())) {
            gameController.pause();
        }
    }
}
