package zmaster587.blimps.entity;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import zmaster587.blimps.Blimps;
import zmaster587.blimps.ModConfiguration;
import zmaster587.blimps.api.IAirliftableTile;
import zmaster587.blimps.network.ServerCommandPacket;
import zmaster587.libVulpes.util.ZUtils;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;

public class EntityCargoBlimp extends EntityFlyingVehicle implements ICargo {

	public Entity cargoEntity;

	public float cargoOffsetX;
	public float cargoOffsetY;
	public float cargoOffsetZ;
	public double liftOffset;
	public boolean canCargoVanish;
	private static final double liftSpeed = 0.1D;

	// Used to get maximum height, can be changed by upgrades/config
	public static float maxHeight;
	// Used to get the maximum horizontal velocity, can be changed by upgrade/configs
	public static double maxVelocity;

	public EntityCargoBlimp(World par1World)
	{
		super(par1World);
		inv = new ItemStack[1];
		this.setSize(1.5F, 2F);
		this.localBoundingBox.setBounds(-1.8D,-5.6D,-2D,0.5D,0.5D,3D);
		cargoOffsetX = 0F;
		cargoOffsetY = 0F;
		cargoOffsetZ = -2.2F;
		cargoEntity = null;
		liftOffset = 0;
		canCargoVanish = false;
	}

	public EntityCargoBlimp(World par1World, double par2, double par4,
			double par6) {
		super(par1World, par2, par4, par6);

		this.setSize(1.5F, 2F);
		this.localBoundingBox.setBounds(-1.8D,-5.6D,-2D,0.5D,0.5D,3D);
		cargoOffsetX = 0F;
		cargoOffsetY = 0F;
		cargoOffsetZ = -2.2F;
		cargoEntity = null;
		liftOffset = 0;
		inv = new ItemStack[1];
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(21, new Integer(-1));
	}

	private void setCargo(Entity e) {
		if(e != null)
			this.dataWatcher.updateObject(21, new Integer(e.getEntityId()));
		else 
			this.dataWatcher.updateObject(21, new Integer(-1));
	}

	public boolean canDock(ForgeDirection dir) {return dir != ForgeDirection.DOWN;}

	public boolean canControl()
	{
		return isBurningFuel();
	}

	// If the blimp has fuel accept vertical direction change otherwise only allow hover and down directions
	public void setVerticalSpeed(byte type)
	{
		if(canControl())
			super.setVerticalDir(type);
		else if(type != 1)
			super.setVerticalDir(type);
	}

	public float getZoomDist() {
		return 10.0f;
	}

	public boolean canBePushed()
	{
		return this.cargoEntity == null;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(liftOffset > 0) {
			liftOffset -= liftSpeed;
			if(liftOffset < 0)
				liftOffset = 0;
		}

		if(isBurningFuel() && this.rand.nextInt(5) == 0)
		{
			float offsetX, offsetZ, yawRad =  (float)(Math.PI/180.0F) * this.rotationYaw;

			offsetZ = MathHelper.sin(yawRad + 0.391699939F) * 4.976946855F;
			offsetX = MathHelper.cos(yawRad + 0.391699939F) * 4.976946855F;

			this.worldObj.spawnParticle("largesmoke",this.posX + offsetX, this.posY + 0.2F, this.posZ + offsetZ,0,0,0);

			offsetZ = MathHelper.sin(yawRad - 0.391699939F) * 4.976946855F;
			offsetX = MathHelper.cos(yawRad - 0.391699939F) * 4.976946855F;

			this.worldObj.spawnParticle("largesmoke",this.posX + offsetX, this.posY + 0.2F, this.posZ + offsetZ,0,0,0);
		}
	}

