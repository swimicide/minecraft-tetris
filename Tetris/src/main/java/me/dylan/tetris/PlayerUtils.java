package me.dylan.tetris;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerUtils {
//    private int n = 4;

//    public PlayerUtils() {
////        getPlayerName();
//    }

    public static List<UUID> getPlayerName() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toList());
    }
}
