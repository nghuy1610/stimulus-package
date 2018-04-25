package com.queuedpixel.stimuluspackage;

import java.util.TreeSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WealthTopCommand implements CommandExecutor
{
    private final StimulusPackagePlugin plugin;
    
    public WealthTopCommand( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        String prefix = "§a[§2Wealth§a] ";
        boolean allPlayers = false;
        int pageNum = 1;
        for ( String arg : args )
        {
            if ( arg.toLowerCase().equals( "all" ))
            {
                allPlayers = true;
            }
            else
            {
                try
                {
                    pageNum = Integer.parseInt( arg );
                }
                catch ( NumberFormatException e )
                {
                    sender.sendMessage( prefix + "§3Invalid page number." );
                    return false;
                }
            }
        }

        if ( pageNum < 1 )
        {
            sender.sendMessage( prefix + "§3Page number must be greater than zero." );
            return false;
        }

        String playerType = allPlayers ? "All" : "Active";
        sender.sendMessage( prefix + "§fDisplaying " + playerType + " Players - Page " + pageNum + ":" );

        TreeSet< SortedLine< Double >> wealthSet =
                allPlayers ? this.plugin.getAllWealthTop() : this.plugin.getActiveWealthTop();
        int size = wealthSet.size();
        int length = Integer.toString( size ).length();

        if ( size - (( pageNum - 1 ) * 10 ) <= 0 )
        {
            sender.sendMessage( prefix + "§3No players to display." );
            return true;
        }

        int index = 0;
        for ( SortedLine< Double > line : wealthSet.descendingSet() )
        {
            index++;

            // skip players until we reach the desired page
            if ( index <= ( pageNum - 1 ) * 10 ) continue;

            // stop output after we display 10 players
            if ( index > pageNum * 10 ) break;

            String indexString = String.format( "%0" + length + "d", index );
            String playerName = line.line;
            String wealth = this.plugin.getEconomy().format( line.sortValue );
            sender.sendMessage( prefix + "§e[§6" + indexString + "§e] §3" + playerName + " §f- §d" + wealth );
        }

        return true;
    }
}