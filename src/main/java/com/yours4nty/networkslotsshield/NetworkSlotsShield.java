package com.yours4nty.networkslotsshield;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkSlotsShield extends JavaPlugin implements Listener {

    private boolean protectionEnabled = true;
    private boolean debugEnabled = true;

    private final Map<String, Integer> messageCount = new HashMap<>();
    private final Map<String, Long> lastLogTime = new HashMap<>();
    private final Map<UUID, Long> lastSlotPacket = new ConcurrentHashMap<>();

    private final Set<UUID> recentlyTeleported = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final Set<PacketType> filteredTypes = new HashSet<>(Arrays.asList(
            PacketType.Play.Server.SET_SLOT,
            PacketType.Play.Server.WINDOW_ITEMS
    ));

    @Override
    public void onEnable() {
        printStartupBanner();
        Bukkit.getPluginManager().registerEvents(this, this);

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        // Listen to filtered packet types
        for (PacketType type : filteredTypes) {
            manager.addPacketListener(new PacketAdapter(this, ListenerPriority.HIGHEST, type) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (!protectionEnabled) return;

                    try {
                        Player player = event.getPlayer();
                        UUID uuid = player.getUniqueId();
                        PacketContainer packet = event.getPacket();

                        // Allow packets right after teleport or inventory open
                        if (recentlyTeleported.contains(uuid)) return;

                        long now = System.currentTimeMillis();
                        long last = lastSlotPacket.getOrDefault(uuid, 0L);
                        if (now - last < 150) {
                            log(player.getName(), "§e[DEBUG] §cSlot packet flood blocked for §6" + player.getName());
                            event.setCancelled(true);
                            return;
                        }
                        lastSlotPacket.put(uuid, now);

                        // Filter SET_SLOT packets
                        if (type == PacketType.Play.Server.SET_SLOT) {
                            Integer slot = packet.getIntegers().readSafely(1);
                            ItemStack item = packet.getItemModifier().readSafely(0);

                            if (slot != null) {
                                int invSize = player.getOpenInventory().getTopInventory().getSize();
                                InventoryType invType = player.getOpenInventory().getTopInventory().getType();
                                int maxAllowed = invSize + 36;

                                if (
                                    slot < maxAllowed ||
                                    item == null ||
                                    item.getType() == Material.AIR ||
                                    invType != InventoryType.CHEST
                                ) {
                                    if (debugEnabled) {
                                        log(player.getName(), "§e[DEBUG] §aAllowed SET_SLOT §7slot=" + slot + " (safe)");
                                    }
                                    return;
                                }

                                log(player.getName(), "§e[DEBUG] §cBlocked SET_SLOT: §7slot " + slot + " > max " + maxAllowed);
                                event.setCancelled(true);
                                return;
                            }
                        }

                        // Filter abnormal WINDOW_ITEMS packets
                        if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                            List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                            if (items == null || items.size() > 150) {
                                log(player.getName(), "§e[DEBUG] §cBlocked abnormal WINDOW_ITEMS for §6" + player.getName() + " §7(size=" + (items != null ? items.size() : "null") + ")");
                                event.setCancelled(true);
                            }
                        }

                    } catch (Exception ex) {
                        log("global", "§c[ERROR] §7Packet analysis exception: §e" + ex.getClass().getSimpleName() + " -> " + ex.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {
        printShutdownBanner();
    }

    // Temporary packet allowance after world change
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        recentlyTeleported.add(uuid);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            recentlyTeleported.remove(uuid);
            if (debugEnabled) log(player.getName(), "§e[DEBUG] §7Protection re-enabled after world change.");
        }, 20L); // 1 second
    }

    // Temporary packet allowance after inventory open
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();
        recentlyTeleported.add(uuid);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            recentlyTeleported.remove(uuid);
            if (debugEnabled) log(player.getName(), "§e[DEBUG] §7Packet filter re-enabled after inventory open.");
        }, 20L);
    }

    // Logging system with throttling and formatting
    private void log(String key, String message) {
        String logKey = key + ":" + message;
        long now = System.currentTimeMillis();
        int count = messageCount.getOrDefault(logKey, 0);

        if (count > 0 && now - lastLogTime.getOrDefault(logKey, 0L) < 1500) {
            messageCount.put(logKey, count + 1);
        } else {
            if (count > 1) {
                Bukkit.getLogger().warning("§6[NetworkShield] " + message + " §7(x" + count + ")");
            } else if (message.contains("Blocked")) {
                Bukkit.getLogger().warning("§6[NetworkShield] " + message);
            } else if (debugEnabled) {
                Bukkit.getLogger().info("§7[NetworkShield] " + message);
            }
            messageCount.put(logKey, 1);
            lastLogTime.put(logKey, now);
        }
    }

    // Prints startup info with decoration
    private void printStartupBanner() {
        Bukkit.getConsoleSender().sendMessage("§6");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §m--------------------------------------------------");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r   §a+==========================+");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r   §a|   §eNetworkSlotsShield   §a|");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r   §a|--------------------------|");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r   §a|     §fProtocol Filter     §a|");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r   §a+==========================+");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §m--------------------------------------------------");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r §7Version: §f1.0");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r §7Author: §bYourS4nty");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r §7GitHub: §9https://github.com/YourS4nty/NetworkShield");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §m--------------------------------------------------");
        Bukkit.getConsoleSender().sendMessage(" ");
    }

    private void printShutdownBanner() {
        Bukkit.getConsoleSender().sendMessage("§c");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §m--------------------------------------------------");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r   §cPlugin disabled.");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §r   §7Thanks for using §eNetworkSlotsShield§7!");
        Bukkit.getConsoleSender().sendMessage("§6[NetworkSlotsShield] §m--------------------------------------------------");
    }

    // Command handler to toggle protection or debug
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("networkshield.toggle")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /networkshield <true|false|debug true|false>");
            return true;
        }

        if (args[0].equalsIgnoreCase("true")) {
            protectionEnabled = true;
            sender.sendMessage("§aNetworkSlotsShield §7protection §aenabled§7.");
        } else if (args[0].equalsIgnoreCase("false")) {
            protectionEnabled = false;
            sender.sendMessage("§cNetworkSlotsShield §7protection §cdisabled§7.");
        } else if (args[0].equalsIgnoreCase("debug") && args.length == 2) {
            if (args[1].equalsIgnoreCase("true")) {
                debugEnabled = true;
                sender.sendMessage("§aDebug mode §7is now §aenabled§7.");
            } else if (args[1].equalsIgnoreCase("false")) {
                debugEnabled = false;
                sender.sendMessage("§cDebug mode §7is now §cdisabled§7.");
            } else {
                sender.sendMessage("§eUsage: /networkshield debug <true|false>");
            }
        } else {
            sender.sendMessage("§eUsage: /networkshield <true|false|debug true|false>");
        }
        return true;
    }
}
