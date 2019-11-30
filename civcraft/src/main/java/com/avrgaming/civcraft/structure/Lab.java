
package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import com.avrgaming.civcraft.components.AttributeBiomeRadiusPerLevel;
import com.avrgaming.civcraft.components.ConsumeLevelComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigLabLevel;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.MultiInventory;

public class Lab
        extends Structure {
    private ConsumeLevelComponent consumeComp = null;

    protected Lab(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public Lab(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
    }

    public String getkey() {
        return this.getTown().getName() + "_" + this.getConfigId() + "_" + this.getCorner().toString();
    }

    @Override
    public String getDynmapDescription() {
        return null;
    }

    @Override
    public String getMarkerIconName() {
        return "warning";
    }

    public ConsumeLevelComponent getConsumeComponent() {
        if (this.consumeComp == null) {
            this.consumeComp = (ConsumeLevelComponent) this.getComponent(ConsumeLevelComponent.class.getSimpleName());
        }
        return this.consumeComp;
    }

    public ConsumeLevelComponent.Result consume(CivAsyncTask task) throws InterruptedException {
        if (this.getChests().size() == 0) {
            return ConsumeLevelComponent.Result.STAGNATE;
        }
        MultiInventory multiInv = new MultiInventory();
        ArrayList<StructureChest> chests = this.getAllChestsById(0);
        for (StructureChest c : chests) {
            Inventory tmp;
            task.syncLoadChunk(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getZ());
            try {
                tmp = task.getChestInventory(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getY(), c.getCoord().getZ(), true);
            } catch (CivTaskAbortException e) {
                return ConsumeLevelComponent.Result.STAGNATE;
            }
            multiInv.addInventory(tmp);
        }
        this.getConsumeComponent().setSource(multiInv);
        this.getConsumeComponent().setConsumeRate(1.0);
        ConsumeLevelComponent.Result result = this.getConsumeComponent().processConsumption();
        this.getConsumeComponent().onSave();
        return result;
    }

    public void process_lab(CivAsyncTask task) throws InterruptedException {
        ConsumeLevelComponent.Result result = this.consume(task);
        switch (result) {
            case STARVE: {
                CivMessage.sendTown(this.getTown(), CivColor.Red + CivSettings.localize.localizedString("var_lab_productionFell", this.getConsumeComponent().getLevel(), new StringBuilder().append(CivColor.Green).append(this.getConsumeComponent().getCountString()).toString()));
                break;
            }
            case LEVELDOWN: {
                CivMessage.sendTown(this.getTown(), CivColor.Red + CivSettings.localize.localizedString("var_lab_lostalvl", this.getConsumeComponent().getLevel()));
                break;
            }
            case STAGNATE: {
                CivMessage.sendTown(this.getTown(), CivColor.Red + CivSettings.localize.localizedString("var_lab_stagnated", this.getConsumeComponent().getLevel(), new StringBuilder().append(CivColor.Green).append(this.getConsumeComponent().getCountString()).toString()));
                break;
            }
            case GROW: {
                CivMessage.sendTown(this.getTown(), CivColor.Green + CivSettings.localize.localizedString("var_lab_productionGrew", this.getConsumeComponent().getLevel(), this.getConsumeComponent().getCountString()));
                break;
            }
            case LEVELUP: {
                CivMessage.sendTown(this.getTown(), CivColor.Green + CivSettings.localize.localizedString("var_lab_lvlUp", this.getConsumeComponent().getLevel()));
                break;
            }
            case MAXED: {
                CivMessage.sendTown(this.getTown(), CivColor.Green + CivSettings.localize.localizedString("var_lab_maxed", this.getConsumeComponent().getLevel(), new StringBuilder().append(CivColor.Green).append(this.getConsumeComponent().getCountString()).toString()));
                break;
            }
            default:
                CivMessage.sendTown(this.getTown(), CivColor.Red + CivSettings.localize.localizedString("var_lab_productionFell", this.getConsumeComponent().getLevel(), new StringBuilder().append(CivColor.Green).append(this.getConsumeComponent().getCountString()).toString()));
                break;
        }
    }

    public int getLevel() {
        return this.getConsumeComponent().getLevel();
    }

    public double getBeakersPerTile() {
        AttributeBiomeRadiusPerLevel attrBiome = (AttributeBiomeRadiusPerLevel) this.getComponent("AttributeBiomeRadiusPerLevel");
        double base = attrBiome.getBaseValue();
        double rate = 1.0;
        if (this.getTown().getBuffManager().hasBuff("ADVANCED_TOOLING")) {
            rate = 1.5;
        }
        return rate * base;
    }

    public int getCount() {
        return this.getConsumeComponent().getCount();
    }

    public int getMaxCount() {
        int level = this.getLevel();
        ConfigLabLevel lvl = CivSettings.labLevels.get(level);
        return lvl.count;
    }

    public ConsumeLevelComponent.Result getLastResult() {
        return this.getConsumeComponent().getLastResult();
    }

}

