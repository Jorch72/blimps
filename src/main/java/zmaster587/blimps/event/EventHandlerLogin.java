package zmaster587.blimps.event;

import zmaster587.blimps.Blimps;
import zmaster587.blimps.network.BlimpPacketHandler;
import zmaster587.blimps.network.PacketConfigSync;
import zmaster587.libVulpes.util.INetworkMachine;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

public class EventHandlerLogin {

	@SubscribeEvent
	public void event(PlayerLoggedInEvent event) {
		if(event.player.worldObj.isRemote) {

			BlimpPacketHandler.sendToPlayer(new PacketConfigSync(), event.player);

		}
	}

	/*@Override
	public void onPlayerLogin(EntityPlayer player) {

	}*/


}
