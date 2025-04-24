// TetrisGameManager
/*
*package me.dylan.tetris;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class TetrisGameManager {

    private final Tetris plugin;
    private final Map<UUID, TetrisGame> activeGames;
    // 멀티플레이어 게임 관리를 위한 변수 추가
    private boolean isMultiplayerGame = false;
    private List<UUID> multiplayerPlayers;
    private List<UUID> eliminatedPlayers;
    private int playerRanking = 5; // 5인 게임 기준

    public TetrisGameManager(Tetris plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
        this.multiplayerPlayers = new ArrayList<>();
        this.eliminatedPlayers = new ArrayList<>();
    }

    // 기존 단일 플레이어 게임 시작
    public void startGame(Player player) {
        UUID playerId = player.getUniqueId();

        // 이미 게임 중인지 확인
        if (activeGames.containsKey(playerId)) {
            player.sendMessage("§c이미 테트리스 게임을 진행 중입니다!");
            return;
        }

        // 새 게임 생성
        TetrisGame game = new TetrisGame(plugin, player, playerId);
        activeGames.put(playerId, game);
        game.startGame();
    }

    // 멀티플레이어 게임 시작 메서드 추가
    public void startMultiplayerGame(List<Player> players) {
        if (players.size() < 2 || players.size() > 5) {
            Bukkit.broadcastMessage("§c멀티플레이어 게임은 2-5명의 플레이어가 필요합니다.");
            return;
        }

        isMultiplayerGame = true;
        multiplayerPlayers.clear();
        eliminatedPlayers.clear();
        playerRanking = players.size();

        // 각 플레이어마다 게임 인스턴스 생성
        for (Player player : players) {
            UUID playerId = player.getUniqueId();

            // 기존 진행 중인 게임이 있다면 종료
            if (activeGames.containsKey(playerId)) {
                endGame(player);
            }

            // 새 멀티플레이어 게임 생성
            TetrisGame game = new TetrisGame(plugin, player, playerId);
            activeGames.put(playerId, game);
            multiplayerPlayers.add(playerId);

            // 멀티플레이어 모드로 설정
            game.setMultiplayerMode(true);
            game.setGameManager(this);
        }

        // 3초 후 모든 플레이어의 게임 동시 시작
        Bukkit.broadcastMessage("§a3초 후 멀티플레이어 게임이 시작됩니다!");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID playerId : multiplayerPlayers) {
                TetrisGame game = activeGames.get(playerId);
                game.startGame();
            }
            Bukkit.broadcastMessage("§a멀티플레이어 게임이 시작되었습니다!");
        }, 20 * 3);
    }

    // 가비지 라인 전송 메서드
    public void sendGarbageToOtherPlayers(UUID senderId, int lineCount) {
        if (!isMultiplayerGame || lineCount <= 0) {
            return;
        }

        Player sender = Bukkit.getPlayer(senderId);
        if (sender != null) {
            sender.sendMessage("§a상대방에게 " + lineCount + "줄의 쓰레기를 보냈습니다!");
        }

        // 모든 플레이어에게 가비지 전송 (보낸 사람 제외)
        for (UUID playerId : multiplayerPlayers) {
            if (!playerId.equals(senderId) && activeGames.containsKey(playerId)) {
                TetrisGame game = activeGames.get(playerId);
                game.receiveGarbage(lineCount);

                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.sendMessage("§c상대방으로부터 " + lineCount + "줄의 쓰레기를 받았습니다!");
                }
            }
        }
    }

    // 플레이어 탈락 처리
    public void playerEliminated(UUID playerId) {
        if (!isMultiplayerGame || !multiplayerPlayers.contains(playerId)) {
            return;
        }

        // 탈락 처리 및 순위 부여
        multiplayerPlayers.remove(playerId);
        eliminatedPlayers.add(playerId);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage("§c게임에서 탈락했습니다! 최종 순위: " + playerRanking--);
        }

        // 모든 플레이어에게 알림
        Bukkit.broadcastMessage("§6" + (player != null ? player.getName() : "알 수 없는 플레이어") +
                               "님이 탈락했습니다! 남은 플레이어: " + multiplayerPlayers.size());

        // 게임 종료 체크
        if (multiplayerPlayers.size() <= 1) {
            // 우승자 결정
            if (multiplayerPlayers.size() == 1) {
                UUID winnerId = multiplayerPlayers.get(0);
                Player winner = Bukkit.getPlayer(winnerId);
                if (winner != null) {
                    Bukkit.broadcastMessage("§a" + winner.getName() + "님이 우승했습니다!");
                    winner.sendMessage("§a축하합니다! 당신이 우승했습니다! 최종 순위: 1");
                }
            }

            // 모든 게임 종료
            endAllMultiplayerGames();
        }
    }

    // 단일 플레이어 게임 종료
    public void endGame(Player player) {
        UUID playerId = player.getUniqueId();

        // 게임 중인지 확인
        TetrisGame game = activeGames.get(playerId);
        if (game == null) {
            return;
        }

        game.endGame();
        HandlerList.unregisterAll(game);
        activeGames.remove(playerId);
        player.sendMessage("§a테트리스 게임을 종료했습니다.");
    }

    // 모든 멀티플레이어 게임 종료
    public void endAllMultiplayerGames() {
        if (!isMultiplayerGame) {
            return;
        }

        // 아직 진행 중인 게임 모두 종료
        List<UUID> allPlayers = new ArrayList<>(multiplayerPlayers);
        allPlayers.addAll(eliminatedPlayers);

        for (UUID playerId : allPlayers) {
            if (activeGames.containsKey(playerId)) {
                TetrisGame game = activeGames.get(playerId);
                game.endGame();
                HandlerList.unregisterAll(game);
                activeGames.remove(playerId);

                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.sendMessage("§a멀티플레이어 게임이 종료되었습니다.");
                }
            }
        }

        // 멀티플레이어 게임 상태 초기화
        isMultiplayerGame = false;
        multiplayerPlayers.clear();
        eliminatedPlayers.clear();

        Bukkit.broadcastMessage("§a모든 멀티플레이어 게임이 종료되었습니다.");
    }
}
*
* */

