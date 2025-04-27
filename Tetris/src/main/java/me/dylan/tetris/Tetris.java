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
    private TetrisGameManager gameManager;

    @Override
    public void onEnable() {
        getLogger().info("Tetris game plugin has been enabled.");

        gameManager = new TetrisGameManager(this);

        Objects.requireNonNull(this.getCommand("tetris")).setExecutor(this);

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // 플러그인 종료 로직
        getLogger().info("Tetris game plugin has been disabled.");
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tetris")) {

            if (!(sender instanceof Player)) {
                return true;
            }

            List<Player> onlinePlayerList = getAllOnlinePlayer();
            Player player = (Player) sender;

            List<Player> registeredPlayers = new ArrayList<>();

            int limit = Math.min(args.length, 5);
            boolean isNull = true;
            if (args.length == 0) {
//                player.sendMessage("\n§c at least one player is needed.");
                player.sendMessage("\n§c 최소 한 명 이상의 플레이어가 필요합니다.");
                return false;
            } else if (args[0].equalsIgnoreCase("quit")) {
//                player.sendMessage("§c finishing game");
//                player.sendMessage("§c");
                gameManager.endGame(player);
                return false;
            }

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
//                player.sendMessage("\n§6please insert correct players.");
                player.sendMessage("\n§6 올바른 플레이어를 넣어주세요.");
                return false;
            }

            gameManager.startGame(onlinePlayerList, registeredPlayers);
        }
        return false;
    }
}
