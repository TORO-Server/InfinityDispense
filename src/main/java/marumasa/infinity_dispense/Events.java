package marumasa.infinity_dispense;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Events implements Listener {
    private final minecraft mc;
    private final Marker marker;

    // 発射し直している最中のブロック (この間の発射イベントはそのまま通す)
    private final Set<Location> redispensing = new HashSet<>();
    // ブロックごとの、最後に発射し直すことにしたワールドの時刻
    private final Map<Location, Long> lastRedispense = new HashMap<>();

    public Events(minecraft minecraft, Marker marker) {
        mc = minecraft;
        this.marker = marker;
    }

    // 権限のあるプレイヤーが設置したときだけ印を付ける
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        final BlockState blockState = event.getBlockPlaced().getState();
        if (!marker.isDispenser(blockState)) return;

        final Player player = event.getPlayer();
        final boolean named = marker.hasInfinityName(blockState);
        final boolean allowed = player.hasPermission(Marker.PERMISSION_CREATE);
        if (named && !allowed) player.sendMessage("§c無限ディスペンサーを設置する権限がありません");

        // アイテムのデータから写された印は使わず、権限の確認を通ったものだけに印を付ける
        final boolean marked = named && allowed;
        if (marker.hasMark(blockState) != marked) marker.setMark((Container) blockState, marked);
    }

    // 他のプラグインが先にキャンセルしたものは処理しない
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        final Block block = event.getBlock();
        final Location location = block.getLocation();

        if (redispensing.contains(location)) return;

        // もし 無限ディスペンサーだったら
        if (marker.isInfinite(block.getState())) {
            // 同じ tick に 2 度は発射し直さない
            // (同じように発射し直すプラグインがあると、互いにキャンセルし合ってサーバーが止まるため)
            final long time = block.getWorld().getGameTime();
            final Long previous = lastRedispense.put(location, time);
            if (previous != null && previous == time) return;

            new getCancelledInventory(block).runTaskLater(mc, 0L);
            event.setCancelled(true);
        }
    }

    // 権限のないプレイヤーには無限ディスペンサーの中身を変更させない
    @EventHandler(ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        if (event.getPlayer().hasPermission(Marker.PERMISSION_EDIT)) return;

        if (marker.isInfinite(event.getInventory())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cこの無限ディスペンサーの中身を変更する権限がありません");
        }
    }

    // ホッパーなどにも無限ディスペンサーの中身を変更させない
    @EventHandler(ignoreCancelled = true)
    public void onMoveItem(InventoryMoveItemEvent event) {
        final Inventory source = event.getSource();

        if (marker.isInfinite(event.getDestination())
                // 無限ドロッパーが自分で隣の容器へ送り出すときは、通常のドロッパーと同じように動かす
                || (event.getInitiator() != source && marker.isInfinite(source))) {
            event.setCancelled(true);
        }
    }

    // キャンセル後の インベントリ 取得
    private class getCancelledInventory extends BukkitRunnable {
        private final Block block;

        public getCancelledInventory(Block block) {
            this.block = block;
        }

        @Override
        public void run() {
            // 次の tick までに壊されたり置き換えられたりしていたら何もしない
            final BlockState blockState = block.getState();
            if (!marker.isInfinite(blockState)) return;

            final Container ctr = (Container) blockState;
            final Inventory inventory = ctr.getInventory();
            final Location location = block.getLocation();

            // インベントリ内のアイテム コピー
            ItemStack[] items = inventory.getContents().clone();
            for (int i = 0; i < items.length; i++) if (items[i] != null) items[i] = items[i].clone();

            redispensing.add(location);
            try {
                // ディスペンサー or ドロッパー 起動
                if (ctr instanceof Dispenser dropper) dropper.dispense();
                else if (ctr instanceof Dropper dropper) dropper.drop();
            } finally {
                redispensing.remove(location);
                // インベントリ 復元
                inventory.setContents(items);
            }
        }
    }
}