	@Override 
	public void onEntityUpdate() {
		super.onEntityUpdate();
		if(this.cargoEntity != null)
		{

			if(this.cargoEntity.isDead)
				this.cargoEntity = null;
			else
			{
				double offsetX;
				double offsetZ;

				offsetX = -(this.cargoOffsetZ*Math.cos(this.rotationYaw*(Math.PI/180.0F)) + (this.cargoOffsetX*Math.sin(this.rotationYaw*(Math.PI/180.0F))));
				offsetZ = -(this.cargoOffsetZ*Math.sin(this.rotationYaw*(Math.PI/180.0F)) + (this.cargoOffsetX*Math.cos(this.rotationYaw*(Math.PI/180.0F))));

				this.cargoEntity.fallDistance = 0.0f;

				this.cargoEntity.rotationYaw = 180;
				this.cargoEntity.motionX = this.posX - this.prevPosX;
				this.cargoEntity.motionY = this.motionY;
				this.cargoEntity.motionZ = this.posZ - this.prevPosZ;

				this.cargoEntity.moveEntity(this.posX + offsetX - this.cargoEntity.posX, this.posY + this.cargoOffsetY - this.cargoEntity.posY - this.liftOffset, this.posZ + offsetZ - this.cargoEntity.posZ);
			}
		}
	}
	
