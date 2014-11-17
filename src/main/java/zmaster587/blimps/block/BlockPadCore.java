package zmaster587.blimps.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import zmaster587.blimps.Blimps;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import zmaster587.libVulpes.tile.IMultiblock;
import zmaster587.libVulpes.tile.TileEntityPointer;
import zmaster587.libVulpes.util.INetworkMachine;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

//MetaData

public class BlockPadCore extends Block {

	int[] padSize = {3,1,3};

	private IIcon textureTop;

	public BlockPadCore() {
		super(Material.rock);
		setCreativeTab(CreativeTabs.tabTransport).setHarvestLevel("pickaxe", 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if(ForgeDirection.getOrientation(side) == ForgeDirection.UP)
			return this.textureTop;

		return this.blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconReg) {
		super.registerBlockIcons(iconReg);
		this.textureTop = iconReg.registerIcon("blimps:masterPadTop");
	}

	/*@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int par5) {
		//if(world.isRemote)
		//return;

		TileEntityLoadingDock tile = (TileEntityLoadingDock) world.getBlockTileEntity(x, y, z);
		if(!tile.isComplete()) {
			tile.attemptUnite();
		}
	}*/

	@Override
	public boolean removedByPlayer(World world,EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		IMultiblock tile = (IMultiblock)world.getTileEntity(x, y, z);
		if(tile.isComplete()) {
			//int initPosX = tile.Xcoord, initPosY = tile.primary_y, initPosZ= tile.primary_z;

			TileEntityLoadingDock e = (TileEntityLoadingDock)tile.getMasterBlock();

			if( e != null && e.isComplete()) {
				e.breakMultiStructure(world);
				//PacketDispatcher.sendPacketToAllAround(x, y, z, 64, world.provider.dimensionId, new MultiblockFormPacket(e, false).makePacket());
			}
		}
		return super.removedByPlayer(world, player, x, y, z, willHarvest);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par5, float par7, float par8, float par9)
	{
		TileEntity e = world.getTileEntity(x, y, z);
		TileEntityLoadingDock pad = null;


		if(!(e instanceof TileEntityLoadingDock) || player.isSneaking()) {
			//TODO: print error
			return false;
		}

		pad = (TileEntityLoadingDock)e;

		if(pad != null && !pad.isComplete()) {
			pad.attemptComplete();
			return false;
		}


		if(pad != null)
		{
			if(world.isRemote) {
				world.markBlockForUpdate(x, y, z);
				pad.hasFuelInv = pad.getInventoryAtFuel() != null;
				pad.hasOutputInv = pad.getInventoryAtOutput() != null;
				pad.hasEntityInput = world.getBlock(pad.xCoord - 2, pad.yCoord, pad.zCoord + 1) == Blimps.blockEntityIOBus && world.getBlockMetadata(pad.xCoord - 2, pad.yCoord, pad.zCoord + 1) == 0;
				pad.hasEntityOutput = world.getBlock(pad.xCoord - 2, pad.yCoord, pad.zCoord - 1) == Blimps.blockEntityIOBus && world.getBlockMetadata(pad.xCoord - 2, pad.yCoord, pad.zCoord - 1) == 0;
			}
			else {
				pad.hasContact = pad.hasContactWithRemote();  //TODO: move to a place where the contact may be changed	
				player.openGui(Blimps.instance, 1, world, pad.xCoord, pad.yCoord, pad.zCoord);
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess iblockaccess, int x, int y, int z, int l){

		ForgeDirection dir = ForgeDirection.getOrientation(l);

		TileEntity ent =  iblockaccess.getTileEntity(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);

		if(ent instanceof IMultiblock)
			return !((TileEntityLoadingDock)ent).isComplete();

		return true;
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return true; //meta == 1 || meta == 0;
	}

	@Override 
	public boolean isOpaqueCube() {return false;}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityLoadingDock(padSize);
	}
}