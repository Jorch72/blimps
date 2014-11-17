package zmaster587.blimps.item;

import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemComponents extends Item {

	public ItemComponents() {
		super();
		setCreativeTab(CreativeTabs.tabTransport);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	private static IIcon[] icons;
	private static String[] names = {"Wooden_Propeller","Iron_Propeller","Steel_Propeller","Gold_Propeller","Diamond_Propeller","Basic_Engine","Jet_Engine","Rocket_Engine", "Submersible_Engine", "Crude_Cockpit"};

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List itemList) {
		for (int i = 0; i < 10; i++) {
			if(i != 7 && i != 6)
				itemList.add(new ItemStack(this, 1, i));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getUnlocalizedName()).append(".").append(itemstack.getItemDamage()).toString();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int i) {
		return i < icons.length ? icons[i] : null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		icons = new IIcon[names.length];
		int i = 0;
		for (String csName : names) {
			icons[i++] = par1IconRegister.registerIcon("blimps:" + csName);
		}
	}

	
	
	public void registerItemStacks() {
		for(int i = 0; i < names.length; i++) {
			GameRegistry.registerCustomItemStack(names[i], new ItemStack(this, 1, i));
			//DEBUG:
			LanguageRegistry.addName(new ItemStack(this,1,i), names[i].replace("_", " "));
		}
	}
}
