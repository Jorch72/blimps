package zmaster587.blimps.network;

import io.netty.buffer.ByteBuf;

import java.util.logging.Logger;

import zmaster587.blimps.entity.EntityFlyingVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class BlimpMovementPacket extends BasePacket {

	private byte type;

	public BlimpMovementPacket(){
	}

	public BlimpMovementPacket(byte type) {
		this.type = type;
	}

	@Override
	public void write(ByteBuf out) {
		out.writeByte(type);
	}

	@Override
	public void read(ByteBuf in) {
		type = in.readByte(); 
	}

	public void execute(EntityPlayer player, Side side) {
		if(player.ridingEntity instanceof EntityFlyingVehicle)
		{
			EntityFlyingVehicle entity = (EntityFlyingVehicle)player.ridingEntity;

			if(type == (byte)2)
				entity.cruiseControl = !entity.cruiseControl;
			else {
				entity.setVerticalDir(type);
			}
		}
	}

	@Override
	public void executeClient(EntityPlayer player) {
		execute((EntityPlayer)player, Side.CLIENT);
		
	}

	@Override
	public void executeServer(EntityPlayerMP player) {
		execute((EntityPlayer)player, Side.SERVER);
		
	}
}
