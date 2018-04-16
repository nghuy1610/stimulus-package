/*

stimulus-package : Give money to players based on economic activity.

Copyright (c) 2018 Queued Pixel <git@queuedpixel.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

package com.queuedpixel.stimuluspackage;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StimulusCommand implements CommandExecutor
{
    private final StimulusPackagePlugin plugin;
    private final StimulusPackageConfiguration config;

    public StimulusCommand( StimulusPackagePlugin plugin )
    {
        this.plugin = plugin;
        this.config = this.plugin.getConfiguration();
    }

    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        // current time
        long now = new Date().getTime();

        // map of players to the number of seconds since they were last on the server
        Map< UUID, Long > playerMap = new HashMap< UUID, Long >();

        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for ( OfflinePlayer player : offlinePlayers )
        {
            // store number of seconds since player was last on
            playerMap.put( player.getUniqueId(), ( now - player.getLastPlayed() ) / 1000 );
        }

        Collection< ? extends Player > onlinePlayers = Bukkit.getOnlinePlayers();
        for ( Player player : onlinePlayers )
        {
            // player is on right now, so zero seconds since they were last on the server
            playerMap.put( player.getUniqueId(), 0l );
        }

        int activeEconomicPlayers = 0;
        int activeStimulusPlayers = 0;

        for ( Long loginInterval : playerMap.values() )
        {
            if ( loginInterval < this.config.getEconomicInterval() ) activeEconomicPlayers++;
            if ( loginInterval < this.config.getStimulusInterval() ) activeStimulusPlayers++;
        }

        // perform volume calculations
        double actualVolume = this.plugin.getActualVolume( now );
        double totalDesiredVolume = this.config.getDesiredVolume() * activeEconomicPlayers;
        double volumeDelta = totalDesiredVolume - actualVolume;

        sender.sendMessage( "Economic Players: " + activeEconomicPlayers +
                            ", Stimulus Players: " + activeStimulusPlayers );
        sender.sendMessage( "Desired Volume: " + String.format( "%.2f", totalDesiredVolume ) +
                            ", Actual Volume: " + String.format( "%.2f", actualVolume ) +
                            ", Delta: " + String.format( "%.2f", volumeDelta ));

        if ( volumeDelta <= 0 ) return true;

        // compute total stimulus
        double stimulusFactor = volumeDelta / totalDesiredVolume;
        double totalStimulus = stimulusFactor * this.config.getDesiredStimulus() * activeStimulusPlayers;
        sender.sendMessage( "Stimulus Factor: " + String.format( "%.2f", stimulusFactor ) +
                            ", Total Stimulus: " + String.format( "%.2f", totalStimulus ));

        return true;
    }
}