	@Override
	public String  getInventoryName() {
		return "Cargo Blimp";
	}
	
	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return true;//this.getDistanceToEntity(entityplayer) < 30.0F;
	}

	@Override
	public void openInventory() {
		// TODO Auto-generated method stub
	}

	@Override
	public void closeInventory() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public int getBurnTimeRemaining() {
		return currentBurnTime;
	}

	@Override
	public ItemStack getItemInSlot(int slot) {	
		if(inv == null)
			return null;
		else
			return inv[slot];
	}

	@Override
	public void tryBurnFuel() {
		if(currentBurnTime == 0 && canBurnItem(0))
		{
			freshBurnTime = currentBurnTime = TileEntityFurnace.getItemBurnTime(inv[0]);
			if(inv[0].isItemEqual(new ItemStack(Items.lava_bucket)))
				inv[0] = new ItemStack(Items.bucket);
			else {
				inv[0].stackSize--;
				if(inv[0].stackSize == 0)
					inv[0] = null;
			}
		}
	}

	@Override
	public ItemStack[] getItemsDropOnDeath() {
		ItemStack[] stack = { inv[0], new ItemStack(Blimps.itemBlimpCargo) };
		return stack;
	}

	public float getMaxHeight()
	{
		return maxHeight;
	}

	public double getMountedYOffset()
	{
		return -0.2D;
	}


	public double getMaxVelocity()
	{
		return this.maxVelocity;//0.3D;
	}

	public double getMaxVerticalSpeed()
	{
		return 0.05D;
	}


	//TODO: some kind of blacklist, such as players or other blimps
	/**
	 ** @param e Entity that is to become the cargo, null to unmount
	 **/
	public void mountCargo(Entity e)
	{
		if(e == null)
		{
			if(this.cargoEntity != null)
			{
				this.cargoEntity.setInvisible(false);
				this.cargoEntity.noClip = false;

				double offsetX = -(this.cargoOffsetZ*Math.cos(this.rotationYaw*(Math.PI/180.0F)) + (this.cargoOffsetX*Math.sin(this.rotationYaw*(Math.PI/180.0F))));
				double offsetZ = -(this.cargoOffsetZ*Math.sin(this.rotationYaw*(Math.PI/180.0F)) + (this.cargoOffsetX*Math.cos(this.rotationYaw*(Math.PI/180.0F))));
				this.cargoEntity.setPosition(this.posX + offsetX, this.posY + this.cargoOffsetY - this.liftOffset, this.posZ + offsetZ);

				this.cargoEntity = null;
			}
		}
		else
		{
			//TODO: return some kind of message to the player
			if(this.cargoEntity != null)
				return;
			else
			{
				this.cargoEntity = e;
				this.cargoEntity.noClip = true;
				
				if(e instanceof EntityMinecart) {
					canCargoVanish = false;
				}
				else {
					e.setInvisible(true);
					canCargoVanish = true;
				}
				
				this.liftOffset = this.posY - e.posY;
			}
		}
		if(!worldObj.isRemote) {
			this.setCargo(e);
		}
	}

	public void unmountCargo(int slot)
	{
		mountCargo(null);
	}

	public Entity findCargo(float maxDistance)
	{

		ArrayList<TileEntity> tiles = new ArrayList<TileEntity>();

		for(int x = (int) -maxDistance; x < maxDistance; x++) {
			for(int z = (int) -maxDistance; z < maxDistance; z++) {
				for(int y = (int) posY; y > 1 && !worldObj.isBlockNormalCubeDefault((int)posX + x, y+1, (int)posZ + z,false); y--) {
					TileEntity tile = worldObj.getTileEntity((int)posX + x, y, (int)posZ + z);
					if(tile instanceof IAirliftableTile || ZUtils.doesArrayContains(ModConfiguration.transportBlimpWhitelist, worldObj.getBlock((int)posX + x, y, (int)posZ + z))) 
						tiles.add(tile);

				}
			}
		}

		if(!tiles.isEmpty()) {
			float minDistance = Float.MAX_VALUE;
			TileEntity closestTile = null;
			for(TileEntity e : tiles) {
				float thisDist = (float) this.getDistanceSq(e.xCoord, e.yCoord, e.zCoord);
				if(thisDist < minDistance) {
					minDistance = thisDist;
					closestTile = e;
				}
			}
			EntityAirlifted entity;
			if(closestTile instanceof IAirliftableTile) {
				entity = new EntityAirlifted(worldObj, (IAirliftableTile) closestTile);
				entity.setPosition(closestTile.xCoord, closestTile.yCoord + 2, closestTile.zCoord);
				entity.noClip = true;
				worldObj.spawnEntityInWorld(entity);
			}
			else {
				entity = new EntityAirlifted(worldObj, closestTile);
				entity.setPosition(closestTile.xCoord, closestTile.yCoord + 2, closestTile.zCoord);
				entity.noClip = true;
				worldObj.spawnEntityInWorld(entity);

			}
			return entity;
		}

		ArrayList<Entity> entityList = (ArrayList)this.worldObj.getEntitiesWithinAABBExcludingEntity(this, 
				AxisAlignedBB.getBoundingBox(this.posX - maxDistance, this.posY - maxDistance, this.posZ - maxDistance, this.posX + maxDistance, this.posY - 1.0F, this.posZ + maxDistance));//new AxisAlignedBB());

		//If not entitys are found return null else find the best candidate
		if(entityList.isEmpty())
			return null;
		else
		{
			Entity ret = null;
			float closestDistance = Float.MAX_VALUE;
			for(Entity e : entityList)
			{
				float currentDist = this.getDistanceToEntity(e);
				//Make sure no blocks are in the way
				MovingObjectPosition test = this.worldObj.rayTraceBlocks(Vec3.createVectorHelper(this.posX, this.posY, this.posZ),Vec3.createVectorHelper(e.posX, e.posY, e.posZ));

				//No MovingObjectPosition mean no blocks in the way
				if(test == null && currentDist < closestDistance && (e instanceof EntityTameable || e instanceof INpc || e instanceof EntityMinecart || e instanceof EntityAnimal))
				{
					ret = e;
					closestDistance = currentDist;
				}
			}
			return ret;
		}
	}



	@Override
	public void setDead()
	{
		super.setDead();

		if(this.cargoEntity != null)
			this.cargoEntity.noClip = false;
	}

	public float getSearchRadius()
	{
		return 3.0F;
	}

	public Entity getCargo(int slot)
	{
		return worldObj.getEntityByID(this.dataWatcher.getWatchableObjectInt(21));
		//return cargoEntity;
	}

	public float getVertPickup()
	{
		return 8.0F;
	}

	@Override
	public double getMaxVertAcceleration() {
		return 0.005F;
	}


	@Override
	public VehicleType getVehicleType() {
		// TODO Auto-generated method stub
		return VehicleType.blimp;
	}

	@Override
	public void readDataFromNetwork(ByteBuf in, byte packetId, NBTTagCompound nbt) {
		super.readDataFromNetwork(in, packetId, nbt);
		// packet 0 in use
		// packet 1 in use
		// packet 2 in use
	}

	@Override
	public void writeDataToNetwork(ByteBuf out, byte packetId) {
		super.writeDataToNetwork(out, packetId);
		//packet 0 in use
		// packet 1 in use
		// packet 2 in use
	}

	@Override
	public void useNetworkData(EntityPlayer player, Side side, byte id,
			NBTTagCompound nbt) {
		super.useNetworkData(player, side, id, nbt);
	}
}