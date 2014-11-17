package zmaster587.blimps.tileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public interface IMachine {

	public int getXCoord();
	
	public int getYCoord();
	
	public int getZCoord();
	
	public int getWorldId();
	
	public void writeDataToNetwork(ByteArrayDataOutput out, byte id);
	
	public void readDataFromNetwork(ByteArrayDataInput in, NBTTagCompound nbt);
	
	public void useNetworkData(EntityPlayer player, Side side, byte id, NBTTagCompound nbt);
}
