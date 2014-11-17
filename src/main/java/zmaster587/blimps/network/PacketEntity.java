package zmaster587.blimps.network;

import io.netty.buffer.ByteBuf;
import zmaster587.blimps.entity.INetworkEntity;
import zmaster587.libVulpes.util.INetworkMachine;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class PacketEntity extends BasePacket {

	INetworkEntity entity;
	
	NBTTagCompound nbt;
	
	byte packetId;
	
	public PacketEntity() {
		nbt = new NBTTagCompound();
	};
	
	public PacketEntity(INetworkEntity machine, byte packetId) {
		this();
		this.entity = machine;
		this.packetId = packetId;
	}
	
	
	@Override
	public void write(ByteBuf out) {
		out.writeInt(((Entity)entity).worldObj.provider.dimensionId);
		out.writeInt(entity.getEntityId());
		out.writeByte(packetId);
		
		entity.writeDataToNetwork(out, packetId);
	}

	@Override
	public void read(ByteBuf in) {
		//DEBUG:
		World world = DimensionManager.getWorld(in.readInt());
		int entityId = in.readInt();
		packetId = in.readByte();
		
		Entity ent = world.getEntityByID(packetId);
		
		if(ent != null && ent instanceof INetworkEntity) {
			entity = (INetworkEntity)ent;
			entity.readDataFromNetwork(in, packetId, nbt);
		}
		else {
			//Error
		}
	}

	public void execute(EntityPlayer player, Side side) {
		if(entity != null)
			entity.useNetworkData(player, side, packetId, nbt);
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
