package zmaster587.blimps.network;

import io.netty.buffer.ByteBuf;
import zmaster587.blimps.Blimps;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class InventoryPacket extends BasePacket {

	int entityId;
	byte GUIID;
	
	public InventoryPacket() {}

	public InventoryPacket(int entityId,byte GUIID)
	{
		this.entityId = entityId;
	}

	@Override
	public void write(ByteBuf out) {
		out.writeInt(entityId);
		out.writeByte(GUIID);
	}

	@Override
	public void read(ByteBuf in) {
		entityId = in.readInt();
		GUIID = in.readByte();
	}

	
	public void execute(EntityPlayer player, Side side) {
		player.openGui(Blimps.instance, GUIID, player.worldObj, 0,0,0);
	}
	
	@Override
	public void executeServer(EntityPlayerMP player) {
		execute((EntityPlayer)player, Side.SERVER);
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		execute(player, Side.CLIENT);
	}
}
