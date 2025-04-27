package me.dylan.tetris;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class TetrisController implements Listener {
    private final Player player;
    private final TetrisPiece piece;

    private final ItemStack leftItem;
    private final ItemStack rightItem;
    private final ItemStack downItem;
    private final ItemStack rotateItem;
    private final ItemStack hardDropItem;
    private final ItemStack holdItem;
    private final int playerNumber;

    public TetrisController(Tetris plugin, TetrisPiece piece, int playerNumber, Player player) {
        this.player = player;
        this.playerNumber = playerNumber;
        this.piece = piece;
        // 컨트롤 아이템 생성
        leftItem = createItem(Material.RED_DYE, "§c left", "Move block to left");
        rightItem = createItem(Material.ORANGE_DYE, "§6 right", "Move block to right");
        downItem = createItem(Material.YELLOW_DYE, "§e down", "Descend block");
        rotateItem = createItem(Material.LIME_DYE, "§a spin", "Rotate block");
        hardDropItem = createItem(Material.BLUE_DYE, "§9 drop", "Drop block");
        holdItem = createItem(Material.PURPLE_DYE, "§b lock", "Lock block");

        giveControlItems();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void giveControlItems() {
        Inventory inv = player.getInventory();
        inv.setItem(0, leftItem);
        inv.setItem(1, rightItem);
        inv.setItem(2, downItem);
        inv.setItem(3, rotateItem);
        inv.setItem(4, hardDropItem);
        inv.setItem(5, holdItem);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != player) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK &&
                event.getAction() != Action.LEFT_CLICK_AIR &&
                event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) return;

        if (leftItem.isSimilar(item)) {
            event.setCancelled(true);
            piece.moveLeft();
        } else if (rightItem.isSimilar(item)) {
            event.setCancelled(true);
            piece.moveRight();
        } else if (downItem.isSimilar(item)) {
            event.setCancelled(true);
            piece.moveDown();
        } else if (rotateItem.isSimilar(item)) {
            event.setCancelled(true);
            piece.rotate();
        } else if (hardDropItem.isSimilar(item)) {
            event.setCancelled(true);
            piece.hardDrop();
        } else if (holdItem.isSimilar(item)) {
            event.setCancelled(true);
            piece.holdBlock();
        }
    }

    public void removeControlItems() {
        Inventory inv = player.getInventory();
        inv.remove(leftItem);
        inv.remove(rightItem);
        inv.remove(downItem);
        inv.remove(rotateItem);
        inv.remove(hardDropItem);
        inv.remove(holdItem);
    }
}