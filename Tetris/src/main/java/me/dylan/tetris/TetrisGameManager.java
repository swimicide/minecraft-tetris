package me.dylan.tetris;

import java.util.List;
import org.bukkit.entity.Player;

public class TetrisGameManager {
    private final Tetris plugin;
    public boolean isGameRunning = false;
    public TetrisGame game;
//    멀티플레이어 게임 관리 변수

    public TetrisGameManager(Tetris plugin) {
        this.plugin = plugin;
    }

    public void startGame(List<Player>onlinePlayerList, List<Player>registeredPlayerList) {
        game = new TetrisGame(plugin, onlinePlayerList, registeredPlayerList);
        isGameRunning = true;
    }

    public void endGame(Player player) {
        if (!isGameRunning || game == null) {
//            player.sendMessage("§c no games are running.");
            player.sendMessage("§c 현제 실행중인 게임이 없습니다.");
            return;
        }

        game.endGame();
        isGameRunning = false;
//        player.sendMessage("§c stopped game.");
        player.sendMessage("§c 게임을 정지시켰습니다.");
    }
}