// TetrisGame.java
/*
* package me.dylan.tetris;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Handler;

public class TetrisGame implements Listener {

    private TetrisController controller;
    private final Tetris plugin;
    private final Player player;
    private TetrisBoard board;
    private TetrisPiece piece;

    private int score;
    private int level;
    private int linesCleared;
    private boolean isGameRunning;
    private boolean collision;
    private BukkitTask gameTask;
    private final UUID playerId;
    public int[] upcomingPieces;
    public boolean firstRun = true;
    public int currentBlock = 0;
    // 홀드 관련 변수 단순화
    public int cube = -1; // 홀드된 블록의 타입을 저장 (-1은 홀드된 블록 없음)

    // 멀티플레이어 관련 변수 추가
    private boolean isMultiplayerMode = false;
    private TetrisGameManager gameManager = null;
    private List<int[]> pendingGarbage = new ArrayList<>(); // 대기중인 쓰레기 라인 [줄 수, 딜레이]
    private static final int GARBAGE_DELAY = 2; // 쓰레기 라인 추가 전 딜레이 (초)
    private boolean isEliminated = false;
    private int garbageHolePosition = -1; // 쓰레기 라인의 구멍 위치

    public TetrisGame(Tetris plugin, Player player, UUID playerId) {
        int height = 47;
        int width = 28;
        this.plugin = plugin;
        this.player = player;
        this.playerId = playerId;
        this.board = new TetrisBoard(width, height, player);
        upcomingPieces = new int [5]; // 배열 생성: 데이터 저장할 '공간'을 할당, 주소 아닌 공간
    }

    // 멀티플레이어 모드 설정
    public void setMultiplayerMode(boolean multiplayerMode) {
        this.isMultiplayerMode = multiplayerMode;
    }

    // 게임 매니저 설정
    public void setGameManager(TetrisGameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void startGame() {
        // 게임 초기화
        board.clear();
        controller = new TetrisController(plugin, this, player);

        score = 0;
        level = 1;
        linesCleared = 0;
        isGameRunning = true;
        isEliminated = false;

        // 이벤트 리스너 등록
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // 준비시간
        player.sendMessage("§a game starting in 3 seconds");
        Bukkit.getScheduler().runTaskLater(plugin, this::spawnNewPiece, 20 * 3);

        // 멀티플레이어 모드인 경우 쓰레기 라인 처리 타이머 시작
        if (isMultiplayerMode) {
            startGarbageProcessingTimer();
        }
    }

    // 쓰레기 라인 처리 타이머
    private void startGarbageProcessingTimer() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isGameRunning || pendingGarbage.isEmpty()) return;

            // 딜레이 감소
            Iterator<int[]> iterator = pendingGarbage.iterator();
            while (iterator.hasNext()) {
                int[] garbage = iterator.next();
                garbage[1]--; // 딜레이 감소

                // 딜레이가 0이 되면 쓰레기 라인 추가
                if (garbage[1] <= 0) {
                    addGarbageLines(garbage[0]);
                    iterator.remove();
                }
            }
        }, 20, 20); // 1초마다 실행
    }

    // 쓰레기 라인 추가
    private void addGarbageLines(int lineCount) {
        if (lineCount <= 0 || !isGameRunning) return;

        // 현재 블록 및 고스트 블록 지우기
        if (piece != null) {
            piece.clearPiece(piece.offsetY);
            piece.clearPiece(piece.ghostOffsetY);
        }

        // 현재 보드의 모든 블록을 lineCount만큼 위로 올림
        for (int y = -6; y >= -52 + lineCount; y--) {
            for (int x = -16; x < -16 + board.width; x++) {
                // 현재 위치의 블록을 lineCount만큼 위로 이동
                if (board.hasBlockAt(x, y, piece.z)) {
                    board.addLockedBlock(x, y + lineCount, piece.z, board.getBlockMaterial(x, y, piece.z));
                    board.clearBlockAt(x, y, piece.z);
                }
            }
        }

        // 쓰레기 라인 생성
        addGarbageRows(lineCount);

        // 블록 다시 그리기
        if (piece != null) {
            // 현재 피스가 쓰레기 라인과 충돌하는지 확인
            if (piece.checkCollision(piece.getRotation(), piece.offsetY)) {
                // 충돌하면 위로 올려서 시도
                piece.offsetY += lineCount;

                // 그래도 충돌하면 게임 오버
                if (piece.checkCollision(piece.getRotation(), piece.offsetY)) {
                    if (isMultiplayerMode && gameManager != null) {
                        isEliminated = true;
                        gameManager.playerEliminated(playerId);
                    }
                    endGame();
                    return;
                }
            }

            piece.drawGhostPiece();
            piece.drawPiece();
        }
    }

    // 쓰레기 라인 생성
    private void addGarbageRows(int lineCount) {
        for (int i = 0; i < lineCount; i++) {
            int y = -52 + i; // 맨 아래부터 쓰레기 라인 추가

            // 쓰레기 라인의 구멍 위치 결정 (랜덤)
            if (garbageHolePosition == -1) {
                garbageHolePosition = (int)(Math.random() * board.width);
            } else {
                // 인접한 구멍이 생기지 않도록 조정 (더 난이도 있게)
                int offset = (int)(Math.random() * 3) - 1; // -1, 0, 1
                garbageHolePosition = (garbageHolePosition + offset + board.width) % board.width;
            }

            // 쓰레기 라인 생성
            for (int j = 0; j < board.width; j++) {
                if (j != garbageHolePosition) {
                    board.addLockedBlock(-16 + j, y, piece.z, board.getGarbageMaterial());
                }
            }
        }
    }

    // 다른 플레이어로부터 쓰레기 라인 받기
    public void receiveGarbage(int lineCount) {
        if (!isGameRunning || lineCount <= 0) return;

        // 딜레이와 함께 쓰레기 라인 대기열에 추가
        pendingGarbage.add(new int[]{lineCount, GARBAGE_DELAY});

        // 받은 쓰레기 라인 알림
        player.sendMessage("§c " + lineCount + "줄의 쓰레기 라인이 " + GARBAGE_DELAY + "초 후 추가됩니다!");
    }

    public void spawnNewPiece() {
        // 다음 블록 생성
        if (firstRun) {
            for (int i = 1; i < 5; i++) {
                int randomIdx = (int)(Math.random() * 8);
                upcomingPieces[i] = randomIdx;
            }
            firstRun = false;
        }

        // 다음 블록 가져오기
        currentBlock = upcomingPieces[0];

        // 블록 배열 한 칸씩 앞으로 당기기
        for (int i = 1; i < upcomingPieces.length; i++) {
            upcomingPieces[i - 1] = upcomingPieces[i];
        }

        // 마지막 위치에 새 랜덤 블록 추가
        upcomingPieces[4] = (int)(Math.random() * 8);

        // 새 블록 생성
        piece = new TetrisPiece(currentBlock, player, board);

        // 다음 블록 표시 업데이트
        piece.clearUpcomingBlocks();
        piece.drawUpcomingBlocks(upcomingPieces);

        // 생성된 블록이 기존 블록과 충돌하면 게임 오버
        if (piece.checkCollision(piece.getRotation(), piece.offsetY)) {
            if (isMultiplayerMode && gameManager != null && !isEliminated) {
                isEliminated = true;
                gameManager.playerEliminated(playerId);
            }
            endGame();
            return;
        }

        // 게임 루프 시작
        startGameLoop();
    }

    public void startGameLoop() {
        long tickDelay = Math.max(2, 20 - (level * 2));
        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
             // 게임 실행 중이 아니라면 루프 종료
            if (!isGameRunning) {
                return;
            }

            if (!piece.moveDown()) { // 블록이 바닥에 닿았을 때
                gameTask.cancel();

                // 블록 고정 후 라인 제거 확인
                int clearedLines = board.checkLines();

                // 점수 계산
                if (clearedLines > 0) {
                    addScore(clearedLines);

                    // 멀티플레이어 모드에서 가비지 라인 전송
                    if (isMultiplayerMode && gameManager != null) {
                        gameManager.sendGarbageToOtherPlayers(playerId, clearedLines);
                    }
                }

                spawnNewPiece();
            }
        }, tickDelay, tickDelay);
    }

    // 점수 계산 및 레벨 업데이트
    private void addScore(int clearedLines) {
        int baseScore = 0;

        // 지운 줄 수에 따른 점수 계산
        switch (clearedLines) {
            case 1:
                baseScore = 100; // 한 줄 제거
                break;
            case 2:
                baseScore = 300; // 두 줄 제거
                break;
            case 3:
                baseScore = 500; // 세 줄 제거
                break;
            case 4:
                baseScore = 800; // 네 줄 제거 (테트리스)
                break;
        }

        // 레벨에 따른 추가 점수
        score += baseScore * level;
        linesCleared += clearedLines;

        // 10줄마다 레벨 업
        if (linesCleared >= level * 10) {
            level++;
            player.sendMessage("§a레벨 업! 현재 레벨: " + level);
        }

        // 점수 알림
        player.sendMessage("§a" + clearedLines + "줄 제거! +" + (baseScore * level) + "점 (총 " + score + "점)");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {}

    public void endGame() {
        // 이미 종료된 게임이면 무시
        if (!isGameRunning) return;

        // 게임 멈추기
        isGameRunning = false;

        // 게임 종료 시 컨트롤러 제거
        if (controller != null) {
            controller.removeControlItems();
        }

        // 실행되던 타이머 즉 게임테스크 전체 삭제
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        // 이벤트 리스너 제거
        HandlerList.unregisterAll(this);

        // 게임 종료 메시지
        if (!isMultiplayerMode) {
            player.sendMessage("§c game over! total score: " + score);
        }
    }

    // 플레이어 컨트롤 액션 메서드 (컨트롤러에서 호출됨)
    public void moveCurrentPieceLeft() {
        if (piece != null && isGameRunning) {
            piece.moveLeft();
        }
    }

    public void moveCurrentPieceRight() {
        if (piece != null && isGameRunning) {
            piece.moveRight();
        }
    }

    public void softDropCurrentPiece() {
        if (piece != null && isGameRunning) {
            piece.moveDown();
        }
    }

    public void rotateCurrentPiece() {
        if (piece != null && isGameRunning) {
            piece.rotate();
        } else {
            player.sendMessage("§7현재 위치에서는 블록을 회전시킬 수 없습니다.");
        }
    }

    public void hardDropCurrentPiece() {
        if (piece != null && isGameRunning) {
            piece.hardDrop();
        }
    }

    public void holdCurrentPiece() {
        if (piece != null && isGameRunning && !piece.isHolding) {
            // 현재 게임 타이머 중지
            gameTask.cancel();

            // 현재 블록 저장
            int currentPieceType = piece.pieceType;

            // 현재 블록 제거
            piece.clearPiece(piece.offsetY);
            piece.clearPiece(piece.ghostOffsetY);

            // 홀드 표시 업데이트
            piece.drawHoldingPiece(currentPieceType);

            if (cube == -1) {
                // 홀드된 블록이 없는 경우: 현재 블록을 홀드하고 새 블록 생성
                cube = currentPieceType;
                spawnNewPiece();
            } else {
                // 홀드된 블록이 있는 경우: 홀드된 블록과 현재 블록 교환
                int temp = cube;
                cube = currentPieceType;
                piece = new TetrisPiece(temp, player, board);
                piece.drawUpcomingBlocks(upcomingPieces);
                startGameLoop();
            }

            // 이번 턴에 홀드 사용함을 표시
            piece.isHolding = true;
        }
    }

    // 게임 상태 확인 메서드들
    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public boolean isRunning() {
        return isGameRunning;
    }
}
* */

