package zmaster587.blimps.entity;

import javax.rmi.CORBA.Tie;

import zmaster587.blimps.Blimps;
import zmaster587.blimps.entity.EntityFlyingVehicle.VehicleType;
import zmaster587.libVulpes.util.ZUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EntitySub extends EntityFlyingVehicle {

	int tickOutOfWater;

	public EntitySub(World world) {
		super(world);
		this.setSize(1.5F, 1.5F);
		localBoundingBox.setBounds(-1.15D, -0.46D, -2.4D, 1.15D, 1.28D, 0.35D);
		tickOutOfWater = 0;
	}

	public EntitySub(World world, double x, double y, double z) {
		super(world,x,y,z);
		localBoundingBox.setBounds(-1.15D, -0.46D, -2.4D, 1.15D, 1.28D, 0.35D);
		tickOutOfWater = 0;
	}

	public boolean canDock(ForgeDirection dir) { return true; }

	@Override
	public void setDocked(EntityPlayer player, ForgeDirection dirToThis, int xCoord, int yCoord, int zCoord) {
		super.setDocked(player, dirToThis, xCoord, yCoord, zCoord);

		//{

		double newRot = (((Math.abs(dirToThis.offsetX)*90) - dirToThis.offsetX*90) + (dirToThis.offsetZ*90));
		//AxisAlignedBB localBounds = ZUtils.rotateAABB(localBoundingBox, newRot);

		this.setPositionAndRotation(xCoord + 0.5D + (dirToThis.offsetX*(0.5D + localBoundingBox.maxZ)),
				0.5 + yCoord + (dirToThis.offsetY * (this.height + 0.5D)),
				zCoord + 0.5D + (dirToThis.offsetZ*(0.5D + localBoundingBox.maxZ)),
				(float)newRot,this.rotationPitch);

		//}

		player.removePotionEffect(16);
		player.removePotionEffect(13);
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return ZUtils.convertLocalBBToGlobal(this.localBoundingBox, this.boundingBox, this, this.rotationYaw);
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entity) {
		return getBoundingBox();
	}

	@Override
	public boolean interactFirst(EntityPlayer par1EntityPlayer) {
		return super.interactFirst(par1EntityPlayer);
	}

	@Override
	public String getInventoryName() {
		// TODO Auto-generated method stub
		return "Small Submarine";
	}

	public void setVerticalSpeed(byte type)
	{
		if(canControl())
			super.setVerticalDir(type);
		else if(type != 1)
			super.setVerticalDir(type);

	}

	public boolean isPushedByWater() {
		return this.riddenByEntity == null;
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
		return true;//this.riddenByEntity != null;// && this.riddenByEntity.equals(entityplayer);
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
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public ItemStack[] getItemsDropOnDeath() {
		// TODO Auto-generated method stub
		//return new ItemStack[] {newDoubleNBTList(ItemStack())};
		return new ItemStack[] {inv[0], new ItemStack(Blimps.itemSubmarine)};
	}

	@Override
	public ItemStack getItemInSlot(int slot) {
		// TODO Auto-generated method stub
		return inv[slot];
	}

	@Override
	public boolean canControl() {
		// TODO Auto-generated method stub
		return isBurningFuel() && this.isInWater() && !this.isDocked;
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
	public float getMaxHeight() {
		// TODO Auto-generated method stub
		return 64;
	}

	@Override
	public double getMaxVelocity() {
		// TODO Auto-generated method stub
		return 0.2D;
	}

	@Override
	public double getMaxVerticalSpeed() {
		// TODO Auto-generated method stub
		return 0.05D;
	}

	@Override
	public double getMaxVertAcceleration() {
		// TODO Auto-generated method stub
		return 0.0005D;
	}

	@Override
	public float getZoomDist() {
		// TODO Auto-generated method stub
		return 8;
	}

	@Override
	public VehicleType getVehicleType() {
		// TODO Auto-generated method stub
		return VehicleType.submarine;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!this.isInWater()) {
			tickOutOfWater++;
			if(tickOutOfWater >= 6)
				this.motionY -= 0.005;
		}
		else
			tickOutOfWater = 0;
	}

	@Override 
	public void onEntityUpdate() {
		super.onEntityUpdate();

		if(this.getSpeed() > (this.getMaxVelocity()*3/4) && worldObj.isRemote)
		{
			float offsetX, offsetZ, yawRad =  (float)(Math.PI/180.0F) * this.rotationYaw;

			float randA = (this.rand.nextFloat()/2.0f) - 0.25f, randB = this.rand.nextFloat();

			offsetZ = MathHelper.sin(yawRad + randA) * 2.5F;
			offsetX = MathHelper.cos(yawRad + randA) * 2.5F;

			this.worldObj.spawnParticle("bubble",this.posX + offsetX, this.posY + randB, this.posZ + offsetZ,0,0,0);

			//offsetZ = MathHelper.sin(yawRad - 0.391699939F) * 4.976946855F;
			//offsetX = MathHelper.cos(yawRad - 0.391699939F) * 4.976946855F;

			//this.worldObj.spawnParticle("largesmoke",this.posX + offsetX, this.posY + 0.2F, this.posZ + offsetZ,0,0,0);
		}

		if(this.riddenByEntity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)this.riddenByEntity;

			if(player.isSneaking() || !this.isInWater()) {
				player.removePotionEffect(16);
				player.removePotionEffect(13);
				player.removePotionEffect(3);
			}
			else {

				//player.setAir(20);
				//if(player.getActivePotionEffects().isEmpty() ) {
				player.addPotionEffect(new PotionEffect(13, 300,255));
				player.addPotionEffect(new PotionEffect(16, 300,255));
				player.addPotionEffect(new PotionEffect(3, 20, 128));
				//}
			}
		}
	}
}
