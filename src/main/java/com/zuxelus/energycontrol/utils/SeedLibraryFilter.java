package com.zuxelus.energycontrol.utils;

import java.util.Collection;
import java.util.Vector;

import com.zuxelus.energycontrol.tileentities.TileEntitySeedLibrary;

import cpw.mods.fml.common.FMLCommonHandler;
import ic2.api.crops.Crops;
import ic2.api.item.IC2Items;
import ic2.core.item.ItemCropSeed;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SeedLibraryFilter {
	public TileEntitySeedLibrary te = null;
	public boolean bulk_mode = false;
	public int unknown_type = 1;
	public int unknown_ggr = 1;
	public int seed_type = -1;
	public int min_growth = 0;
	public int min_gain = 0;
	public int min_resistance = 0;
	public int max_growth = 31;
	public int max_gain = 31;
	public int max_resistance = 31;
	public int min_total = 0;
	public int max_total = 93;
	public SeedLibrarySort sort = SeedLibrarySort.TOTAL_DESC;

	public static final int CACHE_SIZE = 10;
	public Vector<ItemStack> cache = new Vector<ItemStack>(CACHE_SIZE + 1);
	public boolean cached_nothing = false;

	public SeedLibraryFilter(TileEntitySeedLibrary owner) {
		te = owner;
	}

	public void copyFrom(SeedLibraryFilter source) {
		unknown_type = source.unknown_type;
		unknown_ggr = source.unknown_ggr;
		seed_type = source.seed_type;
		min_growth = source.min_growth;
		min_gain = source.min_gain;
		min_resistance = source.min_resistance;
		max_growth = source.max_growth;
		max_gain = source.max_gain;
		max_resistance = source.max_resistance;
		min_total = source.min_total;
		max_total = source.max_total;
		sort = source.sort;

		settingsChanged();
	}

	public ItemStack getSeed(Collection<ItemStack> seeds) {
		if (cached_nothing)
			return null;

		if (cache.size() == 0)
			fillCache(seeds);

		if (cache.size() == 0) {
			cached_nothing = true;
			return null;
		}

		return cache.get(0);
	}

	public int getCount(Collection<ItemStack> seeds) {
		int count = 0;
		for (ItemStack seed : seeds)
			if (isMatch(seed))
				count += seed.stackSize;

		return count;
	}

	public void newSeed(ItemStack seed) {
		if (isMatch(seed)) {
			addToCache(seed);
			updateSeedCount();
		}
	}

	public void lostSeed(ItemStack seed) {
		if (isMatch(seed)) {
			removeFromCache(seed);
			updateSeedCount();
		}
	}

	public void settingsChanged() {
		cache.clear();
		cached_nothing = false;

		if (te != null && !FMLCommonHandler.instance().getEffectiveSide().isClient()) { // world is empty
			updateSeedCount();
			te.updateGUIFilter();
		}
	}

	public boolean isMatch(ItemStack seed) {
		int id = Crops.instance.getCropCard(seed).getId();
		byte scan = ItemCropSeed.getScannedFromStack(seed);

		if (scan == 0)
			return (unknown_type > 0) && (unknown_ggr > 0);
		if (unknown_type == 2)
			return false;
		if (seed_type != -1 && seed_type != id)
			return false;
		if (scan < 4)
			return (unknown_ggr > 0);
		if (unknown_ggr == 2)
			return false;

		byte growth = ItemCropSeed.getGrowthFromStack(seed);
		byte gain = ItemCropSeed.getGainFromStack(seed);
		byte resistance = ItemCropSeed.getResistanceFromStack(seed);

		if (growth < min_growth || growth > max_growth || gain < min_gain || gain > max_gain || resistance < min_resistance || resistance > max_resistance)
			return false;
		int total = growth + gain + resistance;
		if (total < min_total || total > max_total)
			return false;

		return true;
	}

	public String getCropName() {
		int crop_id = seed_type;
		if (crop_id == -1)
			return "Any";
		if (Crops.instance.getCropList()[crop_id] == null)
			return "(Invalid)";
		return Crops.instance.getCropList()[crop_id].name();
	}

	public void setCropFromSeed(ItemStack seed) {
		if (seed == null) {
			seed_type = -1;
		} else if (seed.getItem() != IC2Items.getItem("cropSeed").getItem()) {
			seed_type = -1;
		} else if (ItemCropSeed.getScannedFromStack(seed) == 0) {
			seed_type = -1;
		} else {
			seed_type = Crops.instance.getCropCard(seed).getId();
		}
		settingsChanged();
	}

	protected void fillCache(Collection<ItemStack> seeds) {
		cache.clear();

		bulk_mode = true;
		for (ItemStack seed : seeds)
			newSeed(seed);
		bulk_mode = false;

		updateSeedCount();
	}

	protected void updateSeedCount() {
		if (!bulk_mode && te != null)
			te.updateSeedCount();
	}

	protected void addToCache(ItemStack seed) {
		cached_nothing = false;

		int pos = 0;
		for (int i = cache.size() - 1; i >= 0; i--) {
			int cmp = sort.compare(seed, cache.get(i));
			if (cmp <= 0) {
				pos = i + 1;
				break;
			}
		}

		if (pos >= CACHE_SIZE)
			return;

		cache.add(pos, seed);

		while (cache.size() > CACHE_SIZE)
			cache.remove(cache.size() - 1);

		return;
	}

	protected void removeFromCache(ItemStack seed) {
		cache.remove(seed);
	}

	// Save/load
	public void loadFromNBT(NBTTagCompound input) {
		if (input.hasKey("allow_unknown_type")) {
			// Upgrade path for pre-3.0.
			unknown_type = input.getByte("allow_unknown_type");
			unknown_ggr = input.getByte("allow_unknown_ggr");
		} else {
			unknown_type = input.getByte("unknown_type");
			unknown_ggr = input.getByte("unknown_ggr");
		}
		seed_type = input.getInteger("seed_type");
		min_growth = input.getInteger("min_growth");
		min_gain = input.getInteger("min_gain");
		min_resistance = input.getInteger("min_resistance");
		max_growth = input.getInteger("max_growth");
		max_gain = input.getInteger("max_gain");
		max_resistance = input.getInteger("max_resistance");
		min_total = input.getInteger("min_total");
		max_total = input.getInteger("max_total");

		int sort_type = input.getInteger("sort_type");
		boolean sort_desc = input.getBoolean("sort_desc");
		sort = SeedLibrarySort.getSort(sort_type, sort_desc);

		settingsChanged();
	}

	public void writeToNBT(NBTTagCompound output) {
		output.setByte("unknown_type", (byte) unknown_type);
		output.setByte("unknown_ggr", (byte) unknown_ggr);
		output.setInteger("seed_type", seed_type);
		output.setInteger("min_growth", min_growth);
		output.setInteger("min_gain", min_gain);
		output.setInteger("min_resistance", min_resistance);
		output.setInteger("max_growth", max_growth);
		output.setInteger("max_gain", max_gain);
		output.setInteger("max_resistance", max_resistance);
		output.setInteger("min_total", min_total);
		output.setInteger("max_total", max_total);

		output.setInteger("sort_type", sort.sort_type);
		output.setBoolean("sort_desc", sort.descending);
	}
}