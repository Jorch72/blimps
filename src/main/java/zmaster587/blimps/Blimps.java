package zmaster587.blimps;


import java.util.EnumMap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import zmaster587.blimps.block.BlockPadConcrete;
import zmaster587.blimps.block.BlockPadCore;
import zmaster587.blimps.block.BlockPadEntityLoader;
import zmaster587.blimps.block.BlockSubLock;
import zmaster587.blimps.client.KeyBindings;
import zmaster587.blimps.entity.EntityAirlifted;
import zmaster587.blimps.entity.EntityCargoBlimp;
import zmaster587.blimps.entity.EntityDummyBlimp;
import zmaster587.blimps.entity.EntityScoutBlimp;
import zmaster587.blimps.entity.EntitySub;
import zmaster587.blimps.event.EventHandlerLogin;
import zmaster587.blimps.inventory.GuiHandler;
import zmaster587.blimps.item.ItemBlimpCargo;
import zmaster587.blimps.item.ItemBlimpScout;
import zmaster587.blimps.item.ItemComponents;
import zmaster587.blimps.item.ItemLinker;
import zmaster587.blimps.item.ItemSubmarine;
import zmaster587.blimps.network.BasePacket;
import zmaster587.blimps.network.BlimpPacketHandler;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import zmaster587.libVulpes.tile.TileEntityPointer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid="blimps", name="blimps", version="0.0.9")
public class Blimps {

	public static final String MOD_ID = "blimps";
	public static final String MOD_CHANNEL = MOD_ID;
	public static Item itemBlimpScout;
	public static Item itemBlimpCargo;
	public static Item itemSubmarine;
	public static Item itemLinker;
	public static ItemComponents itemComponents;
	
	public static Block blockZepplinPad;
	public static Block blockPadConcrete;
	public static Block blockEntityIOBus;
	public static Block blockDockingBay;
	
	@SidedProxy(clientSide="zmaster587.blimps.client.ClientProxy", serverSide="zmaster587.blimps.CommonProxy")
	public static CommonProxy proxy;

	@Instance(value = "blimps")
	public static Blimps instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		net.minecraftforge.common.config.Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();
		
		proxy.registerKeyBinds();
		BlimpPacketHandler.init();

		EntityCargoBlimp.maxVelocity = config.get(Configuration.CATEGORY_GENERAL, "Cargo_MaxSpeed", 0.3D).getDouble(0.3D);
		EntityCargoBlimp.maxHeight = (float)config.get(Configuration.CATEGORY_GENERAL, "Cargo_MaxHeight", 255F).getDouble(255f);
		EntityScoutBlimp.maxVelocity = config.get(Configuration.CATEGORY_GENERAL, "Scout_MaxSpeed", 0.75D).getDouble(0.75D);
		EntityScoutBlimp.maxHeight = (float)config.get(Configuration.CATEGORY_GENERAL, "Scout_MaxHeight", 128F).getDouble(128F);
		TileEntityLoadingDock.canDumpToGround = config.get(Configuration.CATEGORY_GENERAL, "TransportPad_CanDumpIfChestNonExistant", false).getBoolean(false);
		TileEntityLoadingDock.deleteItemsOnMissingPad = config.get(Configuration.CATEGORY_GENERAL, "TransportPad_DeleteItemsIfPadMissing", false).getBoolean(false);
		
		itemBlimpScout = new ItemBlimpScout().setUnlocalizedName("blimpScout");
		itemBlimpCargo = new ItemBlimpCargo().setUnlocalizedName("blimpCargo");
		itemLinker = new ItemLinker().setUnlocalizedName("Linker");
		itemSubmarine = new ItemSubmarine().setUnlocalizedName("blimpSub");
		
		itemComponents = new ItemComponents();
		GameRegistry.registerItem(itemComponents, "components");
		itemComponents.setUnlocalizedName("component");
		itemComponents.registerItemStacks();
		
		
		blockZepplinPad = new BlockPadCore().setBlockName("Landing Pad Core").setHardness(0.7F);
		blockPadConcrete = new BlockPadConcrete().setBlockName("Concrete").setHardness(0.7F);
		blockEntityIOBus = new BlockPadEntityLoader().setBlockName("Entity Loader").setHardness(0.7F);
		blockDockingBay = new BlockSubLock().setBlockName("SubLock").setBlockTextureName("blimps:submarineAnchor").setHardness(0.7F);
		
		//ModConfiguration.transportBlimpWhitelist = config.get(Configuration.CATEGORY_GENERAL, "transportBlimpWhitelist", new int[] {54}).getIntList();
		
		config.save();
		EntityRegistry.registerModEntity(EntityScoutBlimp.class, "Scout Blimp", 0, this, 80, 1, true);
		EntityRegistry.registerModEntity(EntityCargoBlimp.class, "Crane Blimp", 1, this, 80, 1, true);
		EntityRegistry.registerModEntity(EntitySub.class, "Submarine", 2, this, 80, 1, true);
		EntityRegistry.registerModEntity(EntityDummyBlimp.class, "DummyBlimp", 3, this, 80, 1, true);
		EntityRegistry.registerModEntity(EntityAirlifted.class, "airLiftedBlock", 4, this, 80, 1, true);
		LanguageRegistry.addName(itemBlimpScout, "Scout blimp in a box");
		LanguageRegistry.addName(itemBlimpCargo, "Crane Blimp in a box");
		LanguageRegistry.addName(itemLinker, "Linker");
		LanguageRegistry.addName(itemSubmarine, "Submarine in a box");
		LanguageRegistry.addName(blockZepplinPad, "Landing pad");
		LanguageRegistry.addName(blockPadConcrete, "Concrete");
		LanguageRegistry.addName(blockEntityIOBus, "Entity Import/Export");
		LanguageRegistry.addName(blockDockingBay, "Docking Bay");
		
