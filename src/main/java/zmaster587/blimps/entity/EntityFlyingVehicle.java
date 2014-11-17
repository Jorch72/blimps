package zmaster587.blimps.entity;

import io.netty.buffer.ByteBuf;

import java.util.List;

import zmaster587.blimps.Blimps;
import zmaster587.blimps.network.BlimpMovementPacket;
import zmaster587.blimps.network.InventoryPacket;
import zmaster587.blimps.network.PacketMachine;
import zmaster587.blimps.network.ServerCommandPacket;
import zmaster587.libVulpes.util.ZUtils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class EntityFlyingVehicle extends Entity implements IInventory, INetworkEntity {


	public enum VehicleType {
		submarine,
		blimp
	}

	protected double speedMultiplier;
	private byte verticalDir;
	public boolean cruiseControl;
	protected float fanLoc;
	protected int freshBurnTime;
	protected int currentBurnTime;
	//Used to calculate rendering stuffs
	protected double speed;
	protected ItemStack inv[];
	protected final AxisAlignedBB localBoundingBox;
	protected short dockDistance;

	public int dockedX;
	public int dockedY;
	public int dockedZ;

	protected boolean isDocked;

	public EntityFlyingVehicle(World par1World)
	{
		super(par1World);
		localBoundingBox = AxisAlignedBB.getBoundingBox(0D, 0D, 0D, 0D, 0D, 0D);
		this.speedMultiplier = 0.07D;
		this.preventEntitySpawning = true;
		this.yOffset = this.height / 2.0F;
		fanLoc = 0.0F;
		cruiseControl = false;
		verticalDir = 0;
		inv = new ItemStack[1];
	}

	public EntityFlyingVehicle(World par1World, double par2, double par4, double par6)
	{
		this(par1World);

		//System.out.println(localBoundingBox);

		this.setPosition(par2, par4 + (double)this.yOffset, par6);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.prevPosX = par2;
		this.prevPosY = par4;
		this.prevPosZ = par6;
		inv = new ItemStack[1];
	}


	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	protected boolean canTriggerWalking()
	{
		return false;
	}


	@Override
	public void markDirty() {
		
	}
	
	public void updateRiderPosition()
	{
		if (this.riddenByEntity != null)
		{
			double d0 = Math.cos((double)this.rotationYaw * Math.PI / 180.0D) * 0.4D;
			double d1 = Math.sin((double)this.rotationYaw * Math.PI / 180.0D) * 0.4D;
			this.riddenByEntity.setPosition(this.posX + d0, this.posY + this.getMountedYOffset() + this.riddenByEntity.getYOffset(), this.posZ + d1);
		}
	}

	/**
	 * Returns the Y offset from the entity's position for any entity riding this one.
	 */
	public double getMountedYOffset()
	{
		return (double)this.height * 0.0D - 0.30000001192092896D;
	}

	/**
	 * Returns true if this entity should push and be pushed by other entities when colliding.
	 */
	public boolean canBePushed()
	{
		return !getIsDocked();
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	public boolean canBeCollidedWith()
	{
		return !this.isDead;
	}

	/**
	 * Must be overridden to set position
	 * @param player Player riding the entity
	 * @param dirToThis direction the face of the block is facing
	 * @param xCoord x coordinate of the block
	 * @param yCoord y coordinate of the block
	 * @param zCoord z coordinate of the block
	 */
	public void setDocked(EntityPlayer player, ForgeDirection dirToThis, int xCoord, int yCoord, int zCoord) {

		this.isDocked = true;

		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		this.dockedX = xCoord;
		this.dockedY = yCoord;
		this.dockedZ = zCoord;
		
		setVerticalDir(0);

		if(!this.worldObj.isRemote) {
			this.setIsDocked(true);
		}
		else {
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 17);
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 18);
		}
	}

	public void undock() {
		this.isDocked = false;
		setVerticalDir(0);
		if(!this.worldObj.isRemote) {
			this.setIsDocked(false);
			this.dockedX = 0;
			this.dockedY = 0;
			this.dockedZ = 0;
			//PacketDispatcher.sendPacketToPlayer(new ServerCommandPacket((byte)1).makePacket(), (Player) this.riddenByEntity);
		}
		else {
			float dist = getZoomDist();
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, dist, 17);
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, dist, 18);
		}
	}

	/**
	 * First layer of player interaction
	 */
	@Override
	public boolean interactFirst(EntityPlayer player)
	{
		if(this.getIsDocked())
			return false;
		if(this.riddenByEntity == null/* || (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != player)*/) {
			player.mountEntity(this);
			if(this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != player)	{
				ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 17);
				ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 18);
			}
			else if(this.worldObj.isRemote) {
				float dist = this.getZoomDist();
				ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, dist, 17);
				ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, dist, 18);
			}
			return true;
		}
		return false;
	}

	public void setVerticalDir(int verticalDir) {
		dataWatcher.updateObject(20, new Byte((byte)verticalDir));
	}
	public byte getVerticalDir() 
	{
		return dataWatcher.getWatchableObjectByte(20);
	}


	public void setFreshBurnTime(int time) {
		this.dataWatcher.updateObject(17, new Integer(this.freshBurnTime));
	}

	public void setCurrentBurnTime(int time) {
		this.dataWatcher.updateObject(18, new Integer(this.currentBurnTime));
	}

	public int getCurrentBurnTime() {
		return this.dataWatcher.getWatchableObjectInt(18);
	}

	public int getFreshBurnTime() {
		return this.dataWatcher.getWatchableObjectInt(17);
	}

	public void setIsDocked(boolean input) {
		this.isDocked = input;
		this.dataWatcher.updateObject(19, new Byte((byte)(input ? 1 : 0)));
	}

	public boolean getIsDocked() { 
		this.isDocked = this.dataWatcher.getWatchableObjectByte(19) == 1; 
		return this.isDocked;
	}

	public int getBurnTimeRemaining()
	{
		return currentBurnTime;
	}

	public int getScaledBurnTimeRemaining(int scale)
	{
		if(this.worldObj.isRemote) {
			freshBurnTime = this.getFreshBurnTime();
			currentBurnTime = this.getCurrentBurnTime();
		}

		if(freshBurnTime == 0)
			return 0;
		return (currentBurnTime * scale)/freshBurnTime;
	}

	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
	{
		if(!this.worldObj.isRemote && !this.isDead && par1DamageSource.getEntity() instanceof EntityPlayer && !par1DamageSource.getEntity().equals(this.riddenByEntity))
		{
			for(ItemStack i : getItemsDropOnDeath())
			{
				if(i != null)
					this.entityDropItem(i, 0.0F);
			}

			this.setDead();
			return true;
		}
		return false;
	}

	public abstract ItemStack[] getItemsDropOnDeath();

	public abstract ItemStack getItemInSlot(int slot);

	public abstract boolean canControl();

	public abstract void tryBurnFuel();

	public abstract float getMaxHeight();

	public abstract double getMaxVelocity();

	public abstract double getMaxVerticalSpeed();

	public abstract double getMaxVertAcceleration();

	public abstract float getZoomDist();

	public abstract VehicleType getVehicleType();

	/**
	 * @param dir direction the face of the block that is clicked is facing
	 * @return if the entity can dock
	 */
	public abstract boolean canDock(ForgeDirection dir);

	@Override
	public int getSizeInventory() {
		return inv.length;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return null;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if(inv == null)
			return null;
		return inv[i];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		ItemStack stack = getStackInSlot(i);
		if (stack != null) {
			setInventorySlotContents(i, null);
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv[i] = itemstack;
	}

	public void syncWithRemote() {
		if(!this.worldObj.isRemote) {
			this.setCurrentBurnTime(this.currentBurnTime);
			this.setFreshBurnTime(this.freshBurnTime);
		}
		else {
			this.currentBurnTime = this.getCurrentBurnTime();
			this.freshBurnTime = this.getFreshBurnTime();
		}


	}

	public boolean isBurningFuel() {
		return currentBurnTime > 0;
	}

	protected void doFuelTick()
	{
		if(currentBurnTime > 0)
			currentBurnTime--;
	}

	protected boolean canBurnItem(int slot)
	{
		if(getItemInSlot(slot) == null)
			return false;
		return TileEntityFurnace.isItemFuel(getItemInSlot(slot));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9)
	{
		double d3 = par1 - this.posX;
		double d4 = par3 - this.posY;
		double d5 = par5 - this.posZ;
		double d6 = d3 * d3 + d4 * d4 + d5 * d5;

		if (d6 <= 1.0D)
		{
			return;
		}

		this.setPosition(par1, par3, par5);
		this.setRotation(par7, par8);

	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		//Do some fuel handling
		if(!this.worldObj.isRemote) {
			if(!isBurningFuel() && !this.isDocked)
				tryBurnFuel();
			else
				doFuelTick();
		}

		syncWithRemote();
		byte b0 = 5;
		double d0 = 0.0D;


		//Adjust camera if the player shiftclicks out TODO: move
		if(worldObj.isRemote && this.riddenByEntity instanceof EntityPlayer && ((EntityPlayer)this.riddenByEntity).isSneaking()) {
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 18);
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 17);
		}


		for (int i = 0; i < b0; ++i)
		{
			double d1 = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * (double)(i + 0) / (double)b0 - 0.125D;
			double d2 = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * (double)(i + 1) / (double)b0 - 0.125D;
			AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.boundingBox.minX, d1, this.boundingBox.minZ, this.boundingBox.maxX, d2, this.boundingBox.maxZ);

			d0 += 1.0D / (double)b0;
		}

		double horizontalVelocity = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		double d5;

		double d10;
		double d11;


		//Handle riding entity movement(arrow keys)?
		if (canControl()) { 
			if(this.riddenByEntity != null && this.riddenByEntity instanceof EntityLivingBase)
			{
				if ((double)((EntityLivingBase)this.riddenByEntity).moveForward > 0.0D || cruiseControl)
				{
					this.motionX += -Math.sin((double)(this.riddenByEntity.rotationYaw * (float)Math.PI / 180.0F)) * this.speedMultiplier * 0.05000000074505806D;
					this.motionZ += Math.cos((double)(this.riddenByEntity.rotationYaw * (float)Math.PI / 180.0F)) * this.speedMultiplier * 0.05000000074505806D;
				}
			}
		}
		else if(this.riddenByEntity == null) {
			this.motionX *= 0.999f;
			this.motionZ *= 0.999f;
			if(!this.getIsDocked() && getVehicleType() == VehicleType.blimp) {
				this.setVerticalDir(-1);
			}
		}

		//Horizontal speed
		double newHorizontolVelocity = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

		if (newHorizontolVelocity > getMaxVelocity())
		{
			d5 = getMaxVelocity() / newHorizontolVelocity;
			this.motionX *= d5;
			this.motionZ *= d5;
			newHorizontolVelocity = getMaxVelocity();
		}

		if (newHorizontolVelocity > horizontalVelocity && this.speedMultiplier < getMaxVelocity())
		{
			this.speedMultiplier += (getMaxVelocity() - this.speedMultiplier) / (getMaxVelocity()*10);

			if (this.speedMultiplier > getMaxVelocity())
			{
				this.speedMultiplier = getMaxVelocity();
			}
		}
		else
		{
			this.speedMultiplier -= (this.speedMultiplier - 0.07D) / 35.0D;

			if (this.speedMultiplier < 0.07D)
			{
				this.speedMultiplier = 0.07D;
			}
		}


		double maxVertSpeed = this.getMaxVerticalSpeed();

		//Manage vertical speed
		if(Math.abs(this.motionY) < getMaxVertAcceleration()) 
			this.motionY = 0.0D;


		this.verticalDir = getVerticalDir();

		if(this.posY > getMaxHeight())
		{
			if(getVerticalDir() > 0)
				setVerticalDir(0);
			if(this.motionY > 0)
				this.motionY = 0;
		}
		if((this.getVehicleType() != VehicleType.submarine) == this.isInWater()) {
			if(verticalDir < 0 && (this.getVehicleType() != VehicleType.submarine) || getVerticalDir() > 0 && (this.getVehicleType() == VehicleType.submarine)) {
				this.motionY = -this.motionY*0.5;
				setVerticalDir(0);
			}
		}

		else if(Math.abs(this.motionY) < maxVertSpeed || verticalDir == 0 || motionY * verticalDir < 0)
		{

			if(verticalDir != 0)
			{
				if(verticalDir > 0  && this.isBurningFuel())
					this.motionY += getMaxVertAcceleration();
				else if(verticalDir < 0)
					this.motionY-= getMaxVertAcceleration()	;
			}
			else if(this.motionY != 0)
				decelerate();
		} else if(verticalDir != 0 && this.motionY * verticalDir > 0) {

			if(verticalDir > 0)
				this.motionY = (double)maxVertSpeed;
			else if(verticalDir < 0)
				this.motionY = -(double)maxVertSpeed;
		}
		//End Manage vertical speed
		if (this.onGround)
		{
			this.motionX *= 0.5D;
			this.motionY *= 0.5D;
			this.motionZ *= 0.5D;
		}

		// Because the player takes the same fall damage as the mount, we need to make sure the mount doesnt take fall
		// damage or the player would also die
		this.fallDistance = 0;

		this.moveEntity(this.motionX, this.motionY, this.motionZ);

		this.motionX *= 0.9900000095367432D;
		this.motionZ *= 0.9900000095367432D;

		this.rotationPitch = 0.0F;
		d5 = (double)this.rotationYaw;
		d11 = this.prevPosX - this.posX;
		d10 = this.prevPosZ - this.posZ;

		if (d11 * d11 + d10 * d10 > 0.001D)
		{
			d5 = (double)((float)(Math.atan2(d10, d11) * 180.0D / Math.PI));
		}

		//Rotate 20 degrees per tick
		double d12 = MathHelper.wrapAngleTo180_double(d5 - (double)this.rotationYaw);

		if (d12 > 20.0D)
		{
			d12 = 20.0D;
		}

		if (d12 < -20.0D)
		{
			d12 = -20.0D;
		}

		this.rotationYaw = (float)((double)this.rotationYaw + d12);
		this.setRotation(this.rotationYaw, this.rotationPitch);

		speed = Math.sqrt(Math.pow(this.motionX, 2) + Math.pow(this.motionZ, 2));
		fanLoc = MathHelper.wrapAngleTo180_float((float) (0.8F * speed) + fanLoc);
	}

	private void decelerate()
	{
		if(MathHelper.abs((float)this.motionY) < getMaxVertAcceleration())
			this.motionY = 0;
		else if(this.motionY < 0)
			this.motionY += getMaxVertAcceleration();
		else if(this.motionY > 0)
			this.motionY -= getMaxVertAcceleration();
	}

	public double getSpeed() {
		return speed;
	}
	public float getFanPosition() { return fanLoc; }


	// Cruise control status is not saved to NBT
	// THIS IS INTENTIONAL
	@Override
	public void writeToNBT(NBTTagCompound compd)
	{
		super.writeToNBT(compd);


		NBTTagList list = new NBTTagList();

		NBTTagList itemList = new NBTTagList();
		for(int i = 0; i < inv.length; i++)
		{
			ItemStack stack = inv[i];

			if(stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)(i));
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		compd.setTag("Inventory", itemList);

		compd.setInteger("currentBurnTime", this.currentBurnTime);
		compd.setInteger("freshBurnTime", this.freshBurnTime);
		compd.setBoolean("docked", isDocked);

		compd.setInteger("dockedX", dockedX);
		compd.setInteger("dockedY", dockedY);
		compd.setInteger("dockedZ", dockedZ);

	}

	@Override
	public void readFromNBT(NBTTagCompound compd)
	{
		super.readFromNBT(compd);

		currentBurnTime = compd.getInteger("currentBurnTime");
		freshBurnTime = compd.getInteger("freshBurnTime");
		isDocked =compd.getBoolean("docked");

		this.setIsDocked(isDocked);

		dockedX = compd.getInteger("dockedX");
		dockedY = compd.getInteger("dockedY");
		dockedZ = compd.getInteger("dockedZ");

		if(!this.worldObj.isRemote) {
			setFreshBurnTime(freshBurnTime);
			setCurrentBurnTime(currentBurnTime);

			NBTTagList tagList = compd.getTagList("Inventory", (byte)10);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
				byte slot = tag.getByte("Slot");
				if (slot >= 0 && slot < inv.length) {
					inv[slot] = ItemStack.loadItemStackFromNBT(tag);
				}
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbtCompound) {
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbtCompound) {

	}

	@Override
	protected void entityInit()
	{
		this.dataWatcher.addObject(17, new Integer(0));
		this.dataWatcher.addObject(18, new Integer(1));
		this.dataWatcher.addObject(19, new Byte((byte)0));
		this.dataWatcher.addObject(20, new Byte((byte)0));
	}


	@Override
	public void readDataFromNetwork(ByteBuf in, byte packetId, NBTTagCompound nbt) {

		if(packetId == 0) {
			nbt.setInteger("currentBurn", in.readShort());
			nbt.setInteger("freshBurn", in.readShort());
		}
		// packet 1 in use
		// packet 2 in use
	}

	@Override
	public void writeDataToNetwork(ByteBuf out, byte packetId) {
		if(packetId == 0) {
			out.writeShort(this.currentBurnTime);
			out.writeShort(this.freshBurnTime);
		}
		// packet 1 in use
		// packet 2 in use
	}

	@Override
	public void useNetworkData(EntityPlayer player, Side side, byte id,
			NBTTagCompound nbt) {
		if(id == 0) {
			this.currentBurnTime = nbt.getShort("currentBurn");
			this.freshBurnTime = nbt.getShort("freshBurn");
		}
		else if(side.isClient() && id == 1) {
			float dist = ((EntityFlyingVehicle)player.ridingEntity).getZoomDist();
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, dist, 18);
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, dist, 17);
		}
		else if(side.isClient() && id == 2) {
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 17);
			ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, FMLClientHandler.instance().getClient().entityRenderer, 4.0f, 18);
		}
	}
}
