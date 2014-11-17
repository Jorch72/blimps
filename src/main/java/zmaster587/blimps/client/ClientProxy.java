package zmaster587.blimps.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import zmaster587.blimps.Blimps;
import zmaster587.blimps.CommonProxy;
import zmaster587.blimps.client.render.*;
import zmaster587.blimps.entity.EntityAirlifted;
import zmaster587.blimps.entity.EntityDummyBlimp;
import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.entity.EntityCargoBlimp;
import zmaster587.blimps.entity.EntityScoutBlimp;
import zmaster587.blimps.entity.EntitySub;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerKeyBinds() {
		KeyBindings.init();
		FMLCommonHandler.instance().bus().register(new KeyBindings());
	}

	@Override
	public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityScoutBlimp.class, new RendererScoutBlimp(new ModelScoutBlimp(), 2.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntityCargoBlimp.class, new RendererCargoBlimp(new ModelCargoBlimp(), 2.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntitySub.class, new RendererScoutBlimp(new ModelSubmarine(), 0.5f));
		RenderingRegistry.registerEntityRenderingHandler(EntityDummyBlimp.class, new RendererScoutBlimp(new ModelCargoBlimp(), 2.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntityAirlifted.class, new RendererCrate());
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLoadingDock.class, new RendererMultiBlock());
	}
}
