package zmaster587.blimps.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;

public class BlockUpgradeStation extends Block {

	public BlockUpgradeStation(int par1, Material par2Material) {
		super( Material.iron);
		this.setCreativeTab(CreativeTabs.tabTransport).setHarvestLevel("pickaxe", 0);
	}

}
