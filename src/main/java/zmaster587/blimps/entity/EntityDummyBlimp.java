package zmaster587.blimps.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityDummyBlimp extends Entity {

	boolean up;
	int killAltitude;
	
	static public final double speed = 0.2D;
	
	public EntityDummyBlimp(World par1World) {
		super(par1World);
	}
	
	public EntityDummyBlimp(World world, double x, double y, double z, boolean up, int killAlt){
		this(world);
		this.up = up;
		this.setPosition(x, y, z);
		
		killAltitude = killAlt;
	}

	@Override
	public boolean canBePushed() { return false; };
	
	@Override
	public boolean canBeCollidedWith() { return false; }
	
	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdate() {
		
		if((this.up && this.posY > 255D) || (!this.up && this.posY < killAltitude)) {
			this.kill();
		}
		
		setPosition(this.posX, this.posY + (this.up ? speed : -speed), this.posZ);
		
		if(this.worldObj.isRemote)
			this.setVelocity(0, this.up ? speed : -speed, 0);
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub
		
	}
}