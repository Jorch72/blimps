package zmaster587.blimps.network;

import io.netty.buffer.ByteBuf;
import zmaster587.blimps.Blimps;
import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.entity.EntityCargoBlimp;
import zmaster587.blimps.entity.ICargo;
import zmaster587.libVulpes.util.INetworkMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

public class ServerCommandPacket extends BasePacket {


	/* 0: Attempt Pickup
	 * 1: CamZoom
	 * 2: defaultCamZoom
	 */

	byte commandCode;

	public ServerCommandPacket() { }

	public ServerCommandPacket(byte commandCode)
	{
		this.commandCode = commandCode;
	}

	@Override
	public void write(ByteBuf out) {
		out.writeByte(commandCode);

	}

	@Override
	public void read(ByteBuf in) {
		commandCode = in.readByte();

	}

	public void execute(EntityPlayer player, Side side) {

		if(side == Side.SERVER)
		{
			if(commandCode == (byte)0 && player.ridingEntity != null && player.ridingEntity instanceof ICargo)
			{
				//Unload if it is there

				if(((ICargo)player.ridingEntity).getCargo(0) != null)
				{
					
					
					for(EntityPlayer trackingPlayer : ((WorldServer)player.worldObj).getEntityTracker().getTrackingPlayers(player.ridingEntity)) {// .func_151247_a(player.ridingEntity, new PickupEntityPacket(player.ridingEntity).makePacket());
						BlimpPacketHandler.sendToPlayer(new PickupEntityPacket(player.ridingEntity), player);
					}
					((ICargo)player.ridingEntity).unmountCargo(0);

					return;
				}

				Entity thingToPickup = ((ICargo)player.ridingEntity).findCargo(((ICargo)player.ridingEntity).getVertPickup());
				if(thingToPickup != null)
				{	
					((ICargo)player.ridingEntity).mountCargo(thingToPickup);
					
					for(EntityPlayer trackingPlayer : ((WorldServer)player.worldObj).getEntityTracker().getTrackingPlayers(player.ridingEntity)) {// .func_151247_a(player.ridingEntity, new PickupEntityPacket(player.ridingEntity).makePacket());

						BlimpPacketHandler.sendToPlayer(new PickupEntityPacket(player.ridingEntity,thingToPickup), player);
					}
				}
			}
		}
		else
			if(commandCode == (byte)1) {
				if(player.ridingEntity != null) {
					float dist = ((EntityFlyingVehicle)player.ridingEntity).getZoomDist();
					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, dist, 15);
					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, dist, 16);
				}
			}
			else if(commandCode == (byte)2) {
				ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 15);
				ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 16);
			}
			else
				System.out.println("Get the duct-tape, this needs fixing ... did you try to execute a server command on the client?");
	}
	
	@Override
	public void executeServer(EntityPlayerMP player) {
		execute((EntityPlayer)player, Side.SERVER);
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		execute((EntityPlayer)player, Side.CLIENT);
	}

}
