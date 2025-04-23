package me.dylan.tetris;//package me.dylan.tetris;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.inventory.InventoryClickEvent;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//
//public class TetrisGUI {
//    private final Tetris plugin;
//    private final Player player;
//    private final TetrisGame game;
//    private Inventory boardInventory;
//
//    // 다양한 블록 아이템들
//    private ItemStack[] blockItems = new ItemStack[8]; // 0=빈공간, 1~7=테트리스 조각
//
//    public TetrisGUI(Tetris plugin, Player player, TetrisGame game) {
//        this.plugin = plugin;
//        this.player = player;
//        this.game = game;
//
//        // 인벤토리 생성
//        this.boardInventory = Bukkit.createInventory(null, 6*9, "테트리스 게임");
//
//        // 블록 아이템 초기화
//        initBlockItems();
//
//        // 컨트롤 버튼 설정
//        setupControlButtons();
//
//        // 이벤트 리스너 등록
//        Bukkit.getPluginManager().registerEvents((Listener) new GUIListener(), plugin);
//        이벤트 리스너 등록 부분 수정
//        Bukkit.getPluginManager().registerEvents(new GUIListener(), plugin);  // 캐스팅 제거
//    }
//
//    private void initBlockItems() {
//        // 빈 공간
//        blockItems[0] = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
//        ItemMeta meta = blockItems[0].getItemMeta();
//        meta.setDisplayName("빈 블록");
//        blockItems[0].setItemMeta(meta);
//
//        // I 조각
//        blockItems[1] = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
//        meta = blockItems[1].getItemMeta();
//        meta.setDisplayName("I 블록");
//        blockItems[1].setItemMeta(meta);
//
//        // 다른 조각들도 비슷하게 초기화...
//
//        // J 조각
//        blockItems[2] = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
//        meta = blockItems[2].getItemMeta();
//        meta.setDisplayName("J 블록");
//        blockItems[2].setItemMeta(meta);
//
//        // L 블록
//        blockItems[3] = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
//        meta = blockItems[3].getItemMeta();
//        meta.setDisplayName("L 블록");
//        blockItems[3].setItemMeta(meta);
//
//        // O 조각
//        blockItems[4] = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
//        meta = blockItems[4].getItemMeta();
//        meta.setDisplayName("O 블록");
//        blockItems[4].setItemMeta(meta);
//
//        // S 블록
//        blockItems[5] = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
//        meta = blockItems[5].getItemMeta();
//        meta.setDisplayName("S 블록");
//        blockItems[5].setItemMeta(meta);
//
//        // T 블록
//        blockItems[6] = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
//        meta = blockItems[6].getItemMeta();
//        meta.setDisplayName("T 블록");
//        blockItems[6].setItemMeta(meta);
//
//        // Z 블록
//        blockItems[7] = new ItemStack(Material.RED_STAINED_GLASS_PANE);
//        meta = blockItems[7].getItemMeta();
//        meta.setDisplayName("Z 블록");
//        blockItems[7].setItemMeta(meta);
//    }
//
//    private void setupControlButtons() {
//        // 좌측
//        ItemStack leftButton = new ItemStack(Material.ARROW);
//        ItemMeta meta = leftButton.getItemMeta();
//        meta.setDisplayName("왼쪽으로 이동");
//        leftButton.setItemMeta(meta);
//        boardInventory.setItem(45, leftButton); // 슬롯 45
//
//        // 다른 컨트롤 버튼들도 비슷하게 설정...
//
//        // 하강
//        ItemStack downButton = new ItemStack(Material.ARROW);
//        meta = downButton.getItemMeta();
//        meta.setDisplayName("아래로 이동");
//        downButton.setItemMeta(meta);
//        boardInventory.setItem(46, downButton); // 슬롯 46
//
//        // 오른쪽 이동 버튼
//        ItemStack rightButton = new ItemStack(Material.ARROW);
//        meta = rightButton.getItemMeta();
//        meta.setDisplayName("오른쪽으로 이동");
//        rightButton.setItemMeta(meta);
//        boardInventory.setItem(45, rightButton);
//
//        // 왼쪽 이동 버튼
//        ItemStack leftButton = new ItemStack(Material.ARROW);
//        ItemMeta meta = leftButton.getItemMeta();
//        meta.setDisplayName("왼쪽으로 이동");
//        leftButton.setItemMeta(meta);
//        boardInventory.setItem(45, leftButton);  // 슬롯 45
//
//        // 아래로 이동 버튼 - 추가 필요
//        ItemStack downButton = new ItemStack(Material.ARROW);
//        meta = downButton.getItemMeta();
//        meta.setDisplayName("아래로 이동");
//        downButton.setItemMeta(meta);
//        boardInventory.setItem(46, downButton);  // 슬롯 46
//
//        // 오른쪽 이동 버튼
//        ItemStack rightButton = new ItemStack(Material.ARROW);
//        meta = rightButton.getItemMeta();
//        meta.setDisplayName("오른쪽으로 이동");
//        rightButton.setItemMeta(meta);
//        boardInventory.setItem(47, rightButton);  // 슬롯 47 (45에서 변경)
//
//        // 회전 버튼 - 추가 필요
//        ItemStack rotateButton = new ItemStack(Material.ENDER_PEARL);
//        meta = rotateButton.getItemMeta();
//        meta.setDisplayName("회전");
//        rotateButton.setItemMeta(meta);
//        boardInventory.setItem(48, rotateButton);  // 슬롯 48
//    }
//
//    public void updateBoard() {
//        int[][] board = game.getBoard();
//        TetrisPiece currentPiece = game.getCurrentPiece();
//
//        // 기본 보드 상태 설정
//        for (int y = 0; y < board.length; y++) {
//            for (int x = 0; x < board[0].length; x++) {
//                int slot = y * 9 + x;
//                int cell = board[y][x];
//
//                ItemStack blockItem;
//                switch (cell) {
//                    case 0: blockItem = emptyBlock; break;  // 빈 공간
//                    case 1: blockItem = iBlock; break;      // I 조각
//                    case 2: blockItem = jBlock; break;      // J 조각
//                    // 다른 조각들...
//                    default: blockItem = emptyBlock;
//                }
//                boardInventory.setItem(slot, blockItems[board[y][x]]);
//            }
//        }
//
//        // 현재 조각 표시
//        if (currentPiece != null) {
//            int[][] shape = currentPiece.getShape();
//            int pieceX = currentPiece.getX();
//            int pieceY = currentPiece.getY();
//            int color = currentPiece.getColor();
//
//            for (int y = 0; y < shape.length; y++) {
//                for (int x = 0; x < shape[0].length; x++) {
//                    if (shape[y][x] == 0) continue;
//
//                    int boardX = pieceX + x;
//                    int boardY = pieceY + y;
//
//                    if (boardY >= 0 && boardY < board.length &&
//                            boardX >= 0 && boardX < board[0].length) {
//                        int slot = boardY * 9 + boardX;
//                        boardInventory.setItem(slot, blockItems[color]);
//                    }
//                }
//            }
//        }
//
//        // 플레이어에게 인벤토리 표시
//        player.openInventory(boardInventory);
//    }
//
//    // GUI 이벤트 핸들러
//    private class GUIListener implements Listener {
//        @EventHandler
//        public void onInventoryClick(InventoryClickEvent event) {
//            if (!(event.getWhoClicked() instanceof Player)) return;
//            if (!event.getView().getTitle().equals("테트리스 게임")) return;
//
//            Player clicker = (Player) event.getWhoClicked();
//            if (!clicker.equals(player)) return;
//
//            event.setCancelled(true); // 아이템 이동 방지
//
//            int slot = event.getRawSlot();
//
//            // 클릭한 위치에 따라 게임 조작
//            if (slot == 45) {          // 왼쪽 이동
//                game.moveLeft();
//            } else if (slot == 46) {   // 아래로 이동
//                game.moveDown();
//            } else if (slot == 47) {   // 오른쪽 이동
//                game.moveRight();
//            } else if (slot == 48) {   // 회전
//                game.rotate();
//            }
//
//            // 게임판 업데이트
//            updateBoard();
//        }
//    }
//}