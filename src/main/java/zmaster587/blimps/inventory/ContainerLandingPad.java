package zmaster587.blimps.inventory;

import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import zmaster587.blimps.Blimps;
import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.network.BlimpPacketHandler;
import zmaster587.blimps.network.PacketConfigSync;
import zmaster587.blimps.network.PacketMachine;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import zmaster587.libVulpes.gui.SlotSingleItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class ContainerLandingPad extends Container {

	TileEntityLoadingDock pad;
	
	private short lastTimeRemaining;
	private boolean lastFuelInv;
	private boolean lastOutputInv;
	private boolean lastHasContact;

	public ContainerLandingPad(InventoryPlayer inventoryPlayer,
			TileEntityLoadingDock entity) {
		pad = entity;
		addSlotToContainer(new Slot(pad,0,128,33));
		
		if(!pad.getWorldObj().isRemote) {
	
			BlimpPacketHandler.sendToPlayer(new PacketMachine(pad, (byte)100), inventoryPlayer.player);
		}
	}

	@Override
	public void addCraftingToCrafters(ICrafting crafting) {
		super.addCraftingToCrafters(crafting);
		
		crafting.sendProgressBarUpdate(this, 0, this.pad.timeDiff);
		crafting.sendProgressBarUpdate(this, 1, pad.hasFuelInv ? 1 : 0);
		crafting.sendProgressBarUpdate(this, 2, pad.hasOutputInv ? 1 : 0);
		crafting.sendProgressBarUpdate(this, 3, pad.hasContact ? 1 : 0);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		return null;
	}

	//TODO: clean up mess code
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		
		for(int i = 0; i < this.crafters.size(); i++) {
			ICrafting iCrafting = (ICrafting)this.crafters.get(i);
			
			if(this.lastTimeRemaining != pad.timeDiff){
				
				BlimpPacketHandler.sendToPlayer(new PacketMachine(pad, (byte)101), (EntityPlayer) crafters.get(i));
				
				//PacketDispatcher.sendPacketToPlayer(new MultiblockFormPacket(pad, (short)this.pad.timeDiff, (byte)MultiblockFormPacket.Codes.UPADTEPROGRESS.ordinal()).makePacket(), (Player) crafters.get(i));
			}
			if(lastFuelInv != pad.hasFuelInv) {
				iCrafting.sendProgressBarUpdate(this, 0, this.pad.hasFuelInv ? 1 : 0);
			}
			if(lastOutputInv != pad.hasOutputInv) {
				iCrafting.sendProgressBarUpdate(this, 0, this.pad.hasOutputInv ? 1 : 0);
			}
			if(lastHasContact != pad.hasContact) {
				iCrafting.sendProgressBarUpdate(this, 0, this.pad.hasContact ? 1 : 0);
			}
			
			
			this.lastFuelInv = this.pad.hasFuelInv;
			this.lastOutputInv = this.pad.hasOutputInv;
			this.lastHasContact = this.pad.hasContact;
			this.lastTimeRemaining = (short)this.pad.timeDiff;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int par1, int par2) {
		
		if (par1 == 0) {
			pad.timeDiff = par2;
			pad.totalTransitTime = par2 + pad.getWorldObj().getTotalWorldTime();
		}
		else if(par1 == 1) {
			pad.hasFuelInv = par2 == 1;
		}
		else if(par1 == 2) {
			pad.hasOutputInv = par2 == 1;
		}
		else if(par1 == 3) {
			pad.hasContact = par2 == 1;
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return pad.isUseableByPlayer(entityplayer);
	}
}