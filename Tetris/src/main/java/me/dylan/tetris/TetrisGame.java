package me.dylan.tetris;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

public class TetrisGame implements Listener {

    private static final Logger log = LoggerFactory.getLogger(TetrisGame.class);
    private TetrisController controller;
    private final Tetris plugin;

    private TetrisBoard board;
    private int score;
    private int level;
    private int linesCleared;
    public boolean isGameRunning;

    public BukkitTask gameTask1;
    public BukkitTask gameTask2;
    public BukkitTask gameTask3;
    public BukkitTask gameTask4;
    public BukkitTask gameTask5;
    public TetrisPiece piece1;
    public TetrisPiece piece2;
    public TetrisPiece piece3;
    public TetrisPiece piece4;
    public TetrisPiece piece5;
    public int[] upcomingPiece1;
    public int[] upcomingPiece2;
    public int[] upcomingPiece3;
    public int[] upcomingPiece4;
    public int[] upcomingPiece5;
    public int currentPiece1;
    public int currentPiece2;
    public int currentPiece3;
    public int currentPiece4;
    public int currentPiece5;
    public boolean firstRun1 = true;
    public boolean firstRun2 = true;
    public boolean firstRun3 = true;
    public boolean firstRun4 = true;
    public boolean firstRun5 = true;
    public int cube1 = -1; // 홀드된 블록의 타입을 저장 (-1은 홀드된 블록 없음)
    public int cube2 = -1; // 홀드된 블록의 타입을 저장 (-1은 홀드된 블록 없음)
    public int cube3 = -1; // 홀드된 블록의 타입을 저장 (-1은 홀드된 블록 없음)
    public int cube4 = -1; // 홀드된 블록의 타입을 저장 (-1은 홀드된 블록 없음)
    public int cube5 = -1; // 홀드된 블록의 타입을 저장 (-1은 홀드된 블록 없음)


    // 홀드 관련 변수 단순화
    public List<Player> onlineList;
    public List<Player> registList;
    public int[] scoreList;
    public int[] levelList;
    public Player standardPlayer; // 실험용
    long tickDelay = Math.max(2, 20 - (level * 2));
    public static final int[][] boardClearCoord = {
            {-16, -52, -13},
            {31, -52, -13},
            {78, -52, -13},
            {8, 13, -17},
            {54, 13, -17}
    };

    public TetrisGame(Tetris plugin, List<Player> onlinePlayerList, List<Player> activePlayerList, List<Player> registeredPlayerList) {
        int height = 47;
        int width = 28;
        this.plugin = plugin;
        this.onlineList = onlinePlayerList;
        this.registList = registeredPlayerList;
        this.standardPlayer = onlinePlayerList.get(0);
        this.board = new TetrisBoard(width, height, Objects.requireNonNull(standardPlayer));
        this.upcomingPiece1 = new int[5];
        this.upcomingPiece2 = new int[5];
        this.upcomingPiece3 = new int[5];
        this.upcomingPiece4 = new int[5];
        this.upcomingPiece5 = new int[5];

//        보드 초기화
        for (int i = 0; i < registeredPlayerList.size(); i++) {
            Player p = registeredPlayerList.get(i);
            if (p != null) {
                int x = boardClearCoord[i][0];
                int y = boardClearCoord[i][1];
                int z = boardClearCoord[i][2];
                board.clearBoards(x, y, z);
                controller = new TetrisController(this.plugin, this, i, this.registList.get(i));
            }
        }
        startGame();
    }

    public void startGame() {
        // 게임 초기화
        int idx = 0;
        int registeredLength = registList.size();
        scoreList = new int[registeredLength];
        levelList = new int[registeredLength];
        for (int i = 0; i < registeredLength; i++) {
            if (registList.get(i) != null) {
                scoreList[idx] = 0;
                levelList[idx] = 1;
                idx += 1;
            }
        }

        isGameRunning = true;

        // 이벤트 리스너 등록
        Bukkit.getPluginManager().registerEvents(this, plugin);
//        준비시간
        Bukkit.getScheduler().runTaskLater(plugin, this::spawn, 20 * 3);
    }

