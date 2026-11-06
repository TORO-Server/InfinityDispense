package marumasa.infinity_dispense;

import org.bukkit.Nameable;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

// 無限ディスペンサーの判定と、ブロックに付ける印 (PDC) の管理
public class Marker {
    // 無限ディスペンサーを設置、登録できる権限
    public static final String PERMISSION_CREATE = "infinitydispense.create";
    // 無限ディスペンサーの中身を変更できる権限
    public static final String PERMISSION_EDIT = "infinitydispense.edit";

    private final NamespacedKey key;
    private final String name;

    public Marker(minecraft plugin, Config config) {
        key = new NamespacedKey(plugin, "infinity_dispenser");
        name = config.name;
    }

    // ディスペンサー or ドロッパー か
    public boolean isDispenser(BlockState state) {
        return state instanceof Dispenser || state instanceof Dropper;
    }

    // カスタム名前が config の name と同じか
    public boolean hasInfinityName(BlockState state) {
        return name != null && state instanceof Nameable nameable && name.equals(nameable.getCustomName());
    }

    // 印が付いているか
    public boolean hasMark(BlockState state) {
        return state instanceof Container container
                && container.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    // 印を付ける or 外す
    public void setMark(Container container, boolean marked) {
        final PersistentDataContainer data = container.getPersistentDataContainer();
        if (marked) data.set(key, PersistentDataType.BYTE, (byte) 1);
        else data.remove(key);
        container.update();
    }

    // 名前と印がそろっているものだけを無限ディスペンサーとして扱う
    public boolean isInfinite(BlockState state) {
        return isDispenser(state) && hasInfinityName(state) && hasMark(state);
    }

    // インベントリが無限ディスペンサーのものか
    public boolean isInfinite(Inventory inventory) {
        // ディスペンサーとドロッパー以外はブロックの状態を取得しない (ホッパーの処理を重くしないため)
        final InventoryType type = inventory.getType();
        if (type != InventoryType.DISPENSER && type != InventoryType.DROPPER) return false;
        return inventory.getHolder() instanceof BlockState state && isInfinite(state);
    }
}
