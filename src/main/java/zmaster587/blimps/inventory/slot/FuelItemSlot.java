package zmaster587.blimps.inventory.slot;

import cpw.mods.fml.common.IFuelHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class FuelItemSlot extends Slot {

	public FuelItemSlot(IInventory par1iInventory, int par2, int par3, int par4){
		super(par1iInventory, par2, par3, par4);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return TileEntityFurnace.isItemFuel(stack);
	}
}