// TetrisBoard.java
/*
* package me.dylan.tetris;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class TetrisBoard {
    private final Player player;
    private final World world;
    public final int width; // public으로 변경
    public final int height; // public으로 변경
    public final int z = -13; // public으로 변경
    private int floor = 0;
    private boolean finish = false;
    private final Material garbageMaterial = Material.GRAY_CONCRETE; // 쓰레기 라인용 블록 재질

    public TetrisBoard(int width, int height, Player player) {
//        width, height는 각 row, column의 줄 길이, 즉 블록의 개수를 나타냄.
//        좌표를 저장하는 변수가 따로 필요.
        this.width = width;
        this.height = height;
        this.player = player;
//        this.plugin = plugin;
        this.world = player.getWorld();
    }

//    게임 시작 전 테트리스 보드 초기화
    public void clear() {
        int x = -16;

        for (int i = 0; i < height; i++) {
            int y = -52 + i;
            for (int j = 0; j < width; j++) {
                Block block = world.getBlockAt(x + j, y, z);
                block.setType(Material.AIR);
            }
        }

        for (int i = -38; i > -59; i--) {
            for (int j = -21; j < -17; j++) {
                Block block = world.getBlockAt(j, i, -11);
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

//    특정 위치에 블록이 존재하는지 확인
    public boolean hasBlockAt(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        return block.getType() != Material.AIR;
    }

    // 특정 위치의 블록 재질 반환
    public Material getBlockMaterial(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        return block.getType();
    }

    // 특정 위치의 블록 제거
    public void clearBlockAt(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(Material.AIR);
    }

    // 쓰레기 라인용 블록 재질 반환
    public Material getGarbageMaterial() {
        return garbageMaterial;
    }

    // 라인 제거 결과 반환 (제거된 라인 수)
    public int checkLines() {
        int _x = -16;
        int clearedLines = 0;

        for (int i = 0; i < height; i++) {
            int y = -52 + i;
            boolean match = true;

            // 해당 줄의 모든 위치에 블록이 있는지 확인
            for (int j = 0; j < width; j++) {
                int x = _x + j;
                Block block = world.getBlockAt(x, y, z);
                if (block.getType() == Material.AIR) {
                    if (finish) {
                        moveBlocksDown(y, floor);
                        return clearedLines;
                    }
                    match = false;
                    break;
                }
            }

            if (match) {
                clearedLines++; // 제거된 라인 수 증가

                // 해당 줄의 블록 제거 (시각적으로 제거)
                for (int j = 0; j < width; j++) {
                    int x = _x + j;
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                }

                if (y < floor) {
                    floor = y;
                }
                finish = true;
            }
        }

        return clearedLines;
    }

    private void moveBlocksDown(int blockStartY, int clearedY) {
        finish = false;
        for (int y = blockStartY; y < -6; y++) {
            boolean match = true;
            for (int x = -16; x < -16 + width; x++) {
                Block block = world.getBlockAt(x, y, z);
                Block newBlock = world.getBlockAt(x, clearedY, z);
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
* */

