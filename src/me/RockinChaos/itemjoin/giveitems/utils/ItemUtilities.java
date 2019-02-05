package me.RockinChaos.itemjoin.giveitems.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.ItemHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.handlers.ServerHandler;
import me.RockinChaos.itemjoin.listeners.InvClickCreative;
import me.RockinChaos.itemjoin.utils.DataStorage;
import me.RockinChaos.itemjoin.utils.Language;
import me.RockinChaos.itemjoin.utils.ProbabilityUtilities;
import me.RockinChaos.itemjoin.utils.Utils;

public class ItemUtilities {
	
  	private static List < ItemMap > items = new ArrayList < ItemMap >();
	public static Map < String, Integer > probability = new HashMap < String, Integer > ();
	private static HashMap <Integer, Integer> failCount = new HashMap <Integer, Integer> ();
	
	public static void addItem(ItemMap itemMap) {
		items.add(itemMap);
	}
	
	public static List < ItemMap > getItems() {
		return items;
	}
	
	public static void clearItems() {
		items.clear();
	}
	
	public static void updateItems(Player player, boolean newAnimation) {
		for (ItemMap item: getItems()) {
			item.updateItem(player);
			if (newAnimation) {
				item.setAnimations(player);
			}
		}
	}
	
	public static void updateItems() {
		Collection < ? > playersOnlineNew = null;
		Player[] playersOnlineOld;
		try {
			if (Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).getReturnType() == Collection.class) {
				if (Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).getReturnType() == Collection.class) {
					playersOnlineNew = ((Collection < ? > ) Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).invoke(null, new Object[0]));
					for (Object objPlayer: playersOnlineNew) {
						Player player = ((Player) objPlayer);
						updateItems(player, true);
						InvClickCreative.isCreative(player, player.getGameMode());
					}
				}
			} else {
				playersOnlineOld = ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).invoke(null, new Object[0]));
				for (Player player: playersOnlineOld) {
					updateItems(player, true);
					InvClickCreative.isCreative(player, player.getGameMode());
				}
			}
		} catch (Exception e) {
			ServerHandler.sendDebugTrace(e);
		}
	}
	
	public static void safeSet(Player player, String type) {
		InvClickCreative.isCreative(player, player.getGameMode());
		if (type.equalsIgnoreCase("JOIN") && type.equalsIgnoreCase("WorldChanged")) { 
			Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
				public void run() {
					ItemUtilities.setClearingOfItems(player, player.getWorld().getName(), "Clear-On-" + type);
				}
			}, ConfigHandler.getClearDelay());
		}
		if (!type.equalsIgnoreCase("Region-Enter") && !type.equalsIgnoreCase("Limit-Modes")) { PlayerHandler.setHeldItemSlot(player); }
		ItemUtilities.updateItems(player, false);
	}
	
	public static String getProbabilityItem(Player player) {
		ProbabilityUtilities probabilities = new ProbabilityUtilities();
		if (!probability.isEmpty()) {
			for (String name: probability.keySet()) {
				for (ItemMap item: getItems()) {
					if (item.getConfigName().equalsIgnoreCase(name) && item.hasItem(player)) { return name; }
				}
				probabilities.addChance(name, probability.get(name));
			}
			return ((String) probabilities.getRandomElement());
		}
		return null;
	}
	
	public static boolean isChosenProbability(ItemMap itemMap, String probable) {
		if (probable != null && itemMap.getConfigName().equalsIgnoreCase(probable) || itemMap.getProbability().equals(-1)) {
			return true;
		}
		return false;
	}
	
	public static boolean hasProbabilityItem(Player player, ItemMap itemMap) {
		for (String probables: probability.keySet()) {
			for (ItemMap item: getItems()) {
				if (item.getConfigName().equalsIgnoreCase(probables) && item.hasItem(player)) {
					if (itemMap.equals(item) || !itemMap.getConfigName().equalsIgnoreCase(item.getConfigName())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static Boolean inClearingWorld(String world, String stringLoc) {
		if (ConfigHandler.getConfig("config.yml").getString(stringLoc) != null) {
			String worldlist = ConfigHandler.getConfig("config.yml").getString(stringLoc).replace(" ", "");
			String[] compareWorlds = worldlist.split(",");
			for (String compareWorld: compareWorlds) {
				if (compareWorld.equalsIgnoreCase(world) || compareWorld.equalsIgnoreCase("all") || compareWorld.equalsIgnoreCase("global")) {
					return true;
				}
			}
		} else if (ConfigHandler.getConfig("config.yml").getString(stringLoc) == null) {
			return true;
		}
		return false;
	}
	
	public static void setClearingOfItems(Player player, String world, String clearOn) {
		if (ConfigHandler.getConfig("config.yml").getString("Clear-Items") != null && ConfigHandler.getConfig("config.yml").getString("Clear-Items").equalsIgnoreCase("All")) {
			if (ConfigHandler.getConfig("config.yml").getString(clearOn) != null && inClearingWorld(world, clearOn) 
					|| ConfigHandler.getConfig("config.yml").getString(clearOn) != null && ConfigHandler.getConfig("config.yml").getBoolean(clearOn) == true) {
				if (ConfigHandler.getConfig("config.yml").getString("AllowOPBypass") != null && ConfigHandler.getConfig("config.yml").getBoolean("AllowOPBypass") == true && player.isOp()) {} else {
					setClearAllItems(player);
				}
			}
		} else if (ConfigHandler.getConfig("config.yml").getString("Clear-Items") != null && ConfigHandler.getConfig("config.yml").getString("Clear-Items").equalsIgnoreCase("ItemJoin")) {
			if (ConfigHandler.getConfig("config.yml").getString(clearOn) != null && inClearingWorld(world, clearOn) 
					|| ConfigHandler.getConfig("config.yml").getString(clearOn) != null && ConfigHandler.getConfig("config.yml").getBoolean(clearOn) == true) {
				if (ConfigHandler.getConfig("config.yml").getString("AllowOPBypass") != null && ConfigHandler.getConfig("config.yml").getBoolean("AllowOPBypass") == true && player.isOp()) {} else {
					setClearItemJoinItems(player);
				}
			}
		} else if (ConfigHandler.getConfig("config.yml").getBoolean(clearOn) == true || inClearingWorld(world, clearOn)) {
			ServerHandler.sendErrorMessage("&c" + ConfigHandler.getConfig("config.yml").getString("Clear-Items") + " for Clear-Items in the config.yml is not a valid option.");
		}
	}
	
	public static void setClearAllItems(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);
		if (ServerHandler.hasCombatUpdate()) { player.getInventory().setItemInOffHand(null); }
	}
	
	public static void setClearItemJoinItems(Player player) {
		PlayerInventory inventory = player.getInventory();
		if (ConfigHandler.getConfig("config.yml").getBoolean("Protect-SpecialItems") == true) {
			for (ItemMap item: ItemUtilities.getItems()) {
				if (!item.isOnlyFirstJoin() && !item.isOnlyFirstWorld()) {
					if (inventory.getHelmet() != null && item.isSimilar(inventory.getHelmet()) && ItemHandler.containsNBTData(inventory.getHelmet())) {
						inventory.setHelmet(null);
					}
					if (inventory.getChestplate() != null && item.isSimilar(inventory.getChestplate()) && ItemHandler.containsNBTData(inventory.getChestplate())) {
						inventory.setChestplate(null);
					}
					if (inventory.getLeggings() != null && item.isSimilar(inventory.getLeggings()) && ItemHandler.containsNBTData(inventory.getLeggings())) {
						inventory.setLeggings(null);
					}
					if (inventory.getBoots() != null && item.isSimilar(inventory.getBoots()) && ItemHandler.containsNBTData(inventory.getBoots())) {
						inventory.setBoots(null);
					}
					if (ServerHandler.hasCombatUpdate() && inventory.getItemInOffHand() != null && item.isSimilar(inventory.getItemInOffHand()) && ItemHandler.containsNBTData(inventory.getItemInOffHand())) {
						inventory.setItemInOffHand(null);
					}
					HashMap < String, ItemStack[] > inventoryContents = new HashMap < String, ItemStack[] > ();
					inventoryContents.put(PlayerHandler.getPlayerID(player), inventory.getContents());
					for (ItemStack contents: inventoryContents.get(PlayerHandler.getPlayerID(player))) {
						if (contents != null && item.isSimilar(contents) && ItemHandler.containsNBTData(contents)) {
							inventory.remove(contents);
						}
					}
					inventoryContents.clear();
				}
			}
		} else {
			if (inventory.getHelmet() != null && ItemHandler.containsNBTData(inventory.getHelmet())) {
				inventory.setHelmet(null);
			}
			if (inventory.getChestplate() != null && ItemHandler.containsNBTData(inventory.getChestplate())) {
				inventory.setChestplate(null);
			}
			if (inventory.getLeggings() != null && ItemHandler.containsNBTData(inventory.getLeggings())) {
				inventory.setLeggings(null);
			}
			if (inventory.getBoots() != null && ItemHandler.containsNBTData(inventory.getBoots())) {
				inventory.setBoots(null);
			}
			if (ServerHandler.hasCombatUpdate() && inventory.getItemInOffHand() != null && ItemHandler.containsNBTData(inventory.getItemInOffHand())) {
				inventory.setItemInOffHand(null);
			}
			HashMap < String, ItemStack[] > inventoryContents = new HashMap < String, ItemStack[] > ();
			inventoryContents.put(PlayerHandler.getPlayerID(player), inventory.getContents());
			for (ItemStack contents: inventoryContents.get(PlayerHandler.getPlayerID(player))) {
				if (contents != null && ItemHandler.containsNBTData(contents)) {
					inventory.remove(contents);
				}
			}
			inventoryContents.clear();
		}
	}
	
	public static void sendFailCount(Player player, int session) {
		if (getFailCount().get(session) != null && getFailCount().get(session) != 0) {
			if (ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null && isOverwriteWorld(player.getWorld().getName()) 
					|| ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null && ConfigHandler.getConfig("items.yml").getBoolean("items-Overwrite") == true) {
				String[] placeHolders = new String[12]; placeHolders[7] = getFailCount().get(session).toString();
				Language.sendLangMessage("General.failedInventory", player, placeHolders);
			} else {
				String[] placeHolders = new String[12]; placeHolders[7] = getFailCount().get(session).toString();
				Language.sendLangMessage("General.failedOverwrite", player, placeHolders);
			}
			removeFailCount(session);
		}
	}
	
	public static HashMap < Integer, Integer > getFailCount() {
		return failCount;
	}
	
	public static void putFailCount(int session, int i) {
		failCount.put(session, i);
	}
	
	public static void removeFailCount(int session) {
		failCount.remove(session);
	}
	
	public static Boolean isObtainable(Player player, ItemMap itemMap, int session) {
		if (itemMap.getProbability().equals(-1) || !itemMap.getProbability().equals(-1) && probability.containsKey(itemMap.getConfigName()) && !hasProbabilityItem(player, itemMap)) {
			if (!itemMap.hasItem(player) || itemMap.isAlwaysGive() || !itemMap.isLimitMode(player.getGameMode())) {
				boolean firstJoin = DataStorage.getSQLData().hasFirstJoined(player, itemMap);
				boolean firstWorld = DataStorage.getSQLData().hasFirstWorld(player, itemMap);
				boolean ipLimited = DataStorage.getSQLData().isIPLimited(player, itemMap);
				if (itemMap.isLimitMode(player.getGameMode())) {
					if (Utils.isInt(itemMap.getSlot()) && Integer.parseInt(itemMap.getSlot()) >= 0 && Integer.parseInt(itemMap.getSlot()) <= 35) {
						if (!firstJoin && !firstWorld && !ipLimited && canOverwrite(player, itemMap.getSlot(), itemMap.getConfigName())) {
							return true;
						}
					} else if (ItemHandler.isCustomSlot(itemMap.getSlot())) {
						if (!firstJoin && !firstWorld && !ipLimited && canOverwrite(player, itemMap.getSlot(), itemMap.getConfigName())) {
							return true;
						}
					}
					if (!firstJoin && !firstWorld && !ipLimited) {
						if (session != 0 && getFailCount().get(session) != null) {
						putFailCount(session, getFailCount().get(session) + 1);
						} else if (session != 0) { putFailCount(session, 1); }
						ServerHandler.sendDebugMessage(player.getName() + " has failed to receive item; " + itemMap.getConfigName());
					} else {
						if (firstJoin) {
							ServerHandler.sendDebugMessage(player.getName() + " has already received first-join " + itemMap.getConfigName() + ", they can no longer recieve this.");
						} else if (firstWorld) {
							ServerHandler.sendDebugMessage(player.getName() + " has already received first-world " + itemMap.getConfigName() + ", they can no longer recieve this in " + player.getWorld().getName());
						} else if (ipLimited) {
							ServerHandler.sendDebugMessage(player.getName() + " has already received ip-limited " + itemMap.getConfigName() + ", they will only recieve this on their dedicated ip."); 
						}
					}
					return false;
				} else { return false; }
			}
		} else { return false; }
		ServerHandler.sendDebugMessage(player.getName() + " already has item; " + itemMap.getConfigName());
		return false;
	}
	
	public static Boolean isOverwriteWorld(String world) {
		if (ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null) {
			String worldlist = ConfigHandler.getConfig("items.yml").getString("items-Overwrite").replace(" ", "");
			String[] compareWorlds = worldlist.split(",");
			for (String compareWorld: compareWorlds) {
				if (compareWorld.equalsIgnoreCase(world) || compareWorld.equalsIgnoreCase("all") || compareWorld.equalsIgnoreCase("global")) {
					return true;
				}
			}
		} else if (ConfigHandler.getConfig("items.yml").getString("items-Overwrite") == null) {
			return true;
		}
		return false;
	}
	
	public static Boolean isOverwrite(Player player) {
		if (ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null && isOverwriteWorld(player.getWorld().getName()) 
				|| ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null && ConfigHandler.getConfig("items.yml").getBoolean("items-Overwrite") == true) {
			return true;
		}
		return false;
	}
	
	public static Boolean canOverwrite(Player player, String slot, String item) {
		try {
			if (!isOverwrite(player) && Utils.isInt(slot) && player.getInventory().getItem(Integer.parseInt(slot)) != null) {
				return false;
			} else if (!isOverwrite(player) && ItemHandler.isCustomSlot(slot)) {
				if (slot.equalsIgnoreCase("Arbitrary") && player.getInventory().firstEmpty() == -1) {
					return true;
				} else if (slot.equalsIgnoreCase("Helmet") && player.getInventory().getHelmet() != null) {
					return false;
				} else if (slot.equalsIgnoreCase("Chestplate") && player.getInventory().getChestplate() != null) {
					return false;
				} else if (slot.equalsIgnoreCase("Leggings") && player.getInventory().getLeggings() != null) {
					return false;
				} else if (slot.equalsIgnoreCase("Boots") && player.getInventory().getBoots() != null) {
					return false;
				} else if (ServerHandler.hasCombatUpdate() && slot.equalsIgnoreCase("Offhand")) {
					if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
						return false;
					}
				} else if (getSlotConversion(slot) != 5 
						&& player.getOpenInventory().getTopInventory().getItem(getSlotConversion(slot)) != null 
						&& player.getOpenInventory().getTopInventory().getItem(getSlotConversion(slot)).getType() != Material.AIR) {
					return false;
				}
			}
		} catch (Exception e) { ServerHandler.sendDebugTrace(e); }
		return true;
	}
	
	public static void setInvSlots(Player player, ItemMap itemMap, boolean noTriggers, ItemStack item, int amount) {
		if (amount != 0 || itemMap.isAlwaysGive()) {
			if (noTriggers) { item.setAmount(amount); }
			if (itemMap.hasItem(player)) {
				player.getInventory().addItem(item);
			} else {
				player.getInventory().setItem(Integer.parseInt(itemMap.getSlot()), item);
			}
		} else { player.getInventory().setItem(Integer.parseInt(itemMap.getSlot()), item); }
		saveSQLItemData(player, itemMap);
		ServerHandler.sendDebugMessage("Given the Item; " + itemMap.getConfigName());
	}
	
	public static void setCustomSlots(Player player, ItemMap itemMap, boolean noTriggers, ItemStack item, int amount) {
		EntityEquipment Equip = player.getEquipment();
			if (itemMap.getSlot().equalsIgnoreCase("Arbitrary")) {
				if (amount != 0 && noTriggers) { item.setAmount(amount); }
				player.getInventory().addItem(item);
				ServerHandler.sendDebugMessage("Given the Item; [" + itemMap.getConfigName() + "]");
				saveSQLItemData(player, itemMap);
			} else if (itemMap.getSlot().equalsIgnoreCase("Helmet")) {
				if (amount != 0 || itemMap.isAlwaysGive()) {
					if (noTriggers) { item.setAmount(amount); }
					if (itemMap.hasItem(player)) { player.getInventory().addItem(item);
					} else { Equip.setHelmet(item); }
				} else { Equip.setHelmet(item); }
				ServerHandler.sendDebugMessage("Given the Item; [" + itemMap.getConfigName() + "]");
				saveSQLItemData(player, itemMap);
			} else if (itemMap.getSlot().equalsIgnoreCase("Chestplate")) {
				if (amount != 0 || itemMap.isAlwaysGive()) {
					if (noTriggers) { item.setAmount(amount); }
					if (itemMap.hasItem(player)) { player.getInventory().addItem(item);
					} else { Equip.setChestplate(item); }
				} else { Equip.setChestplate(item); }
				ServerHandler.sendDebugMessage("Given the Item; [" + itemMap.getConfigName() + "]");
				saveSQLItemData(player, itemMap);
			} else if (itemMap.getSlot().equalsIgnoreCase("Leggings")) {
				if (amount != 0 || itemMap.isAlwaysGive()) {
					if (noTriggers) { item.setAmount(amount); }
					if (itemMap.hasItem(player)) { player.getInventory().addItem(item);
					} else { Equip.setLeggings(item); }
				} else { Equip.setLeggings(item); }
				ServerHandler.sendDebugMessage("Given the Item; [" + itemMap.getConfigName() + "]");
				saveSQLItemData(player, itemMap);
			} else if (itemMap.getSlot().equalsIgnoreCase("Boots")) {
				if (amount != 0 || itemMap.isAlwaysGive()) {
					if (noTriggers) { item.setAmount(amount); }
					if (itemMap.hasItem(player)) { player.getInventory().addItem(item);
					} else { Equip.setBoots(item); }
				} else { Equip.setBoots(item); }
				ServerHandler.sendDebugMessage("Given the Item; [" + itemMap.getConfigName() + "]");
				saveSQLItemData(player, itemMap);
			} else if (ServerHandler.hasCombatUpdate() && itemMap.getSlot().equalsIgnoreCase("Offhand")) {
				if (amount != 0 || itemMap.isAlwaysGive()) {
					if (noTriggers) { item.setAmount(amount); }
					if (itemMap.hasItem(player)) { player.getInventory().addItem(item);
					} else { PlayerHandler.setOffhandItem(player, item); }
				} else { PlayerHandler.setOffhandItem(player, item); }
				ServerHandler.sendDebugMessage("Given the Item; [" + itemMap.getConfigName() + "]");
				saveSQLItemData(player, itemMap);
			} else if (getSlotConversion(itemMap.getSlot()) != 5) {
				if (amount != 0 || itemMap.isAlwaysGive()) {
					if (noTriggers) { item.setAmount(amount); }
					if (itemMap.hasItem(player)) { player.getInventory().addItem(item);
					} else { player.getOpenInventory().getTopInventory ().setItem(getSlotConversion(itemMap.getSlot()), item); }
				} else { player.getOpenInventory().getTopInventory ().setItem(getSlotConversion(itemMap.getSlot()), item); }
				ServerHandler.sendDebugMessage("Given the Item; [" + itemMap.getConfigName() + "]");
				saveSQLItemData(player, itemMap);
			}
	}
	
	public static int getSlotConversion(String str) {
		if (str.equalsIgnoreCase("CRAFTING[0]") || str.equalsIgnoreCase("C[0]") || str.equalsIgnoreCase("C(0)")) {
			return 0;
		} else if (str.equalsIgnoreCase("CRAFTING[1]") || str.equalsIgnoreCase("C[1]") || str.equalsIgnoreCase("C(1)")) {
			return 1;
		} else if (str.equalsIgnoreCase("CRAFTING[2]") || str.equalsIgnoreCase("C[2]") || str.equalsIgnoreCase("C(2)")) {
			return 2;
		} else if (str.equalsIgnoreCase("CRAFTING[3]") || str.equalsIgnoreCase("C[3]") || str.equalsIgnoreCase("C(3)")) {
			return 3;
		} else if (str.equalsIgnoreCase("CRAFTING[4]") || str.equalsIgnoreCase("C[4]") || str.equalsIgnoreCase("C(4)")) {
			return 4;
		}
		return 5;
	}
	
	private static void saveSQLItemData(Player player, ItemMap itemMap) {
		DataStorage.getSQLData().saveFirstJoinData(player, itemMap);
		DataStorage.getSQLData().saveFirstWorldData(player, itemMap);
		DataStorage.getSQLData().saveIpLimitData(player, itemMap);
	}
}