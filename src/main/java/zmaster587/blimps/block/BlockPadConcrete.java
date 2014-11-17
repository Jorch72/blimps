package zmaster587.blimps.block;

import zmaster587.blimps.Blimps;
import zmaster587.blimps.network.PacketMachine;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import zmaster587.libVulpes.tile.IMultiblock;
import zmaster587.libVulpes.tile.TileEntityPointer;
import zmaster587.libVulpes.util.INetworkMachine;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

public class BlockPadConcrete extends Block {

	public BlockPadConcrete() {
		super(Material.rock);
		setCreativeTab(CreativeTabs.tabTransport).setHarvestLevel("pickaxe", 0);
	}

	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block par5) { 
		TileEntity e = world.getTileEntity(x, y, z);
		TileEntity me = e;
		
		if(((IMultiblock)e).isComplete())
			return;
		
		for(int deltaZ = -1; deltaZ <= 1; deltaZ++) {
			for(int deltaX = -1; deltaX <= 1; deltaX++) {

				e = world.getTileEntity(x + deltaX, y, z + deltaZ);
				Block tmp = world.getBlock(x + deltaX, y, z + deltaZ);

				if(tmp == Blimps.instance.blockZepplinPad && ((IMultiblock)e).isComplete()) {
					((TileEntityLoadingDock)world.getTileEntity(x + deltaX, y, z + deltaZ)).completeMultiStructure(e.getWorldObj());
					
					return;
				}
			}
		}
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityPointer();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par5, float par7, float par8, float par9)
	{
		IMultiblock multi = (IMultiblock) world.getTileEntity(x,y, z);
		
		if(!multi.isComplete() || player.isSneaking())
		{
			return false;
		}
		
		TileEntityLoadingDock pad = (TileEntityLoadingDock)multi.getMasterBlock();
		
		if(pad != null)
		{
			if(world.isRemote) {
				world.markBlockForUpdate(x, y, z);
				pad.hasFuelInv = pad.getInventoryAtFuel() != null;
				pad.hasOutputInv = pad.getInventoryAtOutput() != null;
				pad.hasEntityInput = pad.hasEntityInput(pad.getWorldObj());
				pad.hasEntityOutput = pad.hasEntityOutput(pad.getWorldObj());
				
			}
			player.openGui(Blimps.instance, 1, world, pad.xCoord, pad.yCoord, pad.zCoord);
		}
		else return false;
		
		return true;
	}
	
	@Override
	public boolean removedByPlayer(World world,EntityPlayer player, int x, int y, int z) {
		IMultiblock tile = (IMultiblock)world.getTileEntity(x, y, z);
		if(tile.isComplete()) {
			//int initPosX = tile.Xcoord, initPosY = tile.primary_y, initPosZ= tile.primary_z;

			TileEntityLoadingDock e = (TileEntityLoadingDock)tile.getMasterBlock();

			if( e != null && e.isComplete()) {
				e.breakMultiStructure(world);
				//PacketDispatcher.sendPacketToAllAround(x, y, z, 64, world.provider.dimensionId, new MultiblockFormPacket(e, false).makePacket());
			}
		}
		return super.removedByPlayer(world, player, x, y, z);
	}
	
	@Override
	public boolean hasTileEntity(int meta) {
		return true; //meta == 1 || meta == 0;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess iblockaccess, int x, int y, int z, int l){

		net.minecraftforge.common.util.ForgeDirection dir = ForgeDirection.getOrientation(l);

		TileEntity ent =  iblockaccess.getTileEntity(x - dir.offsetX,y - dir.offsetY,z - dir.offsetZ);
		
		if(ent instanceof IMultiblock)
			return !((IMultiblock)ent).isComplete();

		return true;
	}
	
	@Override 
	public boolean isOpaqueCube() {return false;}
}