// TetrisPiece
/*
* package me.dylan.tetris;

import net.kyori.adventure.sound.Sound;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.Arrays;

public class TetrisPiece {
    private static final String[] PIECE_NAMES = {"I", "small_I", "J", "L", "O", "S", "T", "Z"};

    // 블록 색상
    private static final Material[] PIECE_MATERIALS = {
            Material.RED_WOOL,      // I
            Material.PINK_WOOL, // small_I
            Material.ORANGE_WOOL,   // J
            Material.LIGHT_BLUE_WOOL, // L
            Material.YELLOW_WOOL,   // O
            Material.LIME_WOOL,     // S
            Material.PURPLE_WOOL,   // T
            Material.CYAN_WOOL     // Z
    };

    private static final Material GHOST_MATERIAL = Material.WHITE_STAINED_GLASS;


    // 테트리스 블록 좌표 정의 (HTML 코드 참고)
    private static final int[][][][] BLOCK_PLAYER_1 = {
            { // I
                    {
                            {-3, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {-5, -7, -13}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    },
            },

            { // small I
                    {
                            {-3, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                    },

                    {
                            {-5, -7, -13}, {}, {}, {}, {}, {}, {}, {},
                    }

            },

            { // J
                    {
                            {-3, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {-1, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {-3, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {-1, -6, -13}, {}, {}, {}
                    },

                    {
                            {-4, -7, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {0, -9, -13}, {}, {}, {}
                    },

                    {
                            {-5, -11, -13}, {}, {}, {},
                            {-3, -7, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    },
            },

            { // L

                    {
                            {-5, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {-1, -6, -13}, {}, {}, {}
                    },

                    {
                            {-3, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {-1, -10, -13}, {}, {}, {}
                    },

                    {
                            {-3, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {-3, -10, -13}, {}, {}, {}
                    },

                    {
                            {-3, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {-1, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    },
            },

            { // O
                    {
                            {-4, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // S
                    {
                            {-4, -8, -13}, {}, {}, {}, {}, {}, {}, {},
                            {-2, -6, -13}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {-4, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {-2, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // T
                    {
                            {-4, -6, -13}, {}, {}, {},
                            {-6, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {-4, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {-2, -8, -13}, {}, {}, {}
                    },

                    {
                            {-6, -7, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {-4, -9, -13}, {}, {}, {}
                    },

                    {
                            {-6, -8, -13}, {}, {}, {},
                            {-4, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // Z
                    {
                            {-4, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {-2, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {-4, -8, -13}, {}, {}, {}, {}, {}, {}, {},
                            {-2, -6, -13}, {}, {}, {}, {}, {}, {}, {}
                    }
            }
    };

    private static final int[][][][] BLOCK_PLAYER_2 = {
            { // I
                    {
                            {45, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {},
                    },

                    {
                            {43, -7, -13}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {},
                    },
            },

            { // small I
                    {
                            {45, -6, -13}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {43, -7, -13}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // J
                    {
                            {44, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {46, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {44, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {46, -6, -13}, {}, {}, {}
                    },

                    {
                            {42, -7, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {46, -9, -13}, {}, {}, {}
                    },

                    {
                            {42, -12, -13}, {}, {}, {},
                            {44, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    },
            },

            { // L

                    {
                            {42, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {46, -6, -13}, {}, {}, {}
                    },

                    {
                            {44, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {46, -10, -13}, {}, {}, {}
                    },

                    {
                            {42, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {42, -10, -13}, {}, {}, {}
                    },

                    {
                            {42, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {44, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    },
            },

            { // O
                    {
                            {43, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // S
                    {
                            {43, -8, -13}, {}, {}, {}, {}, {}, {}, {},
                            {45, -6, -13}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {43, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {45, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // T
                    {
                            {43, -6, -13}, {}, {}, {},
                            {41, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {43, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {45, -8, -13}, {}, {}, {}
                    },

                    {
                            {41, -7, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {43, -9, -13}, {}, {}, {}
                    },

                    {
                            {41, -8, -13}, {}, {}, {},
                            {43, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // Z
                    {
                            {43, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {45, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {43, -8, -13}, {}, {}, {}, {}, {}, {}, {},
                            {45, -6, -13}, {}, {}, {}, {}, {}, {}, {}
                    }
            }
    };

    private static final int[][][][] BLOCK_PLAYER_3 = {
            { // I
                    {
                            {92, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {90, -7, -13}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // small I
                    {
                            {92, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                    },

                    {
                            {90, -7, -13}, {}, {}, {}, {}, {}, {}, {},
                    }
            },

            { // J
                    {
                            {91, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {93, -8, -13}, {}, {}, {}, {}, {}, {}, {},
                    },

                    {
                            {91, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {93, -6, -13}, {}, {}, {}
                    },

                    {
                            {89, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {93, -10, -13}, {}, {}, {}
                    },

                    {
                            {89, -12, -13}, {}, {}, {},
                            {91, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // L
                    {
                            {89, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {93, -6, -13}, {}, {}, {}
                    },

                    {
                            {91, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {93, -10, -13}, {}, {}, {}
                    },

                    {
                            {89, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {89, -10, -13}, {}, {}, {}
                    },

                    {
                            {89, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {91, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // O
                    {
                            {90, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // S
                    {
                            {90, -8, -13}, {}, {}, {}, {}, {}, {}, {},
                            {92, -6, -13}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {90, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {92, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // T
                    {
                            {90, -6, -13}, {}, {}, {},
                            {88, -8, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {90, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {92, -8, -13}, {}, {}, {}
                    },

                    {
                            {88, -7, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {90, -9, -13}, {}, {}, {}
                    },

                    {
                            {88, -8, -13}, {}, {}, {},
                            {90, -6, -13}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // Z
                    {
                            {90, -6, -13}, {}, {}, {}, {}, {}, {}, {},
                            {92, -8, -13}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {90, -8, -13}, {}, {}, {}, {}, {}, {}, {},
                            {92, -6, -13}, {}, {}, {}, {}, {}, {}, {}
                    }
            }
    };

    private static final int[][][][] BLOCK_PLAYER_4 = {
            { // I
                    {
                            {22, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {20, 58, -17}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // small I
                    {
                            {22, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                    },

                    {
                            {20, 58, -17}, {}, {}, {}, {}, {}, {}, {},
                    }
            },

            { // J
                    {
                            {21, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {23, 57, -17}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {21, 59, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {23, 59, -17}, {}, {}, {}
                    },

                    {
                            {19, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {23, 55, -17}, {}, {}, {}
                    },

                    {
                            {19, 53, -17}, {}, {}, {},
                            {21, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // L
                    {
                            {19, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {23, 59, -17}, {}, {}, {}
                    },

                    {
                            {21, 59, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {23, 55, -17}, {}, {}, {}
                    },

                    {
                            {19, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {19, 55, -17}, {}, {}, {}
                    },

                    {
                            {19, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {21, 57, -17}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // O
                    {
                            {20, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // S
                    {
                            {20, 57, -17}, {}, {}, {}, {}, {}, {}, {},
                            {22, 59, -17}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {20, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {22, 57, -17}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // T
                    {
                            {20, 59, -17}, {}, {}, {},
                            {18, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {20, 59, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {22, 57, -17}, {}, {}, {}
                    },

                    {
                            {18, 58, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {20, 56, -17}, {}, {}, {}
                    },

                    {
                            {18, 57, -17}, {}, {}, {},
                            {20, 59, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // Z
                    {
                            {20, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {22, 57, -17}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {20, 57, -17}, {}, {}, {}, {}, {}, {}, {},
                            {22, 59, -17}, {}, {}, {}, {}, {}, {}, {}
                    }
            }
    };

    private static final int[][][][] BLOCK_PLAYER_5 = {
            { // I
                    {
                            {68, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {66, 58, -17}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // small_i
                    {
                            {68, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                    },

                    {
                            {66, 58, -17}, {}, {}, {}, {}, {}, {}, {},
                    }
            },

            { // J
                    {
                            {67, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {69, 57, -17}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {67, 59, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {69, 59, -17}, {}, {}, {}
                    },

                    {
                            {65, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {69, 55, -17}, {}, {}, {}
                    },

                    {
                            {65, 53, -17}, {}, {}, {},
                            {67, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // L
                    {
                            {65, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {69, 59, -17}, {}, {}, {}
                    },

                    {
                            {67, 59, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {69, 55, -17}, {}, {}, {}
                    },

                    {
                            {65, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {65, 55, -17}, {}, {}, {}
                    },

                    {
                            {65, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {67, 57, -17}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // O
                    {
                            {66, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // S
                    {
                            {66, 57, -17}, {}, {}, {}, {}, {}, {}, {},
                            {68, 59, -17}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {66, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {68, 57, -17}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // T
                    {
                            {66, 59, -17}, {}, {}, {},
                            {64, 57, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {66, 59, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {68, 57, -17}, {}, {}, {}
                    },

                    {
                            {64, 58, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                            {66, 56, -17}, {}, {}, {}
                    },

                    {
                            {64, 57, -17}, {}, {}, {},
                            {66, 59, -17}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
                    }
            },

            { // Z
                    {
                            {66, 59, -17}, {}, {}, {}, {}, {}, {}, {},
                            {68, 57, -17}, {}, {}, {}, {}, {}, {}, {}
                    },

                    {
                            {66, 57, -17}, {}, {}, {}, {}, {}, {}, {},
                            {68, 59, -17}, {}, {}, {}, {}, {}, {}, {}
                    }
            }
    };

//    "I", "small_I", "J", "L", "O", "S", "T", "Z"
    private static final int[][][] UPCOMING_BLOCKS = {
        {
//                I 블록
                {-21, -39, -11},
                {-20, -39, -11},
                {-19, -39, -11},
                {-18, -39, -11},
        },

        {
//                small I 블록
                {-21, -39, -11},
                {-20, -39, -11},
                {-19, -39, -11},
                {-18, -39, -11},
        },

        {
//                J 블록
                {-20, -38, -11},
                {-20, -39, -11},
                {-19, -39, -11},
                {-18, -39, -11}
        },

        {
//                L 블록
                {-18, -38, -11},
                {-18, -39, -11},
                {-19, -39, -11},
                {-20, -39, -11},
        },

        {
//            O블록
                {-19, -38, -11},
                {-18, -38, -11},
                {-19, -39, -11},
                {-18, -39, -11},
        },

        {
//            S블록
                {-19, -38, -11},
                {-18, -38, -11},
                {-19, -39, -11},
                {-20, -39, -11},
        },

        {
//            T블록
                {-19, -38, -11},
                {-20, -39, -11},
                {-19, -39, -11},
                {-18, -39, -11}
        },

        {
//            Z블록
                {-20, -38, -11},
                {-19, -38, -11},
                {-19, -39, -11},
                {-18, -39, -11},
        }
    };

    private static final int[][][] HOLDING_BLOCKS = {
            {
//                I 블록
                    {-21, -34, -12},
                    {-20, -34, -12},
                    {-19, -34, -12},
                    {-18, -34, -12}
            },

            {
//                small I 블록
                    {-21, -34, -12},
                    {-20, -34, -12},
                    {-19, -34, -12},
                    {-18, -34, -12}
            },

            {
//                J 블록
                    {-20, -33, -12},
                    {-20, -34, -12},
                    {-19, -34, -12},
                    {-18, -34, -12}
            },

            {
//                L 블록
                    {-20, -34, -12},
                    {-19, -34, -12},
                    {-18, -34, -12},
                    {-18, -33, -12}
            },

            {
//                O 블록
                    {-19, -33, -12},
                    {-18, -33, -12},
                    {-19, -34, -12},
                    {-18, -34, -12}
            },

            {
//            S블록
                    {-20, -33, -12},
                    {-19, -33, -12},
                    {-19, -34, -12},
                    {-18, -34, -12}
            },

            {
//            T블록
                    {-19, -33, -12},
                    {-20, -34, -12},
                    {-19, -34, -12},
                    {-18, -34, -12}
            },

            {
//            Z블록
                    {-20, -34, -12},
                    {-19, -34, -12},
                    {-19, -33, -12},
                    {-18, -33, -12}
            }
    };


    private final Player player;
    private final World world;

    public int pieceType;
    private int rotation;
    private int offsetX;
    public int offsetY;
    private boolean isLocked;
    private final TetrisBoard board;
    public int ghostOffsetY = 0;
    public boolean isHolding;
//    public int playerNumber;
    public int heldBlock = -1;
    public int cube = -1;
    public int index = 0;

    public TetrisPiece(int pieceType, Player player, TetrisBoard board) { // TetrisGame으로부터 전달받음
        this.pieceType = pieceType;
        this.player = player;
        this.world = player.getWorld();
        this.board = board;
        this.rotation = 1;
        this.offsetX = 0;
        this.offsetY = 0;
        this.ghostOffsetY = 0;
        this.isLocked = false;
        this.isHolding = false;
//        this.playerNumber = 1

        player.sendMessage("length" + BLOCK_PLAYER_1[this.pieceType].length); // 4
        for (int i = 0; i < BLOCK_PLAYER_1[this.pieceType].length; i++) {
            rotation = i;
            player.sendMessage("pieceType: " + pieceType + " rotation: " + rotation);
            generateCoordinationPoints();
        }
//        for (this.rotation = 0; this.rotation < BLOCK_PLAYER_1[this.pieceType].length; this.rotation++) {
//            generateCoordinationPoints();
//        }

        generateCoordinationPoints();
//        player.sendMessage("single run");

        player.sendMessage("next: ");
//        drawGhostPiece();
        drawPiece();
    }

    public void addCoord(int row, int column) { //, int x, int y, int z) {
        int x1 = BLOCK_PLAYER_1[pieceType][rotation][index][0], y1 = BLOCK_PLAYER_1[pieceType][rotation][index][1];
        int x2 = BLOCK_PLAYER_2[pieceType][rotation][index][0], y2 = BLOCK_PLAYER_2[pieceType][rotation][index][1];
        int x3 = BLOCK_PLAYER_3[pieceType][rotation][index][0], y3 = BLOCK_PLAYER_3[pieceType][rotation][index][1];
        int x4 = BLOCK_PLAYER_4[pieceType][rotation][index][0], y4 = BLOCK_PLAYER_4[pieceType][rotation][index][1];
        int x5 = BLOCK_PLAYER_5[pieceType][rotation][index][0], y5 = BLOCK_PLAYER_5[pieceType][rotation][index][1];

        player.sendMessage("row: " + row + " column: " + column + " addCoord");
        player.sendMessage("x: \n");
        player.sendMessage("x1: " + x1 + " x2 : " + x2 + " x3 : " + x3 + " x4 : " + x4 + " x5 : " + x5 + "\n");
        player.sendMessage("y: \n");
        player.sendMessage("y1: " + y1 + " y2 : " + y2 + " y3 : " + y3 + " y4 : " + y4 + " y5 : " + y5 + "\n");

        for (int i = 0; i < row; i++) {
            player.sendMessage("i: " + i);
            player.sendMessage("\n");
            for (int j = 0; j < column; j++) {
                player.sendMessage("j: " + j);
                BLOCK_PLAYER_1[pieceType][rotation][index] = new int[] {x1 + i, y1 - j, -13};
                BLOCK_PLAYER_2[pieceType][rotation][index] = new int[] {x2 + i, y2 - j, -13};
                BLOCK_PLAYER_3[pieceType][rotation][index] = new int[] {x3 + i, y3 - j, -13};
                BLOCK_PLAYER_4[pieceType][rotation][index] = new int[] {x4 + i, y4 - j, -17};
                BLOCK_PLAYER_5[pieceType][rotation][index] = new int[] {x5 + i, y5 - j, -17};
                index += 1;
            }
        }
        player.sendMessage("\n index: " + index);
    }


    public void generateCoordinationPoints() {
        int pointIdx = 0;
        player.sendMessage("piece: " + pieceType);

        switch(pieceType) {
            case 0 :
                switch(rotation) {
                    case 0 : // 세로
                        addCoord(2, 8);
                        break;

                    case 1 : // 가로
                        addCoord(8, 2);
                        break;
                }
                break;

            case 1 :
                switch(rotation) {
                    case 0 :
                        addCoord(1, 8);
                        break;

                    case 1 :
                        addCoord(8, 1);
                        break;
                }
                break;

            case 2 :
                switch(rotation) {
                    case 0 :
                        addCoord(2, 4);
                        addCoord(4, 2);
                        break;

                    case 1 :
                        addCoord(2, 6);
                        addCoord(2, 2);
                        break;

                    case 2 :
                        addCoord(6, 2);
                        addCoord(2, 2);
                        break;

                    case 3 :
                        addCoord(2, 2);
                        addCoord(2, 6);
                        break;
                }
                break;

            case 3 :
                switch(rotation) {
                    case 0 :
                        addCoord(6, 2);
                        addCoord(2, 2);
                        break;

                    case 1 :
                        addCoord(2, 6);
                        addCoord(2, 2);
                        break;

                    case 2 :
                        addCoord(6, 2);
                        addCoord(2, 2);
                        break;

                    case 3 :
                        addCoord(4, 2);
                        addCoord(2, 4);
                        break;
                }
                break;

            case 4 :
                addCoord(4, 4);
                break;

            case 5, 7:
                switch(rotation) {
                    case 0 :
                        addCoord(4, 2);
                        addCoord(4, 2);
                        break;

                    case 1 :
                        addCoord(2, 4);
                        addCoord(2, 4);
                        break;
                }
                break;

            case 6 :
                switch(rotation) {
                    case 0 :
                        addCoord(2, 2);
                        addCoord(6, 2);
                        break;

                    case 1 :
                        addCoord(2, 6);
                        addCoord(2, 2);
                        break;

                    case 2 :
                        addCoord(6, 2);
                        addCoord(2, 2);
                        break;

                    case 3 :
                        addCoord(2, 2);
                        addCoord(2, 6);
                        break;
                }
                break;

        }

        player.sendMessage("-----------------------------");
        index = 0;
    }

    public void drawUpcomingBlocks(int[] upcomingPieces) {
        int newOffsetY = 0;

//        for (int i = 0; i < upcomingPieces.length; i++) {
//            int blockNumber = upcomingPieces[i];
//            int[][] coordinations = UPCOMING_BLOCKS[blockNumber];
//            Material blockType = PIECE_MATERIALS[blockNumber];
//            for (int[] coord : coordinations) {
//                int x = coord[0];
//                int y = coord[1] + newOffsetY;
//                int z = coord[2];
//                Block block = world.getBlockAt(x, y, z);
//                block.setType(blockType);
//            }
//            newOffsetY -= 4;
//        }
    }

    public void clearUpcomingBlocks() {
        for (int i = -38; i > -59; i--) {
            for (int j = -21; j < -17; j++) {
                Block block = world.getBlockAt(j, i, -11);
                block.setType(Material.AIR);
            }
        }
    }

    public void drawGhostPiece() {
//        그냥 블록이 떨어져서 위치할 곳에 유리블럭 생성하면 끝
        ghostOffsetY = 0; // 핵심
//        int[][] coordinates = getCurrentBlockCoordinates(pieceType, rotation);
//        while (!checkCollision(rotation, ghostOffsetY)) {
//            ghostOffsetY -= 1;
//        }
//        ghostOffsetY += 1;
//
//        for (int[] coord : coordinates) {
//            int X = coord[0] + offsetX;
//            int Y = coord[1] + ghostOffsetY;
//            int Z = coord[2];
//
//            Block block = world.getBlockAt(X, Y, Z);
//            block.setType(GHOST_MATERIAL);
//        }
    }

    public void drawPiece() {
        if (isLocked) {
            return;
        }

        player.sendMessage("-------------------drawPiece()");
        rotation = 0;
        int[][][] coordinates = getCurrentBlockCoordinates(pieceType, rotation);
        player.sendMessage("\ncoordinates" + Arrays.deepToString(coordinates));

        for (int i = 0; i < coordinates.length; i++) {
            for (int[] coord : coordinates[i]) { // 플레이어 별 draw
            int worldX = coord[0] + offsetX;
            int worldY = coord[1] + offsetY;
            int worldZ = coord[2];

            Block block = world.getBlockAt(worldX, worldY, worldZ);
            block.setType(PIECE_MATERIALS[pieceType]);
        }
        }

//        player.playSound(player.getLocation(), Sound.sound);
    }

    public void clearPiece(int _offsetY) {
        if (isLocked) return;

//        int[][] coordinates = getCurrentBlockCoordinates(pieceType, rotation);

//        for (int[] coord : coordinates) {
//            int x = coord[0] + offsetX;
//            int y = coord[1] + _offsetY;
//            int z = coord[2];
//
//            Block block = world.getBlockAt(x, y, z);
//            block.setType(Material.AIR);
//        }
    }

    private int[][][] getCurrentBlockCoordinates(int type, int newRotation) {
//        if (type >= BLOCK_PLAYER_1.length || newRotation >= BLOCK_PLAYER_1[pieceType].length) {
        int t = type, r = newRotation;
        if (type >= BLOCK_PLAYER_2.length || newRotation >= BLOCK_PLAYER_2[pieceType].length) {
//            player.playSound(Sound.);
            player.sendMessage("§c오류: 유효하지 않은 블록 타입 또는 회전 상태입니다.");
//            return new int[0][0];
            t = 0;
            r = 0;
//            return BLOCK_PLAYER_2[0][0];
        }
        return
                new int[][][]{
                        BLOCK_PLAYER_1[t][r],
                        BLOCK_PLAYER_2[t][r],
                        BLOCK_PLAYER_3[t][r],
                        BLOCK_PLAYER_4[t][r],
                        BLOCK_PLAYER_5[t][r]
                };
    }

    // 블록 아래로 이동
    public boolean moveDown() {
        if (isLocked) {
            return false;
        }
//        ghostOffsetY = 0;
        // 이전 위치 블록 지우기
//        clearPiece(offsetY);
//        clearPiece(ghostOffsetY);

        // Y 좌표 감소 시도 (마인크래프트에서 Y는 높이)
        offsetY -= 1;

        // 충돌 확인
        if (checkCollision(rotation, offsetY)) {
            // 충돌 시 이전 위치로 돌아가기
            offsetY += 1;
            // 블록 다시 그리기
//            drawGhostPiece();
//            drawPiece();
//            player.sendMessage("moveDown() lock()");
//            lock();
            return false;
        }

        // 새 위치에 블록 그리기
//        drawGhostPiece();
//        drawPiece();
        return true;
    }

    public boolean moveLeft() {
        if (isLocked) {
            return false;
        }

//        이전 위치 블록 지우기
//        clearGhostPiece();
//        clearPiece(offsetY);
//        clearPiece(ghostOffsetY);

//        X 좌표 증가 시도
        /*
         * 왼쪽: -X, 오른쪽: +X
         * */
