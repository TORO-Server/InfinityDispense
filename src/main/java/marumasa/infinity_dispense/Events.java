package marumasa.infinity_dispense;

import org.bukkit.Nameable;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Events implements Listener {
    private final minecraft mc;
    private final Config cfg;

    // イベントをキャンセルした後のインベントリのデータ
    private static final Map<BlockState, ItemStack[]> CancelledItemStacks = new HashMap<>();

    public Events(minecraft minecraft, Config config) {
        mc = minecraft;
        cfg = config;
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent event) {

        // ブロック状態 取得
        final BlockState blockState = event.getBlock().getState();

        if (CancelledItemStacks.containsKey(blockState)) {
            //インベントリリセット処理 実行

            final Container container = (Container) blockState;

            // インベントリ リセット
            // CancelledItemStacks から インベントリのアイテム情報 削除
            container.getInventory().setContents(CancelledItemStacks.remove(container));
            return;
        }

        // もし カスタム名前 設定できる ブロックだったら
        if (blockState instanceof Nameable nameable) {

            // カスタム名前 取得
            final String name = nameable.getCustomName();

            // もし名前が config の name と同じだったら
            if (Objects.equals(name, cfg.name)) {
                new getCancelledInventory((Container) blockState).runTaskLater(mc, 0L);
                event.setCancelled(true);
            }
        }
    }

    // キャンセル後の インベントリ 取得
    private static class getCancelledInventory extends BukkitRunnable {
        private final Container ctr;

        public getCancelledInventory(Container container) {
            ctr = container;
        }

        @Override
        public void run() {
            // インベントリ内のアイテム コピー
            ItemStack[] items = ctr.getInventory().getContents().clone();
            for (int i = 0; i < items.length; i++) if (items[i] != null) items[i] = items[i].clone();

            // コピーしたデータ 登録
            CancelledItemStacks.put(ctr, items);

            // ディスペンサー or ドロッパー 起動
            if (ctr instanceof Dispenser dropper) dropper.dispense();
            else if (ctr instanceof Dropper dropper) dropper.drop();
        }
    }

}
