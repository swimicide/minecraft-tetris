package me.dylan.tetris;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class TetrisBoard {
    private final Player player;
    private final World world;
    private final int width;
    private final int height;
    private final int z = -13; // z축 좌표를 클래스 변수로 설정
//    private int floor = 0;
//    public boolean finish = false;
    public boolean finish1 = false;
    public boolean finish2 = false;
    public boolean finish3 = false;
    public boolean finish4 = false;
    public boolean finish5 = false;
    private int floor1 = 0;
    private int floor2 = 0;
    private int floor3 = 0;
    private int floor4 = 0;
    private int floor5 = 0;

    public TetrisBoard(int width, int height, Player player) {
//        width, height는 각 row, column의 줄 길이, 즉 블록의 개수를 나타냄.
//        좌표를 저장하는 변수가 따로 필요.
        this.width = width;
        this.height = height;
        this.player = player;
//        this.plugin = plugin;
        this.world = player.getWorld();
    }

    public void clearBoards(int x, int y, int z) {
        for (int i = 0; i < height; i++) {
            int fixedY = y + i;
            for (int j = 0; j < width; j++) {
                Block block = world.getBlockAt(x + j, fixedY, z);
                block.setType(Material.AIR);
            }
        }
    }

//    더 이상 움직일 수 없는 블록의 정보 저장
    public void addLockedBlock(int x, int y, int z, Material material) {
        // 실제 월드에 블록 설정
        Block block = world.getBlockAt(x, y, z);
        block.setType(material);
    }

//    특정 Y 높이의 라인이 완성되었는지 확인, 블록으로 한 줄이 완성되었는지 체크
    //    특정 위치에 블록이 존재하는지 확인
    public boolean hasBlockAt(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        return block.getType() != Material.AIR;
    }

    public void refreshLine(int start) {
//        for (int i = 0; i < height; i++) {
//            int y = -52 + i;
//            boolean match = true;
//
//            for (int j = 0; j < width; j++) {
//                int x = start + j;
//                Block block = world.getBlockAt(x, y, -13);
//                if (block.getType() == Material.AIR) {
//                    if (finish) {
//                        moveBlocksDown(y, floor);
//                        finish = false;
//                        return;
//                    }
//                    match = false;
//                    break;
//                }
//            }
//            if (match) {
//                for (int j = 0; j < width; j++) {
//                    int x = start + j;
//                    Block block = world.getBlockAt(x, y, -13);
//                    block.setType(Material.AIR);
//                }
//
//                if (y < floor) {
//                    floor = y;
//                }
//                finish = true;
//            }
//        }
    }

    public int checkLines(int boardNumber) {
        int x = 0;
        int clearedLines = 0;
        switch(boardNumber) {
            case 1:
//                x = -16;
                for (int i = 0; i < height; i++) {
                    int y = -52 + i;
                    boolean match = true;

                    for (int j = 0; j < width; j++) {
                        Block block = world.getBlockAt(-16, y, -13);
                        if (block.getType() == Material.AIR) {
                            if (finish1) {
                                clearedLines = moveBlocksDown(y, floor1, -16);
                                finish1 = false;
                                return clearedLines;
                            }
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        for (int j = 0; j < width; j++) {
//                            int x = start + j;
                            Block block = world.getBlockAt(-16 + j, y, -13);
                            block.setType(Material.AIR);
                        }

                        if (y < floor1) {
                            floor1 = y;
                        }
                        finish1 = true;
                    }
                }
                break;

            case 2:
//                x = 31;
                for (int i = 0; i < height; i++) {
                    int y = -52 + i;
                    boolean match = true;

                    for (int j = 0; j < width; j++) {
                        Block block = world.getBlockAt(31, y, -13);
                        if (block.getType() == Material.AIR) {
                            if (finish2) {
                                clearedLines = moveBlocksDown(y, floor1, 31);
                                finish2 = false;
                                return clearedLines;
                            }
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        for (int j = 0; j < width; j++) {
//                            int x = start + j;
                            Block block = world.getBlockAt(31 + j, y, -13);
                            block.setType(Material.AIR);
                        }

                        if (y < floor2) {
                            floor2 = y;
                        }
                        finish2 = true;
                    }
                }
                break;

            case 3:
//                x = 78;
                for (int i = 0; i < height; i++) {
                    int y = -52 + i;
                    boolean match = true;

                    for (int j = 0; j < width; j++) {
                        Block block = world.getBlockAt(78, y, -13);
                        if (block.getType() == Material.AIR) {
                            if (finish3) {
                                clearedLines = moveBlocksDown(y, floor3, 78);
                                finish3 = false;
                                return clearedLines;
                            }
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        for (int j = 0; j < width; j++) {
//                            int x = start + j;
                            Block block = world.getBlockAt(78 + j, y, -13);
                            block.setType(Material.AIR);
                        }

                        if (y < floor3) {
                            floor3 = y;
                        }
                        finish3 = true;
                    }
                }
                break;

            case 4:
//                x = 8;
                for (int i = 0; i < height; i++) {
                    int y = -52 + i;
                    boolean match = true;

                    for (int j = 0; j < width; j++) {
                        Block block = world.getBlockAt(8, y, -17);
                        if (block.getType() == Material.AIR) {
                            if (finish4) {
                                clearedLines = moveBlocksDown(y, floor4, 8);
                                finish4 = false;
                                return clearedLines;
                            }
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        for (int j = 0; j < width; j++) {
//                            int x = start + j;
                            Block block = world.getBlockAt(8 + j, y, -17);
                            block.setType(Material.AIR);
                        }

                        if (y < floor4) {
                            floor4 = y;
                        }
                        finish4 = true;
                    }
                }
                break;

            case 5:
//                x = 54;
                for (int i = 0; i < height; i++) {
                    int y = -52 + i;
                    boolean match = true;

                    for (int j = 0; j < width; j++) {
                        Block block = world.getBlockAt(54, y, -17);
                        if (block.getType() == Material.AIR) {
                            if (finish5) {
                                clearedLines = moveBlocksDown(y, floor5, 54);
                                finish5 = false;
                                return clearedLines;
                            }
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        for (int j = 0; j < width; j++) {
//                            int x = start + j;
                            Block block = world.getBlockAt(54 + j, y, -17);
                            block.setType(Material.AIR);
                        }

                        if (y < floor5) {
                            floor5 = y;
                        }
                        finish5 = true;
                    }
                }
                break;
        }
        return 0;
    }

    private int moveBlocksDown(int blockStartY, int clearedY, int startX) {
        for (int y = blockStartY; y < -6; y++) {
            boolean match = true;
            for (int x = startX; x < startX + width; x++) {
                Block block = world.getBlockAt(x, y, -13);
                Block newBlock = world.getBlockAt(x, clearedY, -13);
                if (block.getType() != Material.AIR) {
                    match = false;
                    newBlock.setType(block.getType());
                    block.setType(Material.AIR);
                }
            }
            if (match) {
                break;
            }
            clearedY += 1;
        }
        return clearedY;
    }
}

