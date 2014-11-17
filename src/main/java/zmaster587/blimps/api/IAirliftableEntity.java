package zmaster587.blimps.api;

import net.minecraft.nbt.NBTTagCompound;

public interface IAirliftableEntity {
	
	/**
	 * Gets the NBT data required to create a tile from NBT
	 * @return NBT tag the describes the entity
	 */
	public NBTTagCompound getNBTData();
	
	/**
	 * Sets the NBT from the tile entity
	 * @param nbt the nbt data to set from
	 */
	public void setNBTData(NBTTagCompound nbt, int id, int meta);
}
