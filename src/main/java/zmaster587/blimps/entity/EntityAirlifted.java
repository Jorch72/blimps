package zmaster587.blimps.entity;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import zmaster587.blimps.api.IAirliftableEntity;
import zmaster587.blimps.api.IAirliftableTile;

public class EntityAirlifted extends Entity implements IAirliftableEntity {

	NBTTagCompound customNbt;
	int meta;
	Block block;
	TileEntity entity;

	private final double fallspeed = -.5;

	public EntityAirlifted(World world) {
		super(world);
		customNbt = new NBTTagCompound();
		setSize(.5F, .5F);
	}

	public TileEntity getStoredTile() {
		return entity;
	}

	public EntityAirlifted(World world, IAirliftableTile tile) {
		this(world);
		block = world.getBlock(((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord);
		meta = world.getBlockMetadata(((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord);
		entity = world.getTileEntity(((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord);
		NBTTagCompound nbt = new NBTTagCompound();
		((TileEntity)tile).writeToNBT(nbt);
		entity.readFromNBT(nbt);
		tile.onLifted(this);
	}

	public EntityAirlifted(World world, TileEntity tile) {
		this(world);
		block = world.getBlock(((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord);
		meta = world.getBlockMetadata(((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord);
		entity = world.getTileEntity(((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord);
		NBTTagCompound nbt = new NBTTagCompound();
		((TileEntity)tile).writeToNBT(nbt);
		entity = TileEntity.createAndLoadEntity(nbt);
		
		//entity.readFromNBT(nbt);
		//tile.onLifted(this);
		world.removeTileEntity(tile.xCoord, tile.yCoord, tile.zCoord);
		world.setBlockToAir(((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		moveEntity(0, fallspeed, 0);
	}

	
	
	/**
	 * Checks for block collisions, and calls the associated onBlockCollided method for the collided block.
	 */
	@Override
	protected void func_145775_I()
	{
		super.func_145775_I();

		if(this.noClip)
			return;

		int x = MathHelper.floor_double(this.boundingBox.minX + (this.boundingBox.maxX - this.boundingBox.minX)/2);
		int y = MathHelper.floor_double(this.boundingBox.minY - 0.001D);
		int z = MathHelper.floor_double(this.boundingBox.minZ + (this.boundingBox.maxZ - this.boundingBox.minZ)/2);

		if(!this.worldObj.isAirBlock(x, y, z) && !worldObj.getBlock(x, y, z).getMaterial().isReplaceable()) {
			this.worldObj.setBlock(x, y+1, z, block, meta, 3);

			TileEntity tile = this.worldObj.getTileEntity(x, y + 1, z);
			if(tile != null) {
				if(tile instanceof IAirliftableTile)
					((IAirliftableTile)this.worldObj.getTileEntity(x, y + 1, z)).onDropped(this);
				else {
					//get the coords from the new tile
					NBTTagCompound tempTag = new NBTTagCompound();
					//nbt to write to the new tile
					NBTTagCompound tag = new NBTTagCompound();
					tile.writeToNBT(tempTag);
					
					this.writeTileToNBT(tag);
					tag.setInteger("x", tempTag.getInteger("x"));
					tag.setInteger("y", tempTag.getInteger("y"));
					tag.setInteger("z", tempTag.getInteger("z"));
					tile.readFromNBT(tag);
				}
			}
			this.setDead();
		}
	}

	@Override
	public NBTTagCompound getNBTData() {
		// TODO Auto-generated method stub
		return customNbt;
	}

	@Override
	public void setNBTData(NBTTagCompound nbt, int id, int meta ) {
		customNbt = nbt;		
	}

	@Override
	protected void entityInit() {
	}

	private void readTileFromNBT(NBTTagCompound nbt) {
		entity = TileEntity.createAndLoadEntity(nbt);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		//super.readFromNBT(nbt);
		
		NBTTagCompound tileNBT = new NBTTagCompound();
		
		if(nbt.hasKey("tile"))
			readTileFromNBT(nbt.getCompoundTag("tile"));

		customNbt = nbt.getCompoundTag("custom");
		block = Block.getBlockById(customNbt.getInteger("BlockID"));
		meta = customNbt.getInteger("metadata");
	}

	private void writeTileToNBT(NBTTagCompound nbt) {
		if(entity != null) {
			entity.writeToNBT(nbt);
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		//super.writeToNBT(nbt);
		NBTTagCompound tileNBT = new NBTTagCompound();
		writeTileToNBT(tileNBT);

		nbt.setTag("tile", tileNBT);
		customNbt.setInteger("BlockID", Block.getIdFromBlock(block));
		customNbt.setInteger("metadata", meta);
		nbt.setTag("custom", customNbt);
	}
}