//offsetX -= 1;
//
////        충돌 확인
//        if (checkCollision(rotation, offsetY)) {
//offsetX += 1;
////            drawGhostPiece();
////            drawPiece();
//        return false;
//        }
//
////        새 위치에 블록 그리기
////        drawGhostPiece();
////        drawPiece();
//        return true;
//        }
//
//public boolean moveRight() {
////        보드 초기화
//    if (isLocked) {
//        return false;
//    }
//
////        clearPiece(offsetY);
////        clearPiece(ghostOffsetY);
////        이동
//    offsetX += 1;
//
////        충돌체크
//    if (checkCollision(rotation, offsetY)) {
//        offsetX -= 1;
////            drawGhostPiece();
////            drawPiece();
//        return false;
//    }
//
////        drawGhostPiece();
////        drawPiece();
//    return true;
//}
//
//public void rotate() {
//    if (isLocked) return;
//
////        clearPiece(offsetY);
////        clearPiece(ghostOffsetY);
//    player.sendMessage(PIECE_NAMES[this.pieceType] + "블록 회전");
//    // 회전 후 충돌 확인
////        int newRotation = (rotation + 1) % BLOCK_PLAYER_1[pieceType].length;
//    int newRotation = (rotation + 1) % BLOCK_PLAYER_2[pieceType].length;
//    if (!checkCollision(newRotation, offsetY)) {
//        rotation = newRotation;
//            drawPiece();
//            drawGhostPiece();
//        return;
//    }
//        drawGhostPiece();
//        drawPiece();
//}