		//TickRegistry.registerTickHandler(new ServerTickHandler(EnumSet.of(TickType.PLAYER)), Side.CLIENT);
		
		GameRegistry.registerItem(itemBlimpScout, "Scout Blimp");
		GameRegistry.registerItem(itemBlimpCargo, "Crane Blimp");
		GameRegistry.registerItem(itemLinker, "Linker");
		GameRegistry.registerItem(itemSubmarine, "Sub");
		
		
		GameRegistry.registerBlock(blockZepplinPad, "Landing pad");
		GameRegistry.registerBlock(blockPadConcrete, "concrete");
		GameRegistry.registerBlock(blockEntityIOBus, "EntityPad");
		GameRegistry.registerBlock(blockDockingBay, "dockingBay");
		GameRegistry.registerTileEntity(TileEntityLoadingDock.class, "Loading Dock");
		GameRegistry.registerTileEntity(TileEntityPointer.class,"pointer");
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		
		proxy.registerRenderers();
		
		MinecraftForge.EVENT_BUS.register(new EventHandlerLogin());
		//MinecraftForge.EVENT_BUS.register(new KeyBindings());
		
		//RECIPIES
		GameRegistry.addShapedRecipe(new ItemStack(this.blockPadConcrete), "xxx","xyx","xxx", 'x', Blocks.stone, 'y', Blocks.sand);
		GameRegistry.addShapedRecipe(new ItemStack(this.blockEntityIOBus), "xxx","xyx","xxx", 'x', this.blockPadConcrete, 'y', Blocks.gold_block);
		GameRegistry.addShapedRecipe(new ItemStack(this.blockZepplinPad), "xxx","xyx","xxx", 'x', this.blockPadConcrete, 'y', Blocks.diamond_block);
		GameRegistry.addShapedRecipe(new ItemStack(this.blockDockingBay), "ipi","pgp", "ipi", 'i', Blocks.iron_block, 'p', Blocks.piston, 'g', Blocks.glass);
		GameRegistry.addShapedRecipe(new ItemStack(this.itemSubmarine), " x ", " y ", "z z", 'x', new ItemStack(this.itemComponents,1,9), 'y',Blocks.iron_block, 'z', new ItemStack(this.itemComponents,1,8));
		GameRegistry.addShapedRecipe(new ItemStack(this.itemBlimpScout), " x ", " y ", "uwu", 'x', Blocks.wool, 'y', Items.boat, 'w', new ItemStack(this.itemComponents,1,0), 'u',new ItemStack(this.itemComponents,1,5));
		GameRegistry.addShapedRecipe(new ItemStack(this.itemBlimpCargo), "xxx", "vyv", "uwu", 'x', Blocks.wool, 'y', Items.boat, 'w', new ItemStack(this.itemComponents,1,5), 'u',new ItemStack(this.itemComponents,1,1), 'v', Blocks.chest);
		GameRegistry.addShapedRecipe(new ItemStack(this.itemLinker), "x","y","z", 'x', Items.redstone, 'y', Items.gold_ingot, 'z', Items.iron_ingot);
		
		//Components
		GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,0), " x ", " y ", "x x", 'x', Items.stick, 'y', Blocks.iron_block);
		GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,1), " x ", " y ", "x x", 'x', Items.iron_ingot, 'y', Blocks.iron_block);
		//GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,2), " x ", " y ", "x x", 'x', Item.stick, 'y', Block.blockIron);
		GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,3), " x ", " y ", "x x", 'x', Items.gold_ingot, 'y', Blocks.gold_block);
		GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,4), " x ", " y ", "x x", 'x', Items.diamond, 'y', Blocks.diamond_block);
		GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,5), " x ", "yyy", "yyy", 'x', Blocks.furnace, 'y', Blocks.piston);
		//GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,6), " x ", " y ", "x x", 'x', Item.stick, 'y', Block.blockIron);
		//GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,7), " x ", " y ", "x x", 'x', Item.stick, 'y', Block.blockIron);
		GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,8),"xyy","y  ", "y  ", 'x',new ItemStack(this.itemComponents,1,5),'y', Blocks.glass);
		GameRegistry.addShapedRecipe(new ItemStack(this.itemComponents,1,9), " x ", "xyx", " x ", 'x', Blocks.glass, 'y', Items.iron_ingot);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		blockPadConcrete.setBlockTextureName("blimps:padTile");
		blockZepplinPad.setBlockTextureName("blimps:padTile");
		blockEntityIOBus.setBlockTextureName("blimps:padTile");
		
		itemLinker.setTextureName("blimps:Linker");
		itemBlimpCargo.setTextureName("blimps:constructionKit");
		itemBlimpScout.setTextureName("blimps:constructionKit");
		itemSubmarine.setTextureName("blimps:constructionKit");
		
		
	}
}