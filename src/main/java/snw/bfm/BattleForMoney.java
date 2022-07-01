/**
 * This file is part of BattleForMoney.
 *
 * BattleForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * BattleForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BattleForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.bfm;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import snw.bfm.commands.BFMDataCommand;
import snw.bfm.commands.BFMGameCommand;
import snw.bfm.commands.BFMTeamCommand;
import snw.bfm.config.GameConfiguration;
import snw.bfm.config.Preset;
import snw.bfm.game.GameController;
import snw.bfm.game.GameProcess;
import snw.bfm.processor.EventProcessor;
import snw.bfm.tasks.Updater;
import snw.bfm.util.LanguageSupport;
import snw.bfm.util.NickSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class BattleForMoney extends JavaPlugin {
    private static BattleForMoney INSTANCE;
    private GameProcess gameProcess;
    private GameController gameController;
    private final Map<String, Double> coinEarned = new HashMap<>();
    private final Map<String, Double> killReward = new HashMap<>();

    @Override
    public void onLoad() {
        saveDefaultConfig();
        saveResource("presets.yml", false);
        saveResource("nickname.yml", false); // 2022/4/3 NickSupport!
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE = this; // 2022/1/29 把 INSTANCE 引用提前，便于 Util 操作实例。

        LanguageSupport.loadLanguage(getConfig().getString("language", "zh_CN"));

        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
        PluginManager pluginManager = Bukkit.getPluginManager();

        consoleSender.sendMessage("[BattleForMoney] " + ChatColor.GREEN + "========= Battle FOR Money =========");
        consoleSender.sendMessage("[BattleForMoney] " + ChatColor.GREEN + LanguageSupport.getTranslation("setup.author_info"));

        Logger ll = getLogger();
        ll.info(LanguageSupport.getTranslation("setup.load_data"));
        GameConfiguration.init(); // 2022/2/7 v1.1.5 GameConfiguration 不应该是需要实例化的。
        Preset.init();
        NickSupport.init(); // v1.8.0 NickSupport!
        EventProcessor.init();

        registerInternalItems();

        ll.info(LanguageSupport.getTranslation("setup.register_command"));
        // region 注册命令
        BFMTeamCommand.register();
        BFMGameCommand.register();
        BFMDataCommand.register();
        // endregion

        ll.info(LanguageSupport.getTranslation("setup.register_event_processor"));
        pluginManager.registerEvents(new EventProcessor(), this);

        getLogger().info(LanguageSupport.getTranslation("setup.complete"));

        if (getConfig().getBoolean("check_update", false)) { // 检查更新
            new Updater().start();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (getGameProcess() != null) {
            getLogger().info(LanguageSupport.getTranslation("unload.forcestop"));
            getGameProcess().stop();
        }
        Bukkit.getScheduler().cancelTasks(this); // make sure no tasks still running
    }

    public static BattleForMoney getInstance() {
        return INSTANCE;
    }

    public GameProcess getGameProcess() {
        return gameProcess;
    }

    public void setGameProcess(@Nullable GameProcess process) {
        gameProcess = process;
    }

    public GameController getGameController() {
        return gameController;
    }

    public void setGameController(@Nullable GameController gameController) {
        this.gameController = gameController;
    }

    public Map<String, Double> getRemovableCoin() {
        return coinEarned;
    }

    public void registerInternalItems() {
        // region 战斗球
        final ItemStack ball = new ItemStack(Material.SNOWBALL);
        final ItemMeta ballMeta = ball.getItemMeta();
        assert ballMeta != null;
        ballMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "战斗球");
        ball.setItemMeta(ballMeta);
        ItemRegistry.registerItem("fightball", ball);
        // endregion
    }

    public Map<String, Double> getKillReward() {
        return killReward;
    }
}
