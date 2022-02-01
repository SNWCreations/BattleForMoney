package snw.rfm.commands.group;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import snw.rfm.RunForMoney;
import snw.rfm.group.Group;
import snw.rfm.group.GroupHolder;

public final class DeactivateGroupCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "参数不足！");
            return false;
        }

        if (RunForMoney.getInstance().getGameProcess() == null) {
            sender.sendMessage(ChatColor.RED + "操作失败。游戏未运行。");
        } else {
            if (args.length > 1) {
                for (String i : args) {
                    Group groupWillBeDeactivated = GroupHolder.getInstance().findByName(i);
                    if (groupWillBeDeactivated != null) {
                        sender.sendMessage(ChatColor.RED + "成功地启用了组 " + groupWillBeDeactivated.getName() + " 。");
                        groupWillBeDeactivated.activate();
                    } else {
                        sender.sendMessage(ChatColor.RED + "启用组 " + i + " 时失败。因为此组不存在。");
                    }
                }
            } else {
                Group group = GroupHolder.getInstance().findByName(args[0]);
                if (group == null) {
                    sender.sendMessage(ChatColor.RED + "操作失败。此组不存在。");
                } else {
                    group.activate();
                    sender.sendMessage(ChatColor.GREEN + "操作成功。");
                }
            }
        }
        GroupHolder holder = GroupHolder.getInstance();
        Group group = holder.findByName(args[0]);
        if (group == null) {
            sender.sendMessage(ChatColor.RED + "操作失败。此组不存在。");
        } else {
            group.deactivate();
            sender.sendMessage(ChatColor.GREEN + "操作成功。");
            if (args[1].equalsIgnoreCase("true")) {
                group.clear();
                holder.remove(group);
                sender.sendMessage(ChatColor.GREEN + "此组已被移除。");
            }
        }
        return true;
    }
}
