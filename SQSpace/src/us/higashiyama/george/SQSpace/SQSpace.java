
package us.higashiyama.george.SQSpace;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SQSpace extends JavaPlugin implements Listener {

	public static ArrayList<Player> Players = new ArrayList<Player>();
	public static SQSpace instance;

	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {

		instance = this;
		String planet = Bukkit.getServerName();
		if ((planet.equals("Trinitos_Alpha")) || (planet.equals("Trinitos_Beta")) || (planet.equals("Trinitos_Gamma"))) {
			System.out.println("Events registered!");
			this.getServer().getPluginManager().registerEvents(this, this);
		}
	}

	// Adding headshot support
	@EventHandler
	public void arrowDamager(EntityDamageByEntityEvent e) {

		// Making sure the entity that did the damage was an arrow
		if ((e.getDamager() instanceof Arrow) && (e.getEntity() instanceof Player)) {
			final Entity arrow = e.getDamager();
			final Player p = (Player) e.getEntity();
			// Sneaking changes height of the player's head
			final double headHeight = (p.isSneaking()) ? 1.46 : 1.62;

			if (arrow.getLocation().getY() > (p.getLocation().getY() + headHeight)) {
				// Replacing their pumpkin with air
				if (p.getInventory().getHelmet().getType() == Material.PUMPKIN) {
					p.getInventory().setHelmet(new ItemStack(Material.AIR));
					// Refunding them the jack o lantern
					if (p.getInventory().firstEmpty() > -1) {
						p.getInventory().addItem(new ItemStack(Material.JACK_O_LANTERN, 1));
					} else {
						p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.JACK_O_LANTERN, 1));
					}
					((Player) ((Arrow) arrow).getShooter()).sendMessage(ChatColor.RED + "Headshot!");
				}
			}
		}
	}

	boolean checkIfSuffocating(Player p) {

		// WARNING: This check assumes you have already checked that the player
		// is in a "Space" area.
		// It is only a Respiration / Perm check
		boolean suffocating = false;
		if (!p.hasPermission("SQSpace.nosuffocate") && !(p.getGameMode().equals(GameMode.CREATIVE)) && (isInSpace(p)) && (!this.hasSpaceArmor(p))
				&& (!Players.contains(p))) {
			Players.add(p);
			p.sendMessage(ChatColor.AQUA + "[Space] " + ChatColor.RED + "You are now Suffocating!");
			new SuffocationTask(this, p);
			suffocating = true;
		}
		return suffocating;
	}

	public boolean hasSpaceArmor(Player p) {

		/*final ItemStack helmet = p.getInventory().getHelmet();
		if (helmet != null) {
			// Can breathe in space wearing a Pumpkin (Space Helmet)
			// Also can breathe in space if helmet has Respiration III
			if ((helmet.getType() == Material.PUMPKIN))
				return true;
		}*/
		ItemStack[] armor = p.getInventory().getArmorContents();
		if(armor == null) return false;
		for(ItemStack i : armor){
			if(i == null) return false;
			if(!isArmor(i.getType())){
				return false;
			}
		}
		return true;
	}
	
	private boolean isArmor(Material m){
		return m == Material.CHAINMAIL_HELMET ||  m == Material.CHAINMAIL_CHESTPLATE ||  m == Material.CHAINMAIL_LEGGINGS || m ==  Material.CHAINMAIL_BOOTS;
	}

	public static boolean isInSpace(Entity e) {

		final Location l = e.getLocation();
		boolean air1 = true;
		boolean air2 = true;
		final int x = l.getBlockX();
		final int y = l.getBlockY();
		final int z = l.getBlockZ();
		final World w = l.getWorld();
		int height = 40;
		for (int i = 0; i < height; i++) {
			final int id1 = w.getBlockTypeIdAt(x, y + i + 1, z);
			final int id2 = w.getBlockTypeIdAt(x, y - i, z);
			if (id1 != 0) {
				air1 = false;
			}
			if (id2 != 0) {
				air2 = false;
			}
		}
		if ((!air1) && (!air2))
			return false;
		return true;
	}

	@EventHandler
	public void onHelmetChange(final InventoryClickEvent event) {

		if (event.getSlotType().equals(SlotType.ARMOR) && (103 == event.getSlot())) {
			this.checkIfSuffocating((Player) event.getWhoClicked());
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {

		final Player p = event.getPlayer();
		final String planet = p.getLocation().getWorld().getName().toLowerCase();
		if (this.hasSpaceArmor(p)) {
			p.setRemainingAir(p.getMaximumAir());
		}
		if ((planet.startsWith("trinitos"))){
			
			if(p.getGameMode().equals(GameMode.SURVIVAL)){
				if ((isInSpace(p)) && (!p.isFlying()) && (p.getLocation().getY() < 256)) {
					p.setAllowFlight(true);
					p.setFlying(true);
					p.setFlySpeed(0.02F);
				} else if ((!isInSpace(p)) && (p.isFlying())) {
					p.setAllowFlight(false);
					p.setFlying(false);
					p.setFlySpeed(0.1F);
					p.setFallDistance(0.0F);
				}
				if(p.isFlying() && p.isSprinting()){
						p.setSprinting(false);
				}
			}
		
			this.checkIfSuffocating(p);
		}
	}
}
