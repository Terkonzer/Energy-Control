package com.zuxelus.energycontrol;

import com.zuxelus.energycontrol.network.ChannelHandler;
import com.zuxelus.energycontrol.network.PacketAlarm;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class ServerTickHandler {
	public final static ServerTickHandler instance = new ServerTickHandler();

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		EnergyControl.instance.screenManager.clearWorld(event.getWorld());
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		ChannelHandler.network.sendTo(
				new PacketAlarm(EnergyControl.config.maxAlarmRange, EnergyControl.config.allowedAlarms), (EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (EnergyControl.oreHelper == null)
			return;
		ItemStack stack = event.getItemStack();
		if (stack.isEmpty())
			return;
		OreHelper ore = EnergyControl.oreHelper.get(OreHelper.getId(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage()));
		if (ore != null)
			event.getToolTip().add(1, ore.getDescription());
	}
}