    public void spawn() {
        for (int i = 0; i < registList.size(); i++) {
            if (registList.get(i) != null) {
                switch (i) {
                    case 0:
                        spawnNewPiece1();
                        break;

                    case 1:
                        spawnNewPiece2();
                        break;

                    case 2:
                        spawnNewPiece3();
                        break;

                    case 3:
                        spawnNewPiece4();
                        break;

                    case 4:
                        spawnNewPiece5();
                        break;
                }
            }
        }
//        this.startGameLoop();
    }

    /*
     * length
     * currentBlock = playerNumber = activeList = pieceList = upcomingBlocks
     * currentBlock - 현재 보드에서 움직일 블록
     * playerNumber - 플레이어 번호명
     * activeList - 현재 게임 플레이하고있는 플레이어들
     * pieceList - 현재 움직이는 조각이 모두 담겨진 배열
     * upcomingBlocks - 각 보드별 다음에 나올 블록 저장
     * */
    public void spawnNewPiece1() {
        if (this.firstRun1) {
            for (int i = 0; i < 5; i++) {
                int randomIdx = (int) (Math.random() * 8);
                upcomingPiece1[i] = randomIdx;
            }
            this.firstRun1 = false;
        }

        this.currentPiece1 = this.upcomingPiece1[0];

        for (int i = 1; i < this.upcomingPiece1.length; i++) {
            this.upcomingPiece1[i - 1] = this.upcomingPiece1[i];
        }

        this.upcomingPiece1[4] = (int) (Math.random() * 8);

        this.piece1 = new TetrisPiece(this.currentPiece1, 1, this.registList.get(0), this.board);

        this.piece1.clearUpcomingBlocks();
        this.piece1.drawUpcomingBlocks(1, upcomingPiece1);
        this.gameLoop1();
    }

    public void spawnNewPiece2() {
        if (this.firstRun2) {
            for (int i = 0; i < 5; i++) {
                int randomIdx = (int) (Math.random() * 8);
                upcomingPiece2[i] = randomIdx;
            }
            this.firstRun3 = false;
        }

        this.currentPiece2 = this.upcomingPiece2[0];

        for (int i = 1; i < this.upcomingPiece2.length; i++) {
            this.upcomingPiece2[i - 1] = this.upcomingPiece2[i];
        }

        this.upcomingPiece2[4] = (int) (Math.random() * 8);

        this.piece2 = new TetrisPiece(this.currentPiece2, 2, this.registList.get(1), this.board);

        this.piece2.clearUpcomingBlocks();
        this.piece2.drawUpcomingBlocks(2, upcomingPiece2);
        this.gameLoop2();
    }

    public void spawnNewPiece3() {
        if (this.firstRun3) {
            for (int i = 0; i < 5; i++) {
                int randomIdx = (int) (Math.random() * 8);
                upcomingPiece3[i] = randomIdx;
            }
            this.firstRun3 = false;
        }

        this.currentPiece3 = this.upcomingPiece3[0];

        for (int i = 1; i < this.upcomingPiece3.length; i++) {
            this.upcomingPiece3[i - 1] = this.upcomingPiece3[i];
        }

        this.upcomingPiece3[4] = (int) (Math.random() * 8);

        this.piece3 = new TetrisPiece(this.currentPiece3, 3, this.registList.get(2), this.board);

        this.piece3.clearUpcomingBlocks();
        this.piece3.drawUpcomingBlocks(3, upcomingPiece3);
        this.gameLoop3();
    }

    public void spawnNewPiece4() {
        if (this.firstRun4) {
            for (int i = 0; i < 5; i++) {
                int randomIdx = (int) (Math.random() * 8);
                upcomingPiece4[i] = randomIdx;
            }
            this.firstRun4 = false;
        }

        this.currentPiece4 = this.upcomingPiece4[0];

        for (int i = 1; i < this.upcomingPiece4.length; i++) {
            this.upcomingPiece4[i - 1] = this.upcomingPiece4[i];
        }

        this.upcomingPiece4[4] = (int) (Math.random() * 8);

        this.piece4 = new TetrisPiece(this.currentPiece4, 4, this.registList.get(3), this.board);

        this.piece4.clearUpcomingBlocks();
        this.piece4.drawUpcomingBlocks(4, upcomingPiece4);
        this.gameLoop4();
    }

