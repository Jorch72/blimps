package zmaster587.blimps.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TemporaryInventory implements IInventory {
    
	protected ItemStack stack[];
	protected final byte invSize;
	
	public TemporaryInventory() {
		stack = new ItemStack[9];
		invSize = 9;
	}
	
	public TemporaryInventory(int size) {
		stack = new ItemStack[size-1];
		invSize = (byte)size;
	}

	@Override
	public int getSizeInventory() {
		return invSize;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return stack[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		
		if(stack[i] != null && stack[i].stackSize > 0) {
			
			byte amtToRemove = (byte) Math.min(j, stack[i].stackSize);
			stack[i].stackSize -= amtToRemove;
			Item returnItem = stack[i].getItem();
			
			if(stack[i].stackSize == 0)
				stack[i] = null;
			
			return new ItemStack(returnItem, amtToRemove);
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if(stack[i].stackSize > 0)
			return stack[i];
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		stack[i] = itemstack;
		
	}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 1;
	}

	public void markDirty() {
		
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return true;
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
		return false;
	}
}
