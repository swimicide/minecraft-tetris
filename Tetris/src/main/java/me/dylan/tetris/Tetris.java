package me.dylan.tetris;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class Tetris extends JavaPlugin {
    public boolean isGameRunning = false;
    private TetrisGameManager gameManager;

    /*
    * java.lang.IllegalArgumentException:
    * The embedded resource 'config.yml' cannot be found in
    * plugins/minecraft-tetris.jarnable-sound: true
    enable-particles: true

# 점수 설정
scoring:
  single-line: 100
  double-line: 300
  triple-line: 500
  tetris: 800
    * */

    @Override
    public void onEnable() {
        // 플러그인 시작 로직

        getLogger().info("테트리스 플러그인이 활성화되었습니다!");

        // 게임 매니저 초기화
        gameManager = new TetrisGameManager(this);

        // 명령어 등록
//        this.getCommand("tetris").setExecutor(this);
        Objects.requireNonNull(this.getCommand("tetris")).setExecutor(this);

        // 설정 파일 저장
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // 플러그인 종료 로직
        getLogger().info("테트리스 플러그인이 비활성화되었습니다!");
//        // 실행 중인 모든 게임 종료
//        if (gameManager != null) {
//            gameManager.stopAllGames();
//        }
    }

    public static List<UUID> getAllOnlinePlayerUUIDs() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toList());
    }

    public static List<Player> getAllOnlinePlayer() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getPlayer)
                .collect(Collectors.toList());
    }


    @Override
//    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tetris")) {

            if (!(sender instanceof Player)) {
//                sender.sendMessage("§c이 명령어는 플레이어만 사용할 수 있습니다!");
                return true;
            }

            List<Player> onlinePlayerList = getAllOnlinePlayer();
            Player player = (Player) sender;

            List<Player> registeredPlayers = new ArrayList<>();
            List<Player> activePlayerList = new ArrayList<>();

            int limit = Math.min(args.length, 5);
            boolean isNull = true;
            if (args.length == 0) {
                player.sendMessage("\nat least one player is needed.");
                return false;
            }
//            else if (args[0].equals("quit")) {
//                player.sendMessage("finishing game");
//                gameManager.endGame(player);
//            }

            for (int i = 0; i < limit; i++) {
                Player p = Bukkit.getPlayer(args[i]);

                if (p == null) {
                    registeredPlayers.add(null);
                } else {
                    isNull = false;
                    registeredPlayers.add(p);
                }
            }

            if (isNull) {
                player.sendMessage("\nplease insert correct players.");
                return false;
            }

//            player.sendMessage("\nonline players" + onlinePlayerList);
//            player.sendMessage("active players" + activePlayerList);
//            player.sendMessage("registered players" + registeredPlayers);

            gameManager.startGame(onlinePlayerList, registeredPlayers);

//            if (Objects.equals(args[1], "quit")) {
//                for (Player p : activePlayerList) {
//                    gameManager.endGame(p);
//                }
//            }

//            if (args.length == 0) {
                // 기본 명령어 - 게임 시작
//                gameManager.startGame(player);
//                return true;
//            }
//            else if (args.length == 1) {
//                if (args[0].equalsIgnoreCase("quit")) {
//                    // 게임 종료
//                    gameManager.endGame(player);
//                    return true;
//                }
//                else if (args[0].equalsIgnoreCase("help")) {
//                    // 도움말 표시
//                    showHelp(player);
//                    return true;
//                }
//                } else if (args[0].equals("NULL")) {
//                    player.sendMessage("player 1 is null");
//                }
//                Player p1 =  new Player(); // args[0].getUniqueId();
//                CommandSender sender1 = (CommandSender) player;
//                player.sendMessage("sender1: " + sender1);
//                Player p1 = (Player) args[0];
//            }


//             잘못된 명령어
//            showHelp(player);
//            return true;
        }
        return false;
    }

    private void showHelp(Player player) {
        player.sendMessage("§a==== 테트리스 명령어 ====");
        player.sendMessage("§f/tetris - 테트리스 게임 시작");
        player.sendMessage("§f/tetris quit - 진행 중인 게임 종료");
        player.sendMessage("§f/tetris help - 이 도움말 표시");
    }
}