    public void spawnNewPiece5() {
        if (this.firstRun5) {
            for (int i = 0; i < 5; i++) {
                int randomIdx = (int) (Math.random() * 8);
                upcomingPiece5[i] = randomIdx;
            }
            this.firstRun5 = false;
        }

        this.currentPiece5 = this.upcomingPiece5[0];

        for (int i = 1; i < this.upcomingPiece5.length; i++) {
            this.upcomingPiece5[i - 1] = this.upcomingPiece5[i];
        }

        this.upcomingPiece5[4] = (int) (Math.random() * 8);

        this.piece5 = new TetrisPiece(this.currentPiece5, 5, this.registList.get(4), this.board);

        this.piece5.clearUpcomingBlocks();
        this.piece5.drawUpcomingBlocks(5, upcomingPiece5);
        this.gameLoop5();
    }

    public void gameLoop1() {
        gameTask1 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

            if (!this.piece1.moveDown()) {
                gameTask1.cancel();
                board.checkLines(1);
                spawnNewPiece1();
            }
        }, tickDelay, tickDelay);
    }

    public void gameLoop2() {
        gameTask2 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

            if (!this.piece2.moveDown()) {
                gameTask2.cancel();
                board.checkLines(2);
                spawnNewPiece2();
            }
        }, tickDelay, tickDelay);
    }

    public void gameLoop3() {
        gameTask3 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

            if (!this.piece3.moveDown()) {
                gameTask3.cancel();
                board.checkLines(3);
                spawnNewPiece3();
            }

        }, tickDelay, tickDelay);
    }

    public void gameLoop4() {
        gameTask4 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

            if (!this.piece4.moveDown()) {
                gameTask4.cancel();
                board.checkLines(4);
                spawnNewPiece4();
            }
        }, tickDelay, tickDelay);
    }

    public void gameLoop5() {
        gameTask5 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

            if (!this.piece5.moveDown()) {
                gameTask5.cancel();
                board.checkLines(5);
                spawnNewPiece5();
            }
        }, tickDelay, tickDelay);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
    }

    public void endGame() {
        // 게임 멈추기
        isGameRunning = false;

//        게임 종료 시 컨트롤러 제거
        if (controller != null) {
            controller.removeControlItems();
        }

//        실행되던 타이머 즉 게임테스크 전체 삭제
//        if (gameTask != null) {
//            gameTask.cancel();
//            gameTask = null;
//        }
        gameTask1 = null;
        gameTask2 = null;
        gameTask3 = null;
        gameTask4 = null;
        gameTask5 = null;

        // 이벤트 리스너 제거
        HandlerList.unregisterAll(this);
//        HandlerList.unregisterAll(controller.onPlayerInteract(event));

        // 게임 종료 메시지
//        player.sendMessage("§c게임 종료! 최종 점수: " + score);
        standardPlayer.sendMessage("§c game over! total score: " + score);
    }

    // 플레이어 컨트롤 액션 메서드 (컨트롤러에서 호출됨)
    public void moveCurrentPieceLeft(int playerNumber) {
        switch (playerNumber) {
            case 0:
                piece1.moveLeft();
                break;

            case 1:
                piece2.moveLeft();
                break;

            case 2:
                piece3.moveLeft();
                break;

            case 3:
                piece4.moveLeft();
                break;

            case 4:
                piece5.moveLeft();
                break;
        }
    }

    public void moveCurrentPieceRight(int playerNumber) {
        switch (playerNumber) {
            case 0:
                piece1.moveRight();
                break;

            case 1:
                piece2.moveRight();
                break;

            case 2:
                piece3.moveRight();
                break;

            case 3:
                piece4.moveRight();
                break;

            case 4:
                piece5.moveRight();
                break;
        }
    }

    public void softDropCurrentPiece() {
        for (int i = 0; i < registList.size(); i++) {
            if (registList.get(i) != null) {
                switch (i) {
                    case 0:
                        piece1.moveDown();
                        break;

                    case 1:
                        piece2.moveDown();
                        break;

                    case 2:
                        piece3.moveDown();

                    case 3:
                        piece4.moveDown();

                    case 4:
                        piece5.moveDown();
                }
            }
        }
    }

    public void rotateCurrentPiece(int playerNumber) {
        switch (playerNumber) {
            case 0:
                piece1.rotate();
                break;

            case 1:
                piece2.rotate();
                break;

            case 2:
                piece3.rotate();
                break;

            case 3:
                piece4.rotate();
                break;

            case 4:
                piece5.rotate();
                break;
        }
    }

    public void hardDropCurrentPiece(int playerNumber) {
        switch (playerNumber) {
            case 0:
                piece1.hardDrop();
                break;

            case 1:
                piece2.hardDrop();
                break;

            case 2:
                piece3.hardDrop();
                break;

            case 3:
                piece4.hardDrop();
                break;

            case 4:
                piece5.hardDrop();
                break;
        }
    }

    public void holdCurrentPiece(int playerNumber) {
        /*
         * 한 턴에 단 한번만 홀딩 사용 가능
         * 이미 홀딩된 블록 존재할 시 현재 블록과 홀딩된 블록 전환
         * 홀딩된 블록이 없다면 현재 블록을 홀딩시키고 다음 나올 블록 호출
         * 공통점: 현재 블록 홀딩
         * */

        switch (playerNumber) {
            case 0:
                if (!piece1.isHolding && isGameRunning) {
                    gameTask1.cancel();
                    int currentPieceType = piece1.pieceType;
                    piece1.clearPiece(piece1.offsetY);
                    piece1.clearPiece(piece1.ghostOffsetY);
                    piece1.drawHoldingPiece(currentPieceType, playerNumber);
                }
                break;

            case 1:
                piece2.rotate();
                break;

            case 2:
                piece3.rotate();
                break;

            case 3:
                piece4.rotate();
                break;

            case 4:
                piece5.rotate();
                break;
        }
    }
}