//public void hardDrop() {
//    if (isLocked) return;

//        clearPiece(offsetY);
//        clearPiece(ghostOffsetY);

//    while (!checkCollision(rotation, offsetY)) {
//        offsetY -= 1;
//    }

//    offsetY += 1;
//        drawPiece();
//        player.sendMessage("hardDrop() lock()");
//        lock();
//}

//public boolean checkCollision(int newRotation, int _offsetY) {
//        int[][] currentShape = getCurrentBlockCoordinates(pieceType, newRotation);

//        for (int[] coord : currentShape) {
//            int x = coord[0] + offsetX;
//            int y = coord[1] + _offsetY;
//            int z = coord[2];

    // 바닥 충돌 확인
//            if (y < -52 || x < -16 || x > 11) {
//                return true;
//            }

    // 다른 블록과 충돌 확인 (lockedBlocks에 저장된 블록과 충돌 확인)
//            if (board.hasBlockAt(x, y, z)) {
//                return true;
//            }
//        }
//    return false;
//}

//public void lock() {
//    if (isLocked) {
//        return;
//    }
//
//    this.isLocked = true;

//        현재 블록을 보드에 기록
//        int[][] coordinates = getCurrentBlockCoordinates(pieceType, rotation);
//        player.sendMessage("locking block...");
//        for (int[] coord: coordinates) {
//            int x = coord[0] + offsetX;
//            int y = coord[1] + offsetY;
//            int z = coord[2];
//
////            player.sendMessage("x: " + x + " y" + y + " z" + z);
//
//            board.addLockedBlock(x, y, z, PIECE_MATERIALS[pieceType]);
//        }
//        player.sendMessage(PIECE_NAMES[pieceType] + " 블록이 고정되었습니다.");
//}

//**/



