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
    private final TetrisGame game;
    private final Player player;

    // 컨트롤 아이템 정의
    private final ItemStack leftItem;      // 왼쪽 이동
    private final ItemStack rightItem;     // 오른쪽 이동
    private final ItemStack downItem;
    private final ItemStack rotateItem;    // 회전
    private final ItemStack hardDropItem;  // 하드 드롭
    private final ItemStack holdItem;      // 홀드
    private final int playerNumber;

    public TetrisController(Tetris plugin, TetrisGame game, int playerNumber, Player player) {
        this.game = game;
        this.player = player;
        this.playerNumber = playerNumber;
        // 컨트롤 아이템 생성
//        leftItem = createItem(Material.RED_DYE, "§c왼쪽 이동", "테트리스 블록을 왼쪽으로 이동합니다.");
        leftItem = createItem(Material.RED_DYE, "§c left", "테트리스 블록을 왼쪽으로 이동합니다.");
//        rightItem = createItem(Material.ORANGE_DYE, "§6오른쪽 이동", "테트리스 블록을 오른쪽으로 이동합니다.");
        rightItem = createItem(Material.ORANGE_DYE, "§6 right", "테트리스 블록을 오른쪽으로 이동합니다.");
//        downItem = createItem(Material.YELLOW_DYE, "§e블록 하강", "테트리스 블록을 밑으로 한 칸 내립니다.");
        downItem = createItem(Material.YELLOW_DYE, "§e down", "테트리스 블록을 밑으로 한 칸 내립니다.");
//        rotateItem = createItem(Material.LIME_DYE, "§a블록 회전", "테트리스 블록을 회전합니다.");
        rotateItem = createItem(Material.LIME_DYE, "§a spin", "테트리스 블록을 회전합니다.");
//        hardDropItem = createItem(Material.BLUE_DYE, "§9하드 드롭", "테트리스 블록을 즉시 떨어뜨립니다.");
        hardDropItem = createItem(Material.BLUE_DYE, "§9 drop", "테트리스 블록을 즉시 떨어뜨립니다.");
//        holdItem = createItem(Material.PURPLE_DYE, "§b홀드", "현재 블록을 홀드합니다.");
        holdItem = createItem(Material.PURPLE_DYE, "§b lock", "현재 블록을 홀드합니다.");

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

//        player.sendMessage("§a테트리스 컨트롤 아이템이 인벤토리에 추가되었습니다!");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != player) return;

        // 클릭 액션 확인 (왼쪽/오른쪽 클릭 모두 처리)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && // 공기 클릭이 아니라면
                event.getAction() != Action.RIGHT_CLICK_BLOCK && // 블록 클릭이 아니라면
                event.getAction() != Action.LEFT_CLICK_AIR &&
                event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) return;

        // 컨트롤 아이템 확인 및 액션 실행
        if (leftItem.isSimilar(item)) {
            event.setCancelled(true);
            game.moveCurrentPieceLeft(playerNumber);
        } else if (rightItem.isSimilar(item)) {
            event.setCancelled(true);
            game.moveCurrentPieceRight(playerNumber);
        } else if (downItem.isSimilar(item)) {
            event.setCancelled(true);
            game.softDropCurrentPiece();
        } else if (rotateItem.isSimilar(item)) {
            event.setCancelled(true);
            game.rotateCurrentPiece(playerNumber);
        } else if (hardDropItem.isSimilar(item)) {
            event.setCancelled(true);
            game.hardDropCurrentPiece(playerNumber);
        } else if (holdItem.isSimilar(item)) {
            event.setCancelled(true);
            game.holdCurrentPiece(playerNumber);
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