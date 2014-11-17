package zmaster587.blimps.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

public class BlockPadEntityLoader extends Block {
	
	IIcon textureWest;
	
	public BlockPadEntityLoader() {
		super(Material.rock);
		setCreativeTab(CreativeTabs.tabTransport).setHarvestLevel("pickaxe", 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if(ForgeDirection.getOrientation(side) == ForgeDirection.WEST)
			return this.textureWest;
		
		return this.blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconReg) {
		super.registerBlockIcons(iconReg);
		this.textureWest = iconReg.registerIcon("blimps:sideEntityPad");
	}
}
