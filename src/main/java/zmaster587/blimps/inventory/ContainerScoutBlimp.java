package zmaster587.blimps.inventory;

import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;
import zmaster587.blimps.Blimps;
import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.entity.EntityScoutBlimp;
import zmaster587.blimps.inventory.slot.FuelItemSlot;
import zmaster587.blimps.network.PacketConfigSync;
import zmaster587.blimps.network.PacketEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ContainerScoutBlimp extends Container {

	EntityFlyingVehicle blimp;

    public ContainerScoutBlimp (InventoryPlayer inventoryPlayer, EntityFlyingVehicle entity){
            blimp = (EntityFlyingVehicle)entity;
            //public class FuelHandler implements IFuelHandler {
            
    		addSlotToContainer(new FuelItemSlot(blimp,0,44,30));
    		// Player inventory
    		for (int i1 = 0; i1 < 3; i1++) {
    			for (int l1 = 0; l1 < 9; l1++) {
    				addSlotToContainer(new Slot(inventoryPlayer, l1 + i1 * 9 + 9, 8 + l1 * 18, 89 + i1 * 18));
    			}
    		}

    		// Player hotbar
    		for (int j1 = 0; j1 < 9; j1++) {
    			addSlotToContainer(new Slot(inventoryPlayer, j1, 8 + j1 * 18, 147));
    		}
    }

	/*@Override
	public void addCraftingToCrafters(ICrafting iCrafting) {
		super.addCraftingToCrafters(iCrafting);
		if(!blimp.worldObj.isRemote) {
			
			Blimps.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
			Blimps.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(iCrafting);
			Blimps.channels.get(Side.SERVER).writeOutbound(new PacketEntity(blimp,(byte)0));
		}
	}*/
    
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		if(blimp != null)
			return blimp.isUseableByPlayer(entityplayer);

		return false;
	}
    
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);
		//null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {

			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();
			
			//Fuel only in fuel slot
			if(slot != 0 && !blimp.isItemValidForSlot(slot, stack))
				return null;

			//merges the item into player inventory since its in the tileEntity
			if (slot == 0) {
				if (!this.mergeItemStack(stackInSlot, 1, 35, true)) {
					return null;
				}
			}
			//placing it into the tileEntity is possible since its in the player inventory
			else if (!this.mergeItemStack(stackInSlot, 0, 1, false)) {
				return null;
			}

			if (stackInSlot.stackSize == 0) {
				slotObject.putStack(null);
			} else {
				slotObject.onSlotChanged();
			}

			if (stackInSlot.stackSize == stack.stackSize) {
				return null;
			}
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
}
