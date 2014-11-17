package zmaster587.blimps.inventory;

import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.entity.EntityCargoBlimp;
import zmaster587.blimps.entity.EntityScoutBlimp;
import zmaster587.blimps.entity.EntitySub;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if(ID == 0 && player.isRiding()) {
			if( player.ridingEntity instanceof EntityScoutBlimp) 
				return new ContainerScoutBlimp(player.inventory, (EntityScoutBlimp) player.ridingEntity);
			else if (player.ridingEntity instanceof EntityCargoBlimp) 
				return new ContainerScoutBlimp(player.inventory, (EntityCargoBlimp) player.ridingEntity);
			else if (player.ridingEntity instanceof EntitySub)
				return new ContainerScoutBlimp(player.inventory, (EntitySub) player.ridingEntity);
		}
		else if(ID == 1)
			
			return new ContainerLandingPad(player.inventory, (TileEntityLoadingDock)world.getTileEntity(x, y, z));
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if(ID == 0 && player.isRiding()) {
			if(player.ridingEntity instanceof EntityScoutBlimp)
				return new GuiScoutBlimp(player.inventory, (EntityScoutBlimp) player.ridingEntity);
			else if (player.ridingEntity instanceof EntityCargoBlimp) 
				return new GuiScoutBlimp(player.inventory, (EntityCargoBlimp) player.ridingEntity);
			else if (player.ridingEntity instanceof EntitySub)
				return new GuiScoutBlimp(player.inventory, (EntitySub) player.ridingEntity);
			
		}
		else if(ID == 1)
			return new GuiLandingPad(player.inventory, (TileEntityLoadingDock)world.getTileEntity(x, y, z));
		return null;
	}
}
