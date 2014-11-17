package zmaster587.blimps.inventory;

import org.lwjgl.Sys;

import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;
import zmaster587.blimps.Blimps;
import zmaster587.blimps.network.BlimpPacketHandler;
import zmaster587.blimps.network.PacketMachine;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GuiLandingPad extends GuiContainer {

	TileEntityLoadingDock pad;
	GuiButton modeButton;

	private ResourceLocation backdrop = new ResourceLocation("blimps","textures/gui/ControlPanelBlimp.png");

	public GuiLandingPad(InventoryPlayer inventoryPlayer, TileEntityLoadingDock entity) {
		super(new ContainerLandingPad(inventoryPlayer, entity));
		pad = entity;
	}


	@Override
	public void initGui() {
		super.initGui();
		int x = (width - xSize) / 2, y = (height - ySize) / 2;

		this.buttonList.add(new GuiButton(0, x + 32,y + 38,24,20, "GO!"));

		String buttonText;

		buttonText = getModeName(pad.mode);
		modeButton = new GuiButton(1, x + 18, y + 84, 138,20, buttonText);

		this.buttonList.add(modeButton);
	}

	private String getModeName(int mode) {

		String buttonText;

		if(mode == 0)
			buttonText = "Manual";
		else if(mode == 1)
			buttonText = "Automatic";
		else if(mode == 2)
			buttonText = "Automatic(Stack)";
		else if(mode == 3)
			buttonText = "Automatic(full)";
		else if(mode == 4)
			buttonText = "Automatic(full:stacks)";
		else if(mode == 5)
			buttonText = "Entities (manual)";
		else if(mode == 6)
			buttonText = "Entities (auto)";
		else
			buttonText = "BROKEN!";
		return buttonText;
	}

	@Override
	protected void actionPerformed(GuiButton guiButton) {		
		if(guiButton.id == 0) {
			BlimpPacketHandler.sentToServer(new PacketMachine(pad, (byte)1));
			//PacketDispatcher.sendPacketToServer(new MultiblockFormPacket(pad, false, (byte)MultiblockFormPacket.Codes.SENDPACKAGE.ordinal()).makePacket());
		}
		else if(guiButton.id == 1) {			
			if(pad.mode >= 6 || (!pad.hasEntityInput && pad.mode >= 4))
				pad.mode = 0;
			else 
				pad.mode++;
			modeButton.displayString = getModeName(pad.mode);
			
			BlimpPacketHandler.sentToServer(new PacketMachine(pad, (byte)0));
			//NBTTagCompound nbt = new NBTTagCompound();
			//nbt.setByte("mode", pad.mode);
			//pad.writeToNBT(nbt);
			//PacketDispatcher.sendPacketToServer(pad.getDescriptionPacket());
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		int x = (width - xSize) / 2, y = (height - ySize) / 2;
		fontRendererObj.drawString("Recieving",x + 124, 110 + y, 538976288);
		fontRendererObj.drawString("chest:",x + 124, 118 + y, 538976288);

		if(pad.isTransporting()) {
			double timeRemaining = (pad.totalTransitTime - pad.getWorldObj().getTotalWorldTime())/20.0D;
			int minutes = (int)(timeRemaining / 60);
			byte seconds = (byte)(timeRemaining % 60);
			fontRendererObj.drawString(minutes + " min. " + seconds + " sec.",x + 156, y + 6, 538976288);
		}
		else
		{
			fontRendererObj.drawString("--/--",x + 162, y + 6, 538976288);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		this.mc.renderEngine.bindTexture(backdrop);

		int x = (width - xSize) / 2, y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, 228, 170);

		//Time bar indicator
		if(pad.isTransporting()) {
			int i1 = pad.getScaledTimeRemaining(48);

			this.drawTexturedModalRect(x + 166, y + 24, 14, 171, 48 - i1, 12);
		}

		//Fuel status
		if(!pad.hasFuelInv) {
			this.drawTexturedModalRect(x + 74, y + 136, 0,171 ,14, 14);
		}

		// output inv status
		if(!pad.hasOutputInv) {
			this.drawTexturedModalRect(x + 140, y + 136, 0,171 ,14, 14);
		}

		// remote contact
		if(!pad.hasContact) {
			this.drawTexturedModalRect(x + 12, y + 136, 0,171 ,14, 14);
		}

		if(pad.hasEntityInput || pad.hasEntityOutput) {
			this.drawTexturedModalRect(x + 150, y + 39, 0, 185, 78, 40);

			if(!pad.hasEntityInput)
				this.drawTexturedModalRect(x + 164, y + 52, 62, 171, 14, 14);
			if(!pad.hasEntityOutput)
				this.drawTexturedModalRect(x + 197, y + 52, 62, 171, 14, 14);
		}
	}
}