package me.dylan.tetris;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.Random;
// import org.bukkit.plugin.Plugin; // TetrisBoard에서는 직접 Plugin 객체가 필요 없을 수 있습니다.

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TetrisBoard {
    private final Player player; // 디버깅이나 특정 플레이어 관련 로직에 필요할 수 있음
    private final World world;
    private final int width;  // 보드 가로 블록 수
    private final int height; // 보드 세로 블록 수
    // private final int z; // Z 좌표가 보드마다 다를 수 있으므로 checkLines에서 관리

    // 각 보드의 기본 정보 (시작 좌표, 크기 등)를 저장하는 구조가 더 좋을 수 있습니다.
    // 예: private static final BoardInfo[] BOARD_INFOS = { ... };

    public TetrisBoard(int width, int height, Player player) {
        this.width = width;
        this.height = height;
        this.player = player;
        this.world = player.getWorld();
    }

    public void clearBoards(int x, int y, int z) {
        // 기존 로직 유지
        for (int i = 0; i < height; i++) {
            int fixedY = y + i;
            for (int j = 0; j < width; j++) {
                Block block = world.getBlockAt(x + j, fixedY, z);
                block.setType(Material.AIR);
            }
        }
    }

    public void addLockedBlock(int x, int y, int z, Material material) {
        // 기존 로직 유지
        Block block = world.getBlockAt(x, y, z);
        block.setType(material);
    }

    public boolean hasBlockAt(int x, int y, int z) {
        // 기존 로직 유지
        Block block = world.getBlockAt(x, y, z);
        // 경계 체크 추가 가능성 있음 (예: y < boardBottomY)
        return block.getType() != Material.AIR;
    }

    // 특정 라인이 꽉 찼는지 확인하는 헬퍼 메서드
    private boolean isLineFull(int y, int startX, int z) {
        for (int j = 0; j < width; j++) {
            int x = startX + j;
            if (!hasBlockAt(x, y, z)) {
                return false; // 하나라도 비어있으면 꽉 찬 것이 아님
            }
        }
        return true; // 모든 칸이 차 있음
    }

    // 특정 라인을 클리어하는 헬퍼 메서드
    private void clearLine(int y, int startX, int z) {
        for (int j = 0; j < width; j++) {
            int x = startX + j;
            Block block = world.getBlockAt(x, y, z);
            block.setType(Material.AIR);
        }
    }

    /**
     * 지정된 보드의 라인을 확인하고 클리어하며, 클리어된 라인 수를 반환합니다.
     * 클리어된 라인이 있다면 내부적으로 moveBlocksDown을 호출합니다.
     * @param startX 보드 시작 X 좌표
     * @param startY 보드 시작 Y 좌표 (가장 낮은 Y)
     * @param z 보드 Z 좌표
     * @return 클리어된 라인 수
     */
    private int checkAndClearLines(int startX, int startY, int z) {
        int clearedLinesCount = 0;
        List<Integer> clearedYCoords = new ArrayList<>();

        // 보드 바닥부터 위로 순회
        for (int y = startY; y < startY + height; y++) {
            if (isLineFull(y, startX, z)) {
                clearLine(y, startX, z);
                clearedYCoords.add(y);
                clearedLinesCount++;
            }
        }

        // 클리어된 라인이 있다면 블록들을 아래로 이동
        if (clearedLinesCount > 0) {
            moveBlocksDown(startX, z, clearedYCoords, startY);
        }

        return clearedLinesCount;
    }

    /**
     * 클리어된 라인 위쪽의 블록들을 아래로 이동시킵니다.
     * @param startX 보드 시작 X 좌표
     * @param z 보드 Z 좌표
     * @param clearedYCoords 클리어된 라인들의 Y좌표 리스트 (정렬되지 않아도 됨)
     * @param startY 보드 시작 Y 좌표 (가장 낮은 Y)
     */
    private void moveBlocksDown(int startX, int z, List<Integer> clearedYCoords, int startY) {
        if (clearedYCoords.isEmpty()) {
            return;
        }

        Collections.sort(clearedYCoords); // Y좌표 오름차순 정렬

        int lowestClearedY = clearedYCoords.get(0);
        int destinationY = lowestClearedY; // 복사될 위치
        int sourceY = destinationY + 1;    // 복사할 원본 위치

        while (sourceY < startY + height) {
            // sourceY가 클리어된 라인 중 하나면 건너<0xEB><0x9A><0x8D>기
            if (clearedYCoords.contains(sourceY)) {
                sourceY++;
                continue;
            }

            // sourceY 라인을 destinationY 라인으로 복사
            boolean rowNotEmpty = false;
            for (int xOffset = 0; xOffset < width; xOffset++) {
                int currentX = startX + xOffset;
                Block sourceBlock = world.getBlockAt(currentX, sourceY, z);
                Material sourceType = sourceBlock.getType();

                if (sourceType != Material.AIR) {
                    rowNotEmpty = true;
                    Block destinationBlock = world.getBlockAt(currentX, destinationY, z);
                    destinationBlock.setType(sourceType);
                    sourceBlock.setType(Material.AIR); // 원본 위치 클리어
                } else {
                    // 만약 source 블록이 AIR이면 destination도 AIR로 만들어야 함 (이미 클리어된 경우 제외)
                    Block destinationBlock = world.getBlockAt(currentX, destinationY, z);
                    if (destinationBlock.getType() != Material.AIR) {
                        destinationBlock.setType(Material.AIR);
                    }
                }
            }

            // 만약 sourceY 라인 전체가 비어 있었다면, 그 위로는 더 이상 블록이 없을 가능성이 높으므로 종료 (최적화)
            // 하지만 안전하게 계속 진행해도 문제는 없음
            // if (!rowNotEmpty) {
            //     break;
            // }

            destinationY++;
            sourceY++;
        }
    }


    /**
     * 지정된 보드 번호의 라인을 확인하고 클리어된 라인 수를 반환합니다.
     * @param boardNumber 보드 번호 (1부터 시작한다고 가정)
     * @return 클리어된 라인 수
     */
    public int checkLines(int boardNumber) {
        // 각 보드별 좌표 및 크기 정보 필요
        // 예시 좌표 사용
        int x = 0, y = 0, z = 0;
        // int currentHeight = this.height; // 보드별 높이가 다를 경우 필요

        switch (boardNumber) {
            case 1: // Player 1
                x = -16; y = -52; z = -13;
                break;
            case 2: // Player 2
                x = 31; y = -52; z = -13;
                break;
            case 3: // Player 3
                x = 78; y = -52; z = -13;
                break;
            case 4: // Player 4
                x = 8; y = 13; z = -17;
                // currentHeight = 다른 값; // 예시
                break;
            case 5: // Player 5
                // player.sendMessage("\ncheckLines case 5"); // 디버깅 메시지 제거 권장
                x = 54; y = 13; z = -17;
                break;
            default:
                // player.sendMessage("Invalid board number: " + boardNumber); // 오류 처리
                return 0; // 잘못된 보드 번호 처리
        }

        // 분리된 로직 호출
        return checkAndClearLines(x, y, z);
    }

    /**
     * 보드의 블록들을 지정된 라인 수만큼 위로 이동시킵니다.
     * @param linesToShift 위로 이동시킬 라인 수
     * @param startX 보드 시작 X 좌표
     * @param startY 보드 시작 Y 좌표 (가장 낮은 Y)
     * @param z 보드 Z 좌표
     */
    private void shiftBlocksUp(int linesToShift, int startX, int startY, int z) {
        if (linesToShift <= 0) {
            return;
        }

        // 위에서부터 아래로 순회하며 블록 복사 및 원본 삭제
        for (int y = startY + height - 1; y >= startY; y--) {
            int destinationY = y + linesToShift;

            for (int x = startX; x < startX + width; x++) {
                Block sourceBlock = world.getBlockAt(x, y, z);
                Material sourceType = sourceBlock.getType();

                // 대상 위치가 보드 상단 경계를 벗어나면 복사하지 않음
                if (destinationY < startY + height) {
                    Block destinationBlock = world.getBlockAt(x, destinationY, z);
                    destinationBlock.setType(sourceType);
                }
                sourceBlock.setType(Material.AIR); // 원본 위치 클리어
            }
        }
    }

    /**
     * 지정된 Y 좌표에 구멍이 하나 있는 쓰레기 줄 한 줄을 생성합니다.
     * @param y 쓰레기 줄을 생성할 Y 좌표
     * @param startX 보드 시작 X 좌표
     * @param z 보드 Z 좌표
     */
    private void addSingleGarbageLine(int y, int startX, int z) {
        Random random = new Random();
        int holeX = startX + random.nextInt(width); // 0부터 width-1 사이의 구멍 위치

        for (int x = startX; x < startX + width; x++) {
            Block block = world.getBlockAt(x, y, z);
            if (x == holeX) {
                block.setType(Material.AIR); // 구멍 생성
            } else {
                block.setType(Material.GRAY_WOOL); // 쓰레기 블록 채우기
            }
        }
    }

    /**
     * 보드 하단에 여러 줄의 쓰레기 줄을 추가합니다. 기존 블록은 위로 밀립니다.
     * @param linesToAdd 추가할 쓰레기 줄의 수
     * @param startX 보드 시작 X 좌표
     * @param startY 보드 시작 Y 좌표 (가장 낮은 Y)
     * @param z 보드 Z 좌표
     */
    public void addGarbageLines(int linesToAdd, int startX, int startY, int z) {
        if (linesToAdd <= 0) {
            return;
        }

        // 1. 기존 블록들을 위로 이동시켜 공간 확보
        shiftBlocksUp(linesToAdd, startX, startY, z);

        // 2. 보드 하단에 쓰레기 줄 추가
        for (int i = 0; i < linesToAdd; i++) {
            int targetY = startY + i; // 가장 아래부터 채움
            // 대상 Y좌표가 보드 높이를 벗어나지 않는지 확인 (필요시)
            if (targetY < startY + height) {
                addSingleGarbageLine(targetY, startX, z);
            } else {
                // 보드가 꽉 찼을 경우 처리 (예: 게임 오버 로직)
                // 여기서는 더 이상 추가하지 않음
                break;
            }
        }
    }
}