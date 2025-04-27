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
    private final TetrisBoard board;
    private final BlockList blockList;

    public TetrisPiece piece1;
    public TetrisPiece piece2;
    public TetrisPiece piece3;
    public TetrisPiece piece4;
    public TetrisPiece piece5;

    public List<Player> onlineList;
    public List<Player> registList;
    public Player standardPlayer;
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
        this.blockList = new BlockList();

        init();
    }

    public void init() {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);

        for (int i = 0; i < registList.size(); i++) {
            if (registList.get(i) != null) {
                Player player = registList.get(i);
                int x = boardClearCoord[i][0];
                int y = boardClearCoord[i][1];
                int z = boardClearCoord[i][2];
                board.clearBoards(x, y, z);

                switch (i) {
                    case 0 :
                        int[] mainBoardSize1 = {-16, 11, -52};
                        int[] upcomingBoardSize1 = {-21, -17, -38, -59, -11};
                        int[] holdingBoardSize1 = {-35, -32, -22, -17};
                        piece1 = new TetrisPiece(i, player, board, plugin, this,
                                blockList.BLOCK_PLAYER_1, blockList.UPCOMING_BLOCKS_1, blockList.HOLDING_BLOCKS_1,
//                                boardStart, boardEnd, boardHeight);
//                                -16, 11, -52);
                                mainBoardSize1, upcomingBoardSize1, holdingBoardSize1);
                        controller1 = new TetrisController(this.plugin, piece1, i, this.registList.get(i));
                        break;

                    case 1 :
                        int[] mainBoardSize2 = {31, 58, -52};
                        int[] upcomingBoardSize2 = {26, 30, -38, -59, -11};
                        int[] holdingBoardSize2 = {-35, -32, 25, 30};
                        piece2 = new TetrisPiece(i, player, board, plugin, this,
                                blockList.BLOCK_PLAYER_2, blockList.UPCOMING_BLOCKS_2, blockList.HOLDING_BLOCKS_2,
                                mainBoardSize2, upcomingBoardSize2, holdingBoardSize2);
                        controller2 = new TetrisController(this.plugin, piece2, i, this.registList.get(i));
                        break;

                    case 2 :
                        int[] mainBoardSize3 = {78, 105, -52};
                        int[] upcomingBoardSize3 = {73, 77, -38, -59, -11};
                        int[] holdingBoardSize3 = {-35, -32, 72, 77};
                        piece3 = new TetrisPiece(i, player, board, plugin, this,
                                blockList.BLOCK_PLAYER_3, blockList.UPCOMING_BLOCKS_3, blockList.HOLDING_BLOCKS_3,
                                mainBoardSize3, upcomingBoardSize3, holdingBoardSize3);
                        controller3 = new TetrisController(this.plugin, piece3, i, this.registList.get(i));
                        break;

                    case 3 :
                        int[] mainBoardSize4 = {8, 35, 13};
                        int[] upcomingBoardSize4 = {3, 7, 28, 4, -15};
                        int[] holdingBoardSize4 = {32, 35, 2, 7};
                        piece4 = new TetrisPiece(i, player, board, plugin, this,
                                blockList.BLOCK_PLAYER_4, blockList.UPCOMING_BLOCKS_4, blockList.HOLDING_BLOCKS_4,
                                mainBoardSize4, upcomingBoardSize4, holdingBoardSize4);
                        controller4 = new TetrisController(this.plugin, piece4, i, this.registList.get(i));
                        break;

                    case 4 :
                        int[] mainBoardSize5 = {54, 84, 13};
                        int[] upcomingBoardSize5 = {49, 53, 28, 4, -15};
                        int[] holdingBoardSize5 = {32, 35, 48, 53};
                        piece5 = new TetrisPiece(i, player, board, plugin, this,
                                blockList.BLOCK_PLAYER_5, blockList.UPCOMING_BLOCKS_5, blockList.HOLDING_BLOCKS_5,
                                mainBoardSize5, upcomingBoardSize5, holdingBoardSize5);
                        controller5 = new TetrisController(this.plugin, piece5, i, this.registList.get(i));
                        break;
                }
            }
        }
    }

    public void sendGarbageLines(int attackingPlayerIndex, int linesToAdd) {
        if (linesToAdd <= 0) return;

        for (int i = 0; i < registList.size(); i++) {
            if (i == attackingPlayerIndex || registList.get(i) == null) {
                continue;
            }

            int targetX = boardClearCoord[i][0];
            int targetY = boardClearCoord[i][1];
            int targetZ = boardClearCoord[i][2];

            board.addGarbageLines(linesToAdd, targetX, targetY, targetZ);

            registList.get(i).sendMessage("§7" + linesToAdd + "줄의 공격을 받았습니다!");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
    }

    public void endGame() {
        if (!piece1.taskStatus) {
            piece1 = null;
            HandlerList.unregisterAll(controller1);
            controller1.removeControlItems();
            controller1 = null;
        }

        if (!piece2.taskStatus) {
            piece2 = null;
            HandlerList.unregisterAll(controller2);
            controller2.removeControlItems();
            controller2 = null;
        }

        if (!piece3.taskStatus) {
            piece3 = null;
            HandlerList.unregisterAll(controller3);
            controller3.removeControlItems();
            controller3 = null;
        }

        if (!piece4.taskStatus) {
            piece4 = null;
            HandlerList.unregisterAll(controller4);
            controller4.removeControlItems();
            controller4 = null;
        }

        if (!piece5.taskStatus) {
            piece5 = null;
            HandlerList.unregisterAll(controller5);
            controller5.removeControlItems();
            controller5 = null;
        }
    }
}

