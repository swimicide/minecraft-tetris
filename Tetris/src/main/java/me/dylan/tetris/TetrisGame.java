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
    private TetrisController controller1;
    private TetrisController controller2;
    private TetrisController controller3;
    private TetrisController controller4;
    private TetrisController controller5;
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
    public List<Player> active;
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

    public TetrisGame(Tetris plugin, List<Player> onlinePlayerList, List<Player> registeredPlayerList) {
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

//        standardPlayer.sendMessage("\ntest: finish game init()");
//        standardPlayer.sendMessage("registered: " + registeredPlayerList);
//        보드 초기화
        for (int i = 0; i < registeredPlayerList.size(); i++) {
            Player p = registeredPlayerList.get(i);
            if (p != null) {
//                standardPlayer.sendMessage("player: " + p);
                int x = boardClearCoord[i][0];
                int y = boardClearCoord[i][1];
                int z = boardClearCoord[i][2];
                board.clearBoards(x, y, z);
                switch (i) {
                    case 0 :
                        controller1 = new TetrisController(this.plugin, this, i, this.registList.get(i));
                        break;

                    case 1 :
                        controller2 = new TetrisController(this.plugin, this, i, this.registList.get(i));
                        break;

                    case 2 :
                        controller3 = new TetrisController(this.plugin, this, i, this.registList.get(i));
                        break;

                    case 3 :
                        controller4 = new TetrisController(this.plugin, this, i, this.registList.get(i));
                        break;

                    case 4 :
                        controller5 = new TetrisController(this.plugin, this, i, this.registList.get(i));
                        break;
                }
            }
        }
        startGame();
    }

    /**
     * 다른 활성 플레이어 보드에 쓰레기 줄을 추가하는 로직
     * @param attackingPlayerIndex 공격하는 플레이어의 인덱스 (0~4)
     * @param linesToAdd 추가할 쓰레기 줄 수
     */
    private void sendGarbageLines(int attackingPlayerIndex, int linesToAdd) {
        if (linesToAdd <= 0) return;

        for (int i = 0; i < registList.size(); i++) {
            // 자기 자신 또는 비활성 플레이어는 건너<0xEB><0x9A><0x8D>기
            if (i == attackingPlayerIndex || registList.get(i) == null) {
                continue;
            }

            // 대상 보드의 좌표 정보 가져오기
            int targetX = boardClearCoord[i][0];
            int targetY = boardClearCoord[i][1];
            int targetZ = boardClearCoord[i][2];

            // TetrisBoard의 메서드를 호출하여 쓰레기 줄 추가 요청
            board.addGarbageLines(linesToAdd, targetX, targetY, targetZ);

            // (선택사항) 쓰레기 줄이 추가되었음을 대상 플레이어에게 알림
             registList.get(i).sendMessage("§7" + linesToAdd + "줄의 공격을 받았습니다!");
        }
    }

    public void startGame() {
//         게임 초기화
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

//        standardPlayer.sendMessage("\ncurrentPiece: " + currentPiece1);
//        standardPlayer.sendMessage("\nnextPiece: " + Arrays.toString(upcomingPiece1));

        this.piece1 = new TetrisPiece(this.currentPiece1, 0, this.registList.get(0), this.board);
//        standardPlayer.sendMessage("\npiece: " + piece1);

        this.piece1.clearUpcomingBlocks();
        this.piece1.drawUpcomingBlocks(0, upcomingPiece1);
        this.gameLoop1();
    }

    public void spawnNewPiece2() {
        if (this.firstRun2) {
            for (int i = 0; i < 5; i++) {
                int randomIdx = (int) (Math.random() * 8);
                upcomingPiece2[i] = randomIdx;
            }
            this.firstRun2 = false;
        }

        this.currentPiece2 = this.upcomingPiece2[0];

        for (int i = 1; i < this.upcomingPiece2.length; i++) {
            this.upcomingPiece2[i - 1] = this.upcomingPiece2[i];
        }

        this.upcomingPiece2[4] = (int) (Math.random() * 8);

//        standardPlayer.sendMessage("\ncurrentPiece: " + currentPiece2);
//        standardPlayer.sendMessage("\nnextPiece: " + Arrays.toString(upcomingPiece2));

        this.piece2 = new TetrisPiece(this.currentPiece2, 1, this.registList.get(1), this.board);

//        standardPlayer.sendMessage("\npiece: " + piece2);

        this.piece2.clearUpcomingBlocks();
        this.piece2.drawUpcomingBlocks(1, upcomingPiece2);
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

        this.piece3 = new TetrisPiece(this.currentPiece3, 2, this.registList.get(2), this.board);

        this.piece3.clearUpcomingBlocks();
        this.piece3.drawUpcomingBlocks(2, upcomingPiece3);
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

        this.piece4 = new TetrisPiece(this.currentPiece4, 3, this.registList.get(3), this.board);

        this.piece4.clearUpcomingBlocks();
        this.piece4.drawUpcomingBlocks(3, upcomingPiece4);
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

        this.piece5 = new TetrisPiece(this.currentPiece5, 4, this.registList.get(4), this.board);

        this.piece5.clearUpcomingBlocks();
        this.piece5.drawUpcomingBlocks(4, upcomingPiece5);
        this.gameLoop5();
    }

    public void gameLoop1() {
        standardPlayer.sendMessage("loop1()");
        gameTask1 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

            if (piece1 == null || !piece1.moveDown()) { // piece1 null 체크 추가
                if (gameTask1 != null) gameTask1.cancel(); // 타이머 취소 먼저

                if (piece1 != null) { // 블록이 성공적으로 lock 된 경우에만 라인 체크 및 공격
                    int clearedLines = board.checkLines(1); // checkLines는 0부터 시작하는 인덱스가 아닌 보드 번호(1) 사용
                    Player player1 = registList.get(0); // 플레이어 2 가져오기 (null 가능성 있음)
                    if(player1 != null) player1.sendMessage("Player 1 cleared lines: " + clearedLines); // 디버깅
                    standardPlayer.sendMessage("Player 1 cleared lines: " + clearedLines); // 디버깅

                    // 클리어한 라인이 있으면 다른 플레이어에게 쓰레기 줄 보내기
                    if (clearedLines > 0) {
                        // 공격 로직: attackingPlayerIndex는 0 (플레이어 1)
                        sendGarbageLines(0, clearedLines);
                    }
                }

                // 새 블록 생성 (타이머가 취소된 후 실행)
                if (isGameRunning) { // 게임이 여전히 실행 중일 때만 새 블록 생성
                    spawnNewPiece1();
                }
            }
        }, tickDelay, tickDelay);
    }

    public void gameLoop2() {
        standardPlayer.sendMessage("loop2()");
        gameTask2 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

//            if (!this.piece2.moveDown()) {
//                gameTask2.cancel();
////                board.checkLines(2);
//                standardPlayer.sendMessage("cleared lines: " + board.checkLines(2));
//                spawnNewPiece2();
//            }
            if (piece2 == null || !piece2.moveDown()) { // piece1 null 체크 추가
                if (gameTask2 != null) gameTask2.cancel(); // 타이머 취소 먼저

                if (piece2 != null) { // 블록이 성공적으로 lock 된 경우에만 라인 체크 및 공격
                    int clearedLines = board.checkLines(2); // checkLines는 0부터 시작하는 인덱스가 아닌 보드 번호(1) 사용
                    standardPlayer.sendMessage("Player 2 cleared lines: " + clearedLines); // 디버깅
                    Player player2 = registList.get(1); // 플레이어 2 가져오기 (null 가능성 있음)
                    if(player2 != null) player2.sendMessage("Player 2 cleared lines: " + clearedLines); // 디버깅

                    // 클리어한 라인이 있으면 다른 플레이어에게 쓰레기 줄 보내기
                    if (clearedLines > 0) {
                        // 공격 로직: attackingPlayerIndex는 0 (플레이어 1)
                        sendGarbageLines(1, clearedLines);
                    }
                }

                // 새 블록 생성 (타이머가 취소된 후 실행)
                if (isGameRunning) { // 게임이 여전히 실행 중일 때만 새 블록 생성
                    spawnNewPiece2();
                }
            }

        }, tickDelay, tickDelay);
    }

    public void gameLoop3() {
        standardPlayer.sendMessage("loop3()");
        gameTask3 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

//            if (!this.piece3.moveDown()) {
//                gameTask3.cancel();
////                board.checkLines(3);
//                standardPlayer.sendMessage("cleared lines: " + board.checkLines(3));
//                spawnNewPiece3();
//            }

            if (piece3 == null || !piece3.moveDown()) { // piece1 null 체크 추가
                if (gameTask3 != null) gameTask3.cancel(); // 타이머 취소 먼저

                if (piece3 != null) { // 블록이 성공적으로 lock 된 경우에만 라인 체크 및 공격
                    int clearedLines = board.checkLines(3); // checkLines는 0부터 시작하는 인덱스가 아닌 보드 번호(1) 사용
                    standardPlayer.sendMessage("Player 3 cleared lines: " + clearedLines); // 디버깅
                    Player player3 = registList.get(2); // 플레이어 2 가져오기 (null 가능성 있음)
                    if(player3 != null) player3.sendMessage("Player 3 cleared lines: " + clearedLines); // 디버깅

                    // 클리어한 라인이 있으면 다른 플레이어에게 쓰레기 줄 보내기
                    if (clearedLines > 0) {
                        // 공격 로직: attackingPlayerIndex는 0 (플레이어 1)
                        sendGarbageLines(2, clearedLines);
                    }
                }

                // 새 블록 생성 (타이머가 취소된 후 실행)
                if (isGameRunning) { // 게임이 여전히 실행 중일 때만 새 블록 생성
                    spawnNewPiece3();
                }
            }

        }, tickDelay, tickDelay);
    }

    public void gameLoop4() {
        standardPlayer.sendMessage("loop4()");
        gameTask4 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

//            if (!this.piece4.moveDown()) {
//                gameTask4.cancel();
////                board.checkLines(4);
//                standardPlayer.sendMessage("cleared lines: " + board.checkLines(4));
//                spawnNewPiece4();
//            }

            if (piece4 == null || !piece4.moveDown()) { // piece1 null 체크 추가
                if (gameTask4 != null) gameTask4.cancel(); // 타이머 취소 먼저

                if (piece4 != null) { // 블록이 성공적으로 lock 된 경우에만 라인 체크 및 공격
                    int clearedLines = board.checkLines(4); // checkLines는 0부터 시작하는 인덱스가 아닌 보드 번호(1) 사용
                    standardPlayer.sendMessage("Player 4 cleared lines: " + clearedLines); // 디버깅
                    Player player4 = registList.get(3); // 플레이어 2 가져오기 (null 가능성 있음)
                    if(player4 != null) player4.sendMessage("Player 4 cleared lines: " + clearedLines); // 디버깅

                    // 클리어한 라인이 있으면 다른 플레이어에게 쓰레기 줄 보내기
                    if (clearedLines > 0) {
                        // 공격 로직: attackingPlayerIndex는 0 (플레이어 1)
                        sendGarbageLines(3, clearedLines);
                    }
                }

                // 새 블록 생성 (타이머가 취소된 후 실행)
                if (isGameRunning) { // 게임이 여전히 실행 중일 때만 새 블록 생성
                    spawnNewPiece4();
                }
            }

        }, tickDelay, tickDelay);
    }

    public void gameLoop5() {
        standardPlayer.sendMessage("loop5()");
        gameTask5 = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
//                        게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

//            if (!this.piece5.moveDown()) {
//                gameTask5.cancel();
////                board.checkLines(5);
//                standardPlayer.sendMessage("cleared lines: " + board.checkLines(5));
//                spawnNewPiece5();
//            }
            if (piece5 == null || !piece5.moveDown()) { // piece1 null 체크 추가
                if (gameTask5 != null) gameTask5.cancel(); // 타이머 취소 먼저

                if (piece5 != null) { // 블록이 성공적으로 lock 된 경우에만 라인 체크 및 공격
                    int clearedLines = board.checkLines(5); // checkLines는 0부터 시작하는 인덱스가 아닌 보드 번호(1) 사용
                    standardPlayer.sendMessage("Player 5 cleared lines: " + clearedLines); // 디버깅
                    Player player5 = registList.get(4); // 플레이어 2 가져오기 (null 가능성 있음)
                    if(player5 != null) player5.sendMessage("Player 5 cleared lines: " + clearedLines); // 디버깅

                    // 클리어한 라인이 있으면 다른 플레이어에게 쓰레기 줄 보내기
                    if (clearedLines > 0) {
                        // 공격 로직: attackingPlayerIndex는 0 (플레이어 1)
                        sendGarbageLines(4, clearedLines);
                    }
                }

                // 새 블록 생성 (타이머가 취소된 후 실행)
                if (isGameRunning) { // 게임이 여전히 실행 중일 때만 새 블록 생성
                    spawnNewPiece5();
                }
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
//        if (controller != null) {
//            controller.removeControlItems();
//        }

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

