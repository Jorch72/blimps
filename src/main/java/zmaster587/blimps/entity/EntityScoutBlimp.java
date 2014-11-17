package zmaster587.blimps.entity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import zmaster587.blimps.Blimps;
import zmaster587.libVulpes.util.ZUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;

public class EntityScoutBlimp extends EntityFlyingVehicle {

	// Used to get the maximum horizontal velocity, can be changed by upgrade/configs
	public static double maxVelocity;
	// Used to get maximum height, can be changed by upgrades/config
	public static float maxHeight;
	
	public EntityScoutBlimp(World par1World)
	{
		super(par1World);
		this.setSize(2.5F, 1.0F);
		localBoundingBox.setBounds(-0.5D, 0D, -2.5D, 0.5D, 3D, 2.5D);
	}

	public EntityScoutBlimp(World par1World, double par2, double par4,
			double par6) {
		super(par1World, par2, par4, par6);
		localBoundingBox.setBounds(-0.5D, 0D, -2.5D, 0.5D, 3D, 2.5D);
	}

	public boolean canDock(ForgeDirection dir) { return dir != ForgeDirection.DOWN; }
	
	public boolean canControl()
	{
		return isBurningFuel();
	}

	public void setDocked(EntityPlayer player, ForgeDirection dirToThis, int xCoord, int yCoord, int zCoord) {
		
		double newRot = (((Math.abs(dirToThis.offsetX)*90) - dirToThis.offsetX*90) + (dirToThis.offsetZ*90)) + 90.0F;
		this.setPositionAndRotation(xCoord + 0.5D + (dirToThis.offsetX*(0.5D + localBoundingBox.maxX)),
					0.5 + yCoord + (dirToThis.offsetY * (this.height + 0.5D)),
					zCoord + 0.5D + (dirToThis.offsetZ*(0.5D + localBoundingBox.maxX)),
					(float)newRot,this.rotationPitch);
		super.setDocked(player, dirToThis, xCoord, yCoord, zCoord);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox() {
		return ZUtils.convertLocalBBToGlobal(this.localBoundingBox, this.boundingBox, this, this.rotationYaw);
	}
	
	@Override
	public AxisAlignedBB getCollisionBox(Entity entity) {
		return getBoundingBox();
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
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(isBurningFuel() && this.rand.nextInt(5) == 0)
		{
			float offsetX, offsetZ, yawRad =  (float)(Math.PI/180.0F) * this.rotationYaw;
			offsetZ = MathHelper.sin(yawRad) * 1.6F;
			offsetX = MathHelper.cos(yawRad) * 1.6F;
			this.worldObj.spawnParticle("largesmoke",this.posX + offsetX, this.posY - 0.5F, this.posZ + offsetZ,0,0,0);
		}
	}


	@Override
	public String getInventoryName() {
		return "Scout Blimp";
	}

	@Override
	public boolean hasCustomInventoryName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return entityplayer.equals(this.riddenByEntity);//this.getDistanceToEntity(entityplayer) < 30.0F;
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
    public void setInventorySlotContents(int slot, ItemStack stack) {
            inv[slot] = stack;
            if (stack != null && stack.stackSize > getInventoryStackLimit()) {
                    stack.stackSize = getInventoryStackLimit();
            }              
    }

    @Override
    public ItemStack decrStackSize(int slot, int amt) {
            ItemStack stack = getStackInSlot(slot);
            if (stack != null) {
                    if (stack.stackSize <= amt) {
                            setInventorySlotContents(slot, null);
                    } else {
                            stack = stack.splitStack(amt);
                            if (stack.stackSize == 0) {
                                    setInventorySlotContents(slot, null);
                            }
                    }
            }
            return stack;
    }

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return TileEntityFurnace.getItemBurnTime(itemstack) != 0;
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
		ItemStack[] stack = { inv[0], new ItemStack(Blimps.itemBlimpScout) };
		return stack;
	}

	public double getMountedYOffset()
	{
		return -0.6D;
	}
	
	public float getMaxHeight()
	{
		return maxHeight;//255.0F;
	}
	
	public double getMaxVelocity()
	{
		return maxVelocity;//0.75D;
	}
	
	public double getMaxVerticalSpeed()
	{
		return 0.1D;
	}

	@Override
	public double getMaxVertAcceleration() {
		// TODO Auto-generated method stub
		return 0.005D;
	}

	@Override
	public VehicleType getVehicleType() {
		// TODO Auto-generated method stub
		return VehicleType.blimp;
	}
}