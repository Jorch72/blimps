package zmaster587.blimps.network;

import io.netty.buffer.ByteBuf;

import java.net.ProtocolException;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BasePacket {
	public static final String CHANNEL = "blimps";
	/*private static final BiMap<Integer,Class<? extends BasePacket>> idMap;

	static {
		ImmutableBiMap.Builder<Integer, Class<? extends BasePacket>> builder = ImmutableBiMap.builder();
		builder.put(Integer.valueOf(0), BlimpMovementPacket.class);
		builder.put(Integer.valueOf(1), InventoryPacket.class);
		builder.put(Integer.valueOf(2), ServerCommandPacket.class);
		builder.put(Integer.valueOf(3), PickupEntityPacket.class);
		builder.put(Integer.valueOf(4), PacketMachine.class);
		builder.put(Integer.valueOf(5), PacketEntity.class);
		builder.put(Integer.valueOf(6), PacketConfigSync.class);
		
		idMap = builder.build();
	}

	public static BasePacket constructPacket(int packetId) throws ProtocolException, InstantiationException, IllegalAccessException {
		Class<? extends BasePacket> clazz = idMap.get(Integer.valueOf(packetId));
		if(clazz == null){
			throw new ProtocolException("Protocol Exception!  Unknown Packet Id!");
		} else {
			return clazz.newInstance();
		}
	}
	

    public static class ProtocolException extends Exception {

            public ProtocolException() {
            }

            public ProtocolException(String message, Throwable cause) {
                    super(message, cause);
            }

            public ProtocolException(String message) {
                    super(message);
            }

            public ProtocolException(Throwable cause) {
                    super(cause);
            }
    }

	public final int getPacketId() {
		if(idMap.inverse().containsKey(getClass())) {
			return idMap.inverse().get(getClass()).intValue();
		} else {
			throw new RuntimeException("Packet " + getClass().getSimpleName() + " is a missing mapping!");
		}
	}*/

	public abstract void write(ByteBuf out);

	public void readClient(ByteBuf in) {
		read(in);
	}
	
	public abstract void read(ByteBuf in);

	@SideOnly(Side.CLIENT)
	public abstract void executeClient(EntityPlayer thePlayer);
	
	public abstract void executeServer(EntityPlayerMP player);
	
}
