package zmaster587.blimps.network;

import io.netty.buffer.ByteBuf;
import zmaster587.libVulpes.util.INetworkMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketMachine extends BasePacket {

	INetworkMachine machine;
	
	NBTTagCompound nbt;
	
	byte packetId;
	
	public PacketMachine() {
		nbt = new NBTTagCompound();
	};
	
	public PacketMachine(INetworkMachine machine, byte packetId) {
		this();
		this.machine = machine;
		this.packetId = packetId;
	}
	
	
	@Override
	public void write(ByteBuf out) {
		out.writeInt(machine.getWorldId());
		out.writeInt(machine.getXCoord());
		out.writeInt(machine.getYCoord());
		out.writeInt(machine.getZCoord());
		out.writeByte(packetId);
		
		machine.writeDataToNetwork(out, packetId);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void readClient(ByteBuf in) {
		//DEBUG:
		in.readInt();
		
		World world = Minecraft.getMinecraft().theWorld;
		int x = in.readInt();
		int y = in.readInt();
		int z = in.readInt();
		packetId = in.readByte();
		
		TileEntity ent = world.getTileEntity(x, y, z);
		
		if(ent != null && ent instanceof INetworkMachine) {
			machine = (INetworkMachine)ent;
			machine.readDataFromNetwork(in, packetId, nbt);
		}
		else {
			//Error
		}
	}
	
	@Override
	public void read(ByteBuf in) {
		//DEBUG:
		World world = DimensionManager.getWorld(in.readInt());
		int x = in.readInt();
		int y = in.readInt();
		int z = in.readInt();
		packetId = in.readByte();
		
		TileEntity ent = world.getTileEntity(x, y, z);
		
		if(ent != null && ent instanceof INetworkMachine) {
			machine = (INetworkMachine)ent;
			machine.readDataFromNetwork(in, packetId, nbt);
		}
		else {
			//Error
		}
	}

	public void execute(EntityPlayer player, Side side) {
		machine.useNetworkData(player, side, packetId, nbt);
	}

	@Override
	public void executeServer(EntityPlayerMP player) {
		execute((EntityPlayer)player, Side.SERVER);
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		//execute((EntityPlayer)player, Side.CLIENT);
	}
}
