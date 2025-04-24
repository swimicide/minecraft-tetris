package me.dylan.tetris;

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
    public boolean isGameRunning = false;
    public TetrisGame game;
//    멀티플레이어 게임 관리 변수

    public TetrisGameManager(Tetris plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
    }

    public void startGame(List<Player>onlinePlayerList, List<Player>registeredPlayerList) {
//        if (isGameRunning) {
//            for (Player p : onlinePlayerList) {
//                p.sendMessage("§c이미 테트리스 게임을 진행 중입니다!");
//                return;
//            }
//        }

        /*
        * online: 서버 내 모든 플레이어
        * active: null 제외된 진짜 플레이어만 모음 *게임을 하고 있는 플레이어들 중*
        * registered: null까지 포함, 플레이어가 원하는 보드에서 게임할 수 있게 만들어짐
        * 플레이어 번호: playerNumber - 앞에서부터 번호 등록, 1 2 3 4 5
        * */
//        for (Player p : activePlayerList) {
//            p.sendMessage("\ntest 1 - game manager\n"); // 성공
//        }

        game = new TetrisGame(plugin, onlinePlayerList, registeredPlayerList);
        isGameRunning = true;

//        game.startGame();
    }

    public void endGame(Player player) {
//        UUID playerId = player.getUniqueId();

        // 게임 중인지 확인
//        TetrisGame game = activeGames.get(playerId);
//        TetrisGame game = new TetrisGame();
        if (!isGameRunning || game == null) {
            player.sendMessage("§c진행 중인 테트리스 게임이 없습니다!");
            return;
        }

        game.endGame();
//        HandlerList.unregisterAll();
//        activeGames.remove(playerId);
        isGameRunning = false;
        player.sendMessage("stopped game");
//        player.sendMessage("§a테트리스 게임을 종료했습니다.");
    }

//    public void stopAllGames() {
//        for (TetrisGame game : activeGames.values()) {
//            game.endGame();
//        }
//        activeGames.clear();
//    }
}
