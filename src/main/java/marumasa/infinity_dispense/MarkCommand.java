package marumasa.infinity_dispense;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// 設置済みのディスペンサーに印を付けるコマンド
// /infinitydispense <mark|unmark|info> [x y z [world]]
public class MarkCommand implements TabExecutor {
    private static final List<String> SUB_COMMANDS = List.of("mark", "unmark", "info");
    // 見ているブロックを対象にするときの距離
    private static final int TARGET_RANGE = 6;

    private final Marker marker;

    public MarkCommand(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;

        final String subCommand = args[0].toLowerCase(Locale.ROOT);
        if (!SUB_COMMANDS.contains(subCommand)) return false;

        final Block block = findBlock(sender, args);
        final BlockState blockState = block == null ? null : block.getState();
        if (!marker.isDispenser(blockState)) {
            sender.sendMessage("§cディスペンサーかドロッパーを指定してください");
            return true;
        }

        switch (subCommand) {
            case "mark" -> mark(sender, blockState);
            case "unmark" -> unmark(sender, blockState);
            default -> sendInfo(sender, blockState);
        }
        return true;
    }

    private void mark(CommandSender sender, BlockState blockState) {
        // 名前が違うものは登録しても無限にならない
        if (!marker.hasInfinityName(blockState)) {
            sender.sendMessage("§c名前が config の name と同じものだけ登録できます");
            return;
        }
        marker.setMark((Container) blockState, true);
        closeForViewersWithoutPermission((Container) blockState);
        sender.sendMessage("§a無限ディスペンサーとして登録しました");
    }

    private void unmark(CommandSender sender, BlockState blockState) {
        marker.setMark((Container) blockState, false);
        sender.sendMessage("§e無限ディスペンサーの登録を解除しました");
    }

    private void sendInfo(CommandSender sender, BlockState blockState) {
        sender.sendMessage("§7名前: " + (marker.hasInfinityName(blockState) ? "§a一致" : "§c不一致")
                + " §7印: " + (marker.hasMark(blockState) ? "§aあり" : "§cなし")
                + " §7無限: " + (marker.isInfinite(blockState) ? "§aはい" : "§cいいえ"));
    }

    // 登録の前から開いていたプレイヤーが、そのまま中身を変更できないようにする
    private void closeForViewersWithoutPermission(Container container) {
        for (HumanEntity viewer : new ArrayList<>(container.getInventory().getViewers())) {
            if (!viewer.hasPermission(Marker.PERMISSION_EDIT)) viewer.closeInventory();
        }
    }

    // 座標の指定がなければ、見ているブロックを対象にする
    private Block findBlock(CommandSender sender, String[] args) {
        if (args.length == 1) return sender instanceof Player player ? player.getTargetBlockExact(TARGET_RANGE) : null;
        if (args.length != 4 && args.length != 5) return null;

        final World world = args.length == 5
                ? Bukkit.getWorld(args[4])
                : sender instanceof Entity entity ? entity.getWorld() : null;
        if (world == null) return null;

        try {
            return world.getBlockAt(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return List.of();
        final String prefix = args[0].toLowerCase(Locale.ROOT);
        return SUB_COMMANDS.stream().filter(subCommand -> subCommand.startsWith(prefix)).toList();
    }
}
