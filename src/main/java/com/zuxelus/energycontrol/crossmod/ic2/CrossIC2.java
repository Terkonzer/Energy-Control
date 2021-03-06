package com.zuxelus.energycontrol.crossmod.ic2;

import java.util.List;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;

import ic2.api.reactor.IReactor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.Loader;

public abstract class CrossIC2 {
	public enum IC2Type{
		SPEIGER,
		EXP,
		NONE
	}

	public abstract boolean isWrench(ItemStack stack);

	public abstract int getNuclearCellTimeLeft(ItemStack stack);

	public abstract boolean isSteamReactor(TileEntity te);

	public abstract IC2Type getType();
	
	public abstract int getProfile();

	public abstract ItemStack getItemStack(String name);

	public abstract Item getItem(String name);

	public abstract ItemStack getChargedStack(ItemStack stack);

	public static CrossIC2 getMod() {
		try {
			if (Loader.isModLoaded("ic2-classic-spmod")) {
				Class clz = Class.forName("com.zuxelus.energycontrol.crossmod.ic2.IC2Classic");
				if (clz != null)
					return (CrossIC2) clz.newInstance();
			}
			if (Loader.isModLoaded("ic2")) {
				Class clz = Class.forName("com.zuxelus.energycontrol.crossmod.ic2.IC2Exp");
				if (clz != null)
					return (CrossIC2) clz.newInstance();
			}
		} catch (Exception e) {

		}
		return new IC2NoMod();
	}

	public abstract ItemStack getEnergyCard(World world, BlockPos pos);

	public abstract NBTTagCompound getEnergyData(TileEntity te);

	public abstract ItemStack getGeneratorCard(World world, BlockPos pos);

	public abstract NBTTagCompound getGeneratorData(TileEntity te);

	public abstract NBTTagCompound getGeneratorKineticData(TileEntity te);

	public abstract NBTTagCompound getGeneratorHeatData(TileEntity te);

	public abstract List<IFluidTank> getAllTanks(TileEntity te);

	public abstract ItemStack getReactorCard(World world, BlockPos pos);

	public abstract ItemStack getLiquidAdvancedCard(World world, BlockPos pos);

	public abstract CardState updateCardReactor(World world, ICardReader reader, IReactor reactor);

	public abstract CardState updateCardReactor5x5(World world, ICardReader reader, BlockPos target);

	public abstract void loadOreInfo();

	public abstract void showBarrelInfo(EntityPlayer player, TileEntity te);
}
