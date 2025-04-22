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
    private int floor = 0;
    public boolean finish = false;

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
        for (int i = 0; i < height; i++) {
            int y = -52 + i;
            boolean match = true;

            for (int j = 0; j < width; j++) {
                int x = start + j;
                Block block = world.getBlockAt(x, y, -13);
                if (block.getType() == Material.AIR) {
                    if (finish) {
                        moveBlocksDown(y, floor);
                        return;
                    }
                    match = false;
                    break;
                }
            }
            if (match) {
                for (int j = 0; j < width; j++) {
                    int x = start + j;
                    Block block = world.getBlockAt(x, y, -13);
                    block.setType(Material.AIR);
                }

                if (y < floor) {
                    floor = y;
                }
                finish = true;
            }
        }
    }

    public void checkLines(int boardNumber) {
        int x = 0;
        switch(boardNumber) {
            case 1 :
                x = -16;
                refreshLine(x);
                break;

            case 2 :
                x = 31;
                refreshLine(x);
                break;

            case 3 :
                x = 78;
                refreshLine(x);
                break;

            case 4 :
                x = 8;
                refreshLine(x);
                break;

            case 5 :
                x = 54;
                refreshLine(x);
                break;
        }
    }

    private void moveBlocksDown(int blockStartY, int clearedY) {
        for (int y = blockStartY; y < -6; y++) {
            boolean match = true;
            for (int x = -16; x < -16 + width; x++) {
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
    }
}

