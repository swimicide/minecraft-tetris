package me.dylan.tetris;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TetrisPiece {
    private static final String[] PIECE_NAMES = {"I", "small_I", "J", "L", "O", "S", "T", "Z"};

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

    private final Tetris plugin;
    private final TetrisBoard board;
    private final TetrisGame game;

    private final Player player;
    private final World world;

    public BukkitTask gameTask;
    public int currentPiece;
    private int rotation;
    private int offsetX;
    public int offsetY;
    private boolean isLocked;
    public int ghostOffsetY = 0;
    public boolean isHolding;
    public int playerNumber;
    public boolean taskStatus;

    public int[] upcomingPiece;
    public boolean firstRun = true;
    public int cube = -1;
    public int moveCount = 0;

    private final int[][][][] blockCoord;
    private final int[][][] upcomingBlock;
    private final int[][][] holdingBlock;

    private final int[] mainBoardSize;
    private final int[] upcomingBoardSize;
    private final int[] holdingBoardSize;

    public TetrisPiece(int playerNumber, Player player, TetrisBoard board, Tetris plugin, TetrisGame game,
                       int[][][][] blockCoord, int[][][] upcomingBlock, int[][][] holdingBlock,
                       int[] mainBoardSize, int[] upcomingBoardSize, int[] holdingBoardSize) {
        this.playerNumber = playerNumber;
        this.player = player;
        this.world = player.getWorld();
        this.board = board;
        this.plugin = plugin;
        this.game = game;

        this.blockCoord = blockCoord;
        this.upcomingBlock = upcomingBlock;
        this.holdingBlock = holdingBlock;

        this.mainBoardSize = mainBoardSize;
        this.upcomingBoardSize = upcomingBoardSize;
        this.holdingBoardSize = holdingBoardSize;

        this.upcomingPiece = new int[5];
        this.offsetX = 0;
        this.offsetY = 0;
        this.ghostOffsetY = 0;
        this.isLocked = false;
        this.isHolding = false;
        this.taskStatus = true;

        Bukkit.getScheduler().runTaskLater(plugin, this::generatePiece, 60);
    }

    public void generatePiece() {
        if (this.firstRun) {
            for (int i = 0; i < 5; i++) {
                int randomIdx = (int) (Math.random() * 8);
                upcomingPiece[i] = randomIdx;
            }
            this.firstRun = false;
        }

        this.currentPiece = this.upcomingPiece[0];

        for (int i = 1; i < this.upcomingPiece.length; i++) {
            this.upcomingPiece[i - 1] = this.upcomingPiece[i];
        }

        this.upcomingPiece[4] = (int) (Math.random() * 8);

        drawPiece();
        drawGhostPiece();

        clearUpcomingBlocks();
        drawUpcomingBlocks(upcomingPiece);

        gameLoop();
    }

    public void gameLoop() {
        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            moveCount += 1;

            if (!moveDown()) {
                if (moveCount == 1) {
                    player.sendMessage("player " + playerNumber + " - game over");
                    gameTask.cancel();
                    taskStatus = false;
                }

                int clearedLines = board.checkLines(playerNumber + 1);
                if (clearedLines > 0) {
                    game.sendGarbageLines(playerNumber, clearedLines);
                }

                generatePiece();
                moveCount = 0;
            }
        }, 20, 20);
    }

    public void drawUpcomingBlocks(int[] upcomingPieces) {
        int newOffsetY = 0;

        for (int i = 0; i < upcomingPieces.length; i++) {
            int blockNumber = upcomingPieces[i];
            if (blockNumber < 0 || blockNumber >= PIECE_NAMES.length) {
                continue;
            }

            int[][] coordination = upcomingBlock[blockNumber];

            if (coordination == null) {
                continue;
            }

            Material blockType = PIECE_MATERIALS[blockNumber];

            for (int[] coord : coordination) {
                if (coord == null || coord.length < 3) continue;

                int baseX = coord[0];
                int baseY = coord[1];
                int baseZ = coord[2];

                int finalX = baseX;
                int finalY = baseY + newOffsetY;
                int finalZ = baseZ;

                Block block = world.getBlockAt(finalX, finalY, finalZ);
                block.setType(blockType);
            }
            newOffsetY -= 4;
        }
    }

    public void clearUpcomingBlocks() {
        int xStart = upcomingBoardSize[0]; // -21
        int xLimit = upcomingBoardSize[1]; // -17
        int yStart = upcomingBoardSize[2]; // -38
        int yLimit = upcomingBoardSize[3]; // -58
        int z = upcomingBoardSize[4];

        for (int i = yStart; i > yLimit; i--) {
            for (int j = xStart; j < xLimit; j++) {
                Block block = world.getBlockAt(j, i, z);
                block.setType(Material.AIR);
            }
        }
    }

    public void drawGhostPiece() {
        ghostOffsetY = 0;
//        int[][] coordinates = getCurrentBlockCoordinates(currentPiece, rotation)[playerNumber];
        int[][] coordinates = blockCoord[currentPiece][rotation];
        while (!checkCollision(rotation, ghostOffsetY)) {
            ghostOffsetY -= 1;
        }
        ghostOffsetY += 1;

        for (int[] coord : coordinates) {
            int X = coord[0] + offsetX;
            int Y = coord[1] + ghostOffsetY;
            int Z = coord[2];

            Block block = world.getBlockAt(X, Y, Z);
            block.setType(GHOST_MATERIAL);
        }
    }

    public void drawPiece() {
        if (isLocked) {
            return;
        }

//        int[][] coordination = getCurrentBlockCoordinates(currentPiece, rotation)[playerNumber];
        int[][] coordinates = blockCoord[currentPiece][rotation];

        for (int[] coord: coordinates) {
            int worldX = coord[0] + offsetX;
            int worldY = coord[1] + offsetY;
            int worldZ = coord[2];

            Block block = world.getBlockAt(worldX, worldY, worldZ);
            block.setType(PIECE_MATERIALS[currentPiece]);
        }
    }

    public void clearPiece(int _offsetY) {
        if (isLocked) return;

//        int[][] coordinates = getCurrentBlockCoordinates(currentPiece, rotation)[playerNumber];
        int[][] coordinates = blockCoord[currentPiece][rotation];

        for (int[] coord : coordinates) {
            int x = coord[0] + offsetX;
            int y = coord[1] + _offsetY;
            int z = coord[2];

            Block block = world.getBlockAt(x, y, z);
            block.setType(Material.AIR);
        }
    }

    public boolean moveDown() {
        moveCount += 1;
        if (isLocked) {
            return false;
        }
        clearPiece(offsetY);
        clearPiece(ghostOffsetY);

        offsetY -= 1;

        if (checkCollision(rotation, offsetY)) {
            offsetY += 1;
            drawGhostPiece();
            drawPiece();
            lock();
            return false;
        }

        drawGhostPiece();
        drawPiece();
        return true;
    }

    public void moveLeft() {
        if (isLocked) {
            return;
        }

        clearPiece(offsetY);
        clearPiece(ghostOffsetY);

        offsetX -= 1;

        if (checkCollision(rotation, offsetY)) {
            offsetX += 1;
            drawGhostPiece();
            drawPiece();
        }

        drawGhostPiece();
        drawPiece();
    }

    public void moveRight() {
        if (isLocked) {
            return;
        }

        clearPiece(offsetY);
        clearPiece(ghostOffsetY);
        offsetX += 1;

        if (checkCollision(rotation, offsetY)) {
            offsetX -= 1;
            drawGhostPiece();
            drawPiece();
        }

        drawGhostPiece();
        drawPiece();
    }

    public void rotate() {
        if (isLocked) return;

        clearPiece(offsetY);
        clearPiece(ghostOffsetY);

        int newRotation = (rotation + 1) % blockCoord[currentPiece].length;
        if (!checkCollision(newRotation, offsetY)) {
            rotation = newRotation;
            drawPiece();
            drawGhostPiece();
            return;
        }
        drawGhostPiece();
        drawPiece();
    }

    public void hardDrop() {
        moveCount += 1;
        if (isLocked) return;

        clearPiece(offsetY);
        clearPiece(ghostOffsetY);

        while (!checkCollision(rotation, offsetY)) {
            offsetY -= 1;
        }

        offsetY += 1;
        drawPiece();
        lock();
    }

    public boolean checkCollision(int newRotation, int _offsetY) {
        boolean isCollision = false;
        int[][] currentShape = blockCoord[currentPiece][newRotation];

        for (int[] coord : currentShape) {
            int x = coord[0] + offsetX;
            int y = coord[1] + _offsetY;
            int z = coord[2];

            int xStart = mainBoardSize[0];
            int xLimit = mainBoardSize[1];
            int yLimit = mainBoardSize[2];

            if (y < yLimit || x < xStart || x > xLimit) {
                isCollision = true;
            }

            if (board.hasBlockAt(x, y, z)) {
                isCollision = true;
            }
        }
        return isCollision;
    }

    public void lock() {
        if (isLocked) {
            return;
        }

        this.isLocked = true;

        int[][] coordinates = blockCoord[currentPiece][rotation];
        for (int[] coord: coordinates) {
            int x = coord[0] + offsetX;
            int y = coord[1] + offsetY;
            int z = coord[2];

            board.addLockedBlock(x, y, z, PIECE_MATERIALS[currentPiece]);
        }
    }

    public void holdBlock() {
        if (!this.isHolding) {
            gameTask.cancel();
            int currentPieceType = this.currentPiece;
            clearPiece(this.offsetY);
            clearPiece(this.ghostOffsetY);
            drawHoldingPiece(currentPieceType);
            if (cube == -1) {
                cube = currentPieceType;
                generatePiece();
            } else {
                int temp = cube;
                cube = currentPieceType;
                currentPiece = temp;
                upcomingPiece[0] = currentPiece;
                drawPiece();
                drawGhostPiece();
                drawUpcomingBlocks(upcomingPiece);
                gameLoop();
            }
            this.isHolding = true;
            drawHoldingPiece(currentPieceType);
        }
    }

    public void drawHoldingPiece(int pieceType) {
        int yStart = holdingBoardSize[0];
        int yLimit = holdingBoardSize[1];
        int xStart = holdingBoardSize[2];
        int xLimit = holdingBoardSize[3];

        for (int i = yStart; i < yLimit; i++) {
            for (int j = xStart; j < xLimit; j++) {
                Block block = world.getBlockAt(j, i, -12);
                block.setType(Material.AIR);
            }
        }
        int[][] cubeCoord = holdingBlock[pieceType];
        for (int[] coord : cubeCoord) {
            int x = coord[0];
            int y = coord[1];
            int z = coord[2];

            Block block = world.getBlockAt(x, y, z);
            block.setType(PIECE_MATERIALS[pieceType]);
        }
    }
}
