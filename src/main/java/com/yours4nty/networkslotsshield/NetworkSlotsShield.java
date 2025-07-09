package com.yours4nty.networkslotsshield;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkSlotsShield extends JavaPlugin {

    private boolean protectionEnabled = true;
    private boolean debugEnabled = true;

    private final Map<String, Integer> messageCount = new HashMap<>();
    private final Map<String, Long> lastLogTime = new HashMap<>();
    private final Map<UUID, Long> lastSlotPacket = new ConcurrentHashMap<>();

    private final Set<PacketType> filteredTypes = new HashSet<>(Arrays.asList(
            PacketType.Play.Server.SET_SLOT,
            PacketType.Play.Server.WINDOW_ITEMS
    ));

    @Override
    public void onEnable() {
        logDecorated("NetworkSlotsShield ENABLED", "@YourS4nty");

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        for (PacketType type : filteredTypes) {
            manager.addPacketListener(new PacketAdapter(this, ListenerPriority.HIGHEST, type) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (!protectionEnabled) return;

                    try {
                        Player player = event.getPlayer();
                        UUID uuid = player.getUniqueId();
                        PacketContainer packet = event.getPacket();

                        long now = System.currentTimeMillis();
                        long last = lastSlotPacket.getOrDefault(uuid, 0L);
                        if (now - last < 150) {
                            log(player.getName(), "[DEBUG] Slot packet flood blocked for " + player.getName());
                            event.setCancelled(true);
                            return;
                        }
                        lastSlotPacket.put(uuid, now);

                        if (type == PacketType.Play.Server.SET_SLOT) {
                            Integer slot = packet.getIntegers().readSafely(1);
                            if (slot != null && slot >= 46) {
                                log(player.getName(), "[DEBUG] Blocked SET_SLOT packet with slot " + slot + " for " + player.getName());
                                event.setCancelled(true);
                                return;
                            } else if (debugEnabled) {
                                log(player.getName(), "[DEBUG] Valid SET_SLOT (" + slot + ") sent to " + player.getName());
                            }
                        }

                        if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                            List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                            if (items == null || items.size() > 100) {
                                log(player.getName(), "[DEBUG] Blocked abnormal WINDOW_ITEMS for " + player.getName() + " (size=" + (items != null ? items.size() : "null") + ")");
                                event.setCancelled(true);
                            }
                        }

                    } catch (Exception ex) {
                        log("global", "[template] Packet analysis error: " + ex.getClass().getSimpleName() + " -> " + ex.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {
        logDecorated("NetworkSlotsShield DISABLED", "@YourS4nty");
    }

    private void log(String key, String message) {
        String logKey = key + ":" + message;
        long now = System.currentTimeMillis();
        int count = messageCount.getOrDefault(logKey, 0);

        if (count > 0 && now - lastLogTime.getOrDefault(logKey, 0L) < 1500) {
            messageCount.put(logKey, count + 1);
        } else {
            if (count > 1) {
                Bukkit.getLogger().warning("[NetworkShield] " + message + " (x" + count + ")");
            } else if (message.contains("Blocked")) {
                Bukkit.getLogger().warning("[NetworkShield] " + message);
            } else if (debugEnabled) {
                Bukkit.getLogger().info("[NetworkShield] " + message);
            }
            messageCount.put(logKey, 1);
            lastLogTime.put(logKey, now);
        }
    }

    private void logDecorated(String status, String author) {
        String line = "§8§m----------------------------------------------------";
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(line);
        Bukkit.getConsoleSender().sendMessage("§6§lNetworkSlotsShield §7» §a" + status);
        Bukkit.getConsoleSender().sendMessage("§7Developed by §e" + author);
        Bukkit.getConsoleSender().sendMessage(line);
        Bukkit.getConsoleSender().sendMessage("");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("networkshield.toggle")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /networkshield <true|false|debug true|false>");
            return true;
        }

        if (args[0].equalsIgnoreCase("true")) {
            protectionEnabled = true;
            sender.sendMessage("§aNetworkSlotsShield §7protection enabled.");
        } else if (args[0].equalsIgnoreCase("false")) {
            protectionEnabled = false;
            sender.sendMessage("§cNetworkSlotsShield §7protection disabled.");
        } else if (args[0].equalsIgnoreCase("debug") && args.length == 2) {
            if (args[1].equalsIgnoreCase("true")) {
                debugEnabled = true;
                sender.sendMessage("§aDebug mode §7enabled.");
            } else if (args[1].equalsIgnoreCase("false")) {
                debugEnabled = false;
                sender.sendMessage("§cDebug mode §7disabled.");
            } else {
                sender.sendMessage("§eUsage: /networkshield debug <true|false>");
            }
        } else {
            sender.sendMessage("§eUsage: /networkshield <true|false|debug true|false>");
        }
        return true;
    }
}
