package org.hot;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class tabcom implements TabCompleter {
	
	List<String> argruments = new ArrayList<>();
	
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] arg){
		argruments.clear();
		argruments.add("setcoin");
		argruments.add("setcommand");
		argruments.add("sethours");
		argruments.add("setoperatorname");
		
		List<String> result = new ArrayList<>();
		
		if(arg.length == 1){
			for(String a : argruments){
				if(a.toLowerCase().startsWith(arg[0].toLowerCase())){
					result.add(a);
				}
			}
			return result;
		}
		if(arg.length >= 2){
			return result;
		}
		return argruments;
	}
}
