
package com.avrgaming.civcraft.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.exception.CivException;

public class XrayCheckCommand
        extends CommandBase {
    @Override
    public void init() {
        this.command = "/xraycheck";
        this.displayName = CivSettings.localize.localizedString("adcmd_find_xrayers");
        this.sendUnknownToDefault = true;
    }

    @Override
    public void doDefaultAction() throws CivException {
        StringBuilder out = new StringBuilder();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String loc = this.onCheck((int) player.getLocation().getY(), player);
            if ("".equals(loc)) continue;
            out.append(loc);
        }
        CivMessage.sendHeading(this.sender, "Potential XRAYers");
        if (!"".equals(out.toString())) {
            CivMessage.send((Object) this.sender, out.toString());
        } else {
            CivMessage.send((Object) this.sender, "No Players Found");
        }
    }

    public String onCheck(int g, Player player) {
        if (g <= 33 && g > 20) {
            return CivColor.Green + player.getName() + " y = " + g + "\n";
        }
        if (g <= 20 && g > 12) {
            return CivColor.Gold + player.getName() + " y = " + g + "\n";
        }
        if (g <= 12 && g >= 1) {
            return CivColor.Red + player.getName() + " y =  " + g + "\n";
        }
        return "";
    }

    @Override
    public void showHelp() {
    }

    @Override
    public void permissionCheck() throws CivException {
        if (!this.sender.isOp()) {
            throw new CivException(CivSettings.localize.localizedString("adcmd_NotAdmin"));
        }
    }
}

