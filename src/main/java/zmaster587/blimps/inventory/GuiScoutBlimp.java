package zmaster587.blimps.inventory;

import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.entity.EntityScoutBlimp;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class GuiScoutBlimp extends GuiContainer {
	
	private ResourceLocation backdrop = new ResourceLocation("blimps","textures/gui/ScoutBlimp.png");
	
	EntityFlyingVehicle blimp;
	
	public GuiScoutBlimp(InventoryPlayer inventoryPlayer,
			EntityFlyingVehicle entity)
	{
		super(new ContainerScoutBlimp(inventoryPlayer, entity));
		
		blimp = entity;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		super.drawGuiContainerForegroundLayer(par1, par2);
		this.drawString(fontRendererObj, blimp.getInventoryName(), 24, 4, 0xffffff);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		this.mc.renderEngine.bindTexture(backdrop);
		
		int x = (width - xSize) / 2, y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, 176, 170);
		
		int i1 = blimp.getScaledBurnTimeRemaining(14);
		
		if(blimp.isBurningFuel())
			this.drawTexturedModalRect(x + 67, y + 34 + 12 - i1, 177, 14 - i1, 14, i1 + 2);
		
	}
}
