package zmaster587.blimps.block;

import java.util.ArrayList;

import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.entity.EntitySub;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

public class BlockSubLock extends Block {

	private static final short searchDist = 4;
	
	public BlockSubLock() {
		super(Material.iron);
		setHarvestLevel("pickaxe", 0);
	}

	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par5, float par7, float par8, float par9) {
		
		//TileEntityVehicleDock e = (TileEntityVehicleDock)world.getBlockTileEntity(x, y, z);
		EntityFlyingVehicle veh;
		
		if(player.ridingEntity instanceof EntityFlyingVehicle) {
			
			//Get the direction
			ForgeDirection dir = ForgeDirection.getOrientation(par5);
			if(!isVehicleBound(world,x,y,z) && ((EntityFlyingVehicle)player.ridingEntity).canDock(dir)) {


				//Pass direction so we can position the sub correctly
				((EntityFlyingVehicle)player.ridingEntity).setDocked(player, dir, x, y, z);
				
				dir = dir.getOpposite();

				// Because if we dismount then players move to the wrong location
				player.mountEntity(null);
	            //player.ridingEntity.riddenByEntity = null;
	            //player.ridingEntity = null;
				
				player.setPosition(x + (2*dir.offsetX), y + dir.offsetY, z + (2*dir.offsetZ));
				
				return true;
			}
			else return false;
		}
		else if(player.ridingEntity == null && (veh = getBountVeh(world,x,y,z)) != null) {

			if(veh != null) {
				player.mountEntity(veh);
				veh.undock();
				return true;
			}
		}
		return false;
	}
	
	private boolean isVehicleBound(World world, int x, int y, int z) {
		
		return getBountVeh(world,  x,  y,  z) != null;
	}
	
	private EntityFlyingVehicle getBountVeh(World world, int x, int y, int z) {
		ArrayList<EntityFlyingVehicle> ent = (ArrayList<EntityFlyingVehicle>) world.getEntitiesWithinAABB(EntityFlyingVehicle.class, AxisAlignedBB.getBoundingBox(x - searchDist, y - searchDist, z - searchDist, x  + searchDist, y + searchDist, z + searchDist));
		
		for(EntityFlyingVehicle e : ent) {
			if(e.dockedX == x && e.dockedY == y && e.dockedZ == z)
				return e;
		}
		return null;
	}
	
	@Override
	public boolean hasTileEntity(int meta) {
		return false; //meta == 1 || meta == 0;
	}
}