//        if (piece != null && isGameRunning && !piece.isHolding) {
//            // 현재 게임 타이머 중지
//            gameTask.cancel();
//
//            // 현재 블록 저장
//            int currentPieceType = piece.pieceType;
//
//            // 현재 블록 제거
//            piece.clearPiece(piece.offsetY);
//            piece.clearPiece(piece.ghostOffsetY);
//
//            // 홀드 표시 업데이트
////            piece.drawHoldingPiece(currentPieceType);
//
//            if (cube == -1) {
//                // 홀드된 블록이 없는 경우: 현재 블록을 홀드하고 새 블록 생성
//                cube = currentPieceType;
////                spawnNewPiece();
//            } else {
//                // 홀드된 블록이 있는 경우: 홀드된 블록과 현재 블록 교환
//                int temp = cube;
//                cube = currentPieceType;
////                piece = new TetrisPiece(temp, standardPlayer, board);
////                piece.drawUpcomingBlocks(upcomingPieces);
////                startGameLoop();
//            }
//
//            // 이번 턴에 홀드 사용함을 표시
//            piece.isHolding = true;
///*
//*
//*
//남과 대전할 수 있는 기능: 5인 개인전 멀티플레이 기능 추가에 도움이 필요해
//<게임 틀>
//1. 개인전이므로, 플레이어 한 명당 한 보드씩 사용하면서 게임 진행
//    지금은 보드와 각 보드에 해당하는 블록의 좌표가 한 개 뿐이므로 한 명만 게임 진행 가능
//2. 보드 크기, 블록 크기는 보드 별로 달라지지 않고 항상 일정하게 유지
//3. 각 플레이어 별로 모두 같은 기능 부여
//   예시로 플레이어 A, 플레이어 B가 있다면 둘 다 블록을 잠글 수 있고, 다음에 나올 블럭을 확인할 수 있다.
//   또한 블록과 상호작용하는 방식도 똑같이 적용된다. (moveLeft, moveRight 등)
//
//<게임 룰>
//* 플레이어 A, 플레이어 B, 플레이어 C, 플레이어 D, 플레이어 E
//* 신규 룰: 플레이어 간 공격 및 수비 기능 추가
//   플레이어 한 명이 블록으로 찬 줄을 제거했다면 남은 플레이어들의 보드에 쓰레기 줄이 추가
//   남은 플레이어 보드에 추가될 쓰레기 줄은 제거된 줄에 비례
//   예시로 플레이어 A가 두 줄을 제거했다면 남은 플레이어들의 보드에 쓰레기 줄 두 개 추가
//
//
//* 플레이어 등수는 탈락한 순서 오름차순으로 결정, 먼저 탈락할수록 순위가 낮음
//* */
//
//        }

