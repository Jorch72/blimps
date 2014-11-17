package zmaster587.blimps.network;

import io.netty.buffer.ByteBuf;
import zmaster587.blimps.Blimps;
import zmaster587.blimps.entity.ICargo;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class PickupEntityPacket extends BasePacket {

	int entityId;
	int entityIdToMount;
	boolean mount;

	public PickupEntityPacket() { }

	public PickupEntityPacket(Entity current, Entity entityToMount)
	{
		this(current);
		this.entityIdToMount = entityToMount.getEntityId();
		mount = true;
	}

	public PickupEntityPacket(Entity current)
	{
		mount = false;
		entityId = current.getEntityId();
	}

	@Override
	public void write(ByteBuf out) {
		out.writeInt(entityId);
		out.writeBoolean(mount);
		out.writeInt(entityIdToMount);

	}

	@Override
	public void read(ByteBuf in){
		entityId = in.readInt();
		mount = in.readBoolean();

		if(mount)
			entityIdToMount = in.readInt();
	}

	//TODO: add cargo slots

	public void execute(EntityPlayer player, Side side) {

		if(side == side.CLIENT)
		{
			Entity e = player.worldObj.getEntityByID(entityId);

			if(!(e instanceof ICargo))
				return;

			if(mount) {
				Entity f = player.worldObj.getEntityByID(entityIdToMount);

				if(f != null)
					((ICargo)e).mountCargo(f);
					
			}
			else
				((ICargo)e).unmountCargo(0);

		}
	}
	
	@Override
	public void executeServer(EntityPlayerMP player) {
		execute((EntityPlayer)player, Side.SERVER);
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		execute((EntityPlayer)player, Side.CLIENT);
	}

}
