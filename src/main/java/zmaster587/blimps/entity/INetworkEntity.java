package zmaster587.blimps.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.relauncher.Side;

public interface INetworkEntity {
	
	//Cannot overwrite Entity
	public int getEntityId();
	
	public void writeDataToNetwork(ByteBuf out, byte id);
	
	public void readDataFromNetwork(ByteBuf in, byte packetId, NBTTagCompound nbt);
	
	public void useNetworkData(EntityPlayer player, Side side, byte id, NBTTagCompound nbt);
}
