package org.hot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

@SuppressWarnings("deprecation")
public final class Hottime extends JavaPlugin {
	
	BigInteger coin = new BigInteger("0");
	List<UUID> alreadyconfirmed = new ArrayList<>();
	String commandformat = "코인 추가 %1$s %2$s";
	List<Integer> hours = new ArrayList<>();
	String operatorname = "console";
	
	@Override
	public void onEnable() {
		// Plugin startup logic
		saveConfig();
		File cfile = new File(getDataFolder(), "config.yml");
		if (cfile.length() == 0) {
			getConfig().options().copyDefaults(true);
			saveConfig();
		}
		Date now = new Date();
		if (now.getDate() == getConfig().getInt("saveday")) {
			List<String> list = getConfig().getStringList("todaysconfirmlist");
			for(String s : list) {
				alreadyconfirmed.add(UUID.fromString(s));
			}
		} else {
			getConfig().set("todaysconfirmlist", null);
			getConfig().set("saveday", new Date().getDate());
			saveConfig();
		}
		{
			String str = getConfig().getString("coin");
			if (str != null) {
				coin = new BigInteger(str);
			}
		}
		{
			String str = getConfig().getString("operatorname");
			if (str != null) {
				operatorname = str;
			}
		}
		{
			String str = getConfig().getString("commandformat");
			if (str != null) {
				commandformat = str;
			}
		}
		hours = getConfig().getIntegerList("hours");
		Bukkit.getPluginCommand("hottime").setTabCompleter(new tabcom());
		getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
			Date nowday = new Date();
			if (nowday.getDate() != getConfig().getInt("saveday")) {
				alreadyconfirmed.clear();
				getConfig().set("todaysconfirmlist", null);
				getConfig().set("saveday", new Date().getDate());
				saveConfig();
			}
			if (ithoursisnow()) {
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(!alreadyconfirmed.contains(p.getUniqueId())) {
						Bukkit.dispatchCommand(operatorgen(operatorname), new Formatter().format(commandformat, p.getName(), coin).toString());
						p.sendMessage(Component.text("[ ").color(NamedTextColor.WHITE).append(Component.text("핫타임").color(NamedTextColor.RED)).append(Component.text(" ] 핫타임 이벤트! " + coin + "코인이 입금되었습니다!")).color(NamedTextColor.WHITE));
						alreadyconfirmed.add(p.getUniqueId());
						List<String> list = new ArrayList<>();
						for(UUID uuid : alreadyconfirmed) {
							list.add(uuid.toString());
						}
						getConfig().set("todaysconfirmlist", list);
						saveConfig();
					}
				}
			}
		}, 1L, 1L);
		getLogger().info("Hottime Plugin Enabled");
	}
	
	@Override
	public void onDisable() {
		// Plugin shutdown logic
		getLogger().info("Hottime Plugin Disabled");
	}
	
	public CommandSender operatorgen(String name) {
		if(name.equalsIgnoreCase("console")) {
			return Bukkit.getConsoleSender();
		} else {
			return PlayerUtils.nonePlayerwithname(name);
		}
	}
	
	public static String plusargs(String[] args, Integer start) {
		String commandl = "";
		for (int range = start; range < args.length; range++) {
			commandl += args[range];
			if (!(args.length - 1 == range)) {
				commandl += " ";
			}
		}
		return commandl;
	}
	
	public boolean ithoursisnow() {
		Date date = new Date();
		for (int hour : hours) {
			if (hour == date.getHours()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(label.equalsIgnoreCase("hottime")) {
			if(args.length == 0) {
				sender.sendMessage("현재 설정된 코인은 " + coin + "코인입니다");
			} else if (args.length == 1) {
				if(args[0].equalsIgnoreCase("reset")) {
					coin = new BigInteger("100");
					getConfig().set("coin", null);
					commandformat = "코인 추가 %1$s %2$s";
					getConfig().set("commandformat", null);
					hours = new ArrayList<>();
					getConfig().set("hours", null);
					alreadyconfirmed = new ArrayList<>();
					getConfig().set("todaysconfirmlist", null);
					getConfig().set("saveday", new Date().getDate());
					saveConfig();
				}
			} else if (args.length == 2) {
				if(args[0].equalsIgnoreCase("setcoin")) {
					try {
						coin = new BigInteger(args[1]);
						getConfig().set("coin", coin);
						saveConfig();
					} catch (NumberFormatException e) {
						sender.sendMessage("숫자만 입력해주세요!");
					}
				}
				if(args[0].equalsIgnoreCase("setoperatorname")) {
					operatorname = args[1];
					getConfig().set("operatorname", operatorname);
					saveConfig();
				}
			}
			if (args.length >= 2) {
				if(args[0].equalsIgnoreCase("setcommand")) {
					commandformat = plusargs(args, 1);
					getConfig().set("commandformat", commandformat);
					saveConfig();
				}
				if(args[0].equalsIgnoreCase("sethours")) {
					List<Integer> temphours = hours;
					try {
						hours.clear();
						String input = plusargs(args, 1);
						while(input.contains(" ")) {
							input = input.replace(" ", "");
						}
						String[] split = input.split(",");
						for(String s : split) {
							String[] split2 = s.split("~");
							if(split2.length == 1) {
								int parsed = max(min(Integer.parseInt(split2[0]), 23), 0);
								if(!hours.contains(parsed)) {
									hours.add(parsed);
								}
							} else if (split2.length == 2) {
								int i = Integer.parseInt(split2[0]);
								int j = Integer.parseInt(split2[1]);
								if(i > j) {
									int temp = i;
									i = j;
									j = temp;
								}
								for(int k = max(i, 0); k <= min(j, 23); k++) {
									if(!hours.contains(k)) {
										hours.add(k);
									}
								}
							} else {
								sender.sendMessage("~ 사용은 객체당 하나만 해주세요!");
								hours = temphours;
								return true;
							}
						}
						getConfig().set("hours", hours);
						saveConfig();
					} catch (NumberFormatException e) {
						hours = temphours;
						sender.sendMessage("이 인자에는 \"~\", \",\", 공백 그리고 숫자만 입력할 수 있습니다!");
					}
				}
			}
		}
		return true;
	}
}
