package zmaster587.blimps.network;

import io.netty.buffer.ByteBuf;
import zmaster587.blimps.entity.EntityCargoBlimp;
import zmaster587.blimps.entity.EntityScoutBlimp;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class PacketConfigSync extends BasePacket {

	double cargoVel, cargoHeight, scoutVel, scoutHeight;
	
	public PacketConfigSync() {
		
	}
	
	@Override
	public void write(ByteBuf out) {
		out.writeDouble(EntityCargoBlimp.maxVelocity);
		out.writeFloat(EntityCargoBlimp.maxHeight);
		out.writeDouble(EntityScoutBlimp.maxVelocity);
		out.writeFloat(EntityScoutBlimp.maxHeight);
	}

	@Override
	public void read(ByteBuf in) {
		cargoVel = in.readDouble();
		cargoHeight = in.readFloat();
		scoutVel = in.readDouble();
		scoutHeight = in.readFloat();
	}

	public void execute(EntityPlayer player, Side side) {
		EntityCargoBlimp.maxVelocity = cargoVel;
		EntityCargoBlimp.maxHeight = (float)cargoHeight;
		EntityScoutBlimp.maxVelocity = scoutVel;
		EntityScoutBlimp.maxHeight = (float)scoutHeight;
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
