package zmaster587.blimps.client;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import zmaster587.blimps.Blimps;
import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.entity.ICargo;
import zmaster587.blimps.network.BlimpPacketHandler;
import zmaster587.blimps.network.InventoryPacket;
import zmaster587.blimps.network.PickupEntityPacket;
import zmaster587.blimps.network.ServerCommandPacket;
import zmaster587.blimps.network.BlimpMovementPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatAllowedCharacters;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyBindings {


	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		final Minecraft minecraft = FMLClientHandler.instance().getClient();
		final EntityPlayerSP player = minecraft.thePlayer;


		//Prevent control when a GUI is open
		if(Minecraft.getMinecraft().currentScreen != null)// && Minecraft.getMinecraft().currentScreen instanceof GuiChat)
			return;
		
		if(upBinding.isPressed() && player.ridingEntity instanceof EntityFlyingVehicle) {

			BlimpPacketHandler.sentToServer(new BlimpMovementPacket((byte)1));

			((EntityFlyingVehicle)player.ridingEntity).setVerticalDir(1);
		}
		else if(downBinding.isPressed() && player.ridingEntity instanceof EntityFlyingVehicle){
			BlimpPacketHandler.sentToServer(new BlimpMovementPacket((byte)-1));
			((EntityFlyingVehicle)player.ridingEntity).setVerticalDir(-1);
		}
		else if(neutral.isPressed() && player.ridingEntity instanceof EntityFlyingVehicle) {
			BlimpPacketHandler.sentToServer(new BlimpMovementPacket((byte)0));
			((EntityFlyingVehicle)player.ridingEntity).setVerticalDir(0);
		}
		else if(cruiseControl.isPressed() && player.ridingEntity instanceof EntityFlyingVehicle) {

			BlimpPacketHandler.sentToServer(new BlimpMovementPacket((byte)2));
			((EntityFlyingVehicle)player.ridingEntity).cruiseControl = !((EntityFlyingVehicle)player.ridingEntity).cruiseControl;
		}
		else if(inv.isPressed() &&  player.isRiding() && player.ridingEntity instanceof EntityFlyingVehicle)
		{
			BlimpPacketHandler.sentToServer(new InventoryPacket(player.ridingEntity.getEntityId(), (byte)0));

			//player.openGui(Blimps.instance, 0, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
		}
		else if(grab.isPressed() && player.isRiding() && player.ridingEntity instanceof ICargo)
		{
			BlimpPacketHandler.sentToServer(new ServerCommandPacket((byte)0));
		}
	}
	
	static KeyBinding upBinding = new KeyBinding("Ascend(Blimp Mod)", Keyboard.KEY_Z, "key.controls." + Blimps.MOD_ID);
	static KeyBinding downBinding = new KeyBinding("Descend(Blimp Mod)", Keyboard.KEY_X, "key.controls." + Blimps.MOD_ID);
	static KeyBinding neutral = new KeyBinding("Hold Altitude(Blimp Mod)",Keyboard.KEY_C, "key.controls." + Blimps.MOD_ID);
	static KeyBinding grab = new KeyBinding("Grab Entity(Blimp Mod)", Keyboard.KEY_G, "key.controls." + Blimps.MOD_ID);
	static KeyBinding inv = new KeyBinding("Open inventory(Blimp Mod)",  Keyboard.KEY_R, "key.controls." + Blimps.MOD_ID);
	static KeyBinding cruiseControl = new KeyBinding("Cruise Control(Blimp Mod)", Keyboard.KEY_UP, "key.controls." + Blimps.MOD_ID);

	public static final void init() {
		KeyBinding[] keyList = {upBinding,downBinding,neutral,inv,grab,cruiseControl};
		//boolean[] repeatings = {false,false,false,false,false,false};

		ClientRegistry.registerKeyBinding(upBinding);
		ClientRegistry.registerKeyBinding(downBinding);
		ClientRegistry.registerKeyBinding(neutral);
		ClientRegistry.registerKeyBinding(grab);
		ClientRegistry.registerKeyBinding(inv);
		ClientRegistry.registerKeyBinding(cruiseControl);
		
		//KeyBindings bindings = new KeyBindings(keyList, repeatings);

		//KeyBindingRegistry.registerKeyBinding(bindings);
	}
}
