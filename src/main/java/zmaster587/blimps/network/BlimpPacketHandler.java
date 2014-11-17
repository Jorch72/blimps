package zmaster587.blimps.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.logging.Logger;

import zmaster587.blimps.Blimps;
import zmaster587.blimps.network.BasePacket;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Sharable
public class BlimpPacketHandler {


	public static final EnumMap<Side, FMLEmbeddedChannel> channels = Maps.newEnumMap(Side.class);


	public static void init() {


		if (!channels.isEmpty()) // avoid duplicate inits..
			return;

		Codec codec = new Codec();

		codec.addDiscriminator(0, BlimpMovementPacket.class);
		codec.addDiscriminator(1, InventoryPacket.class);
		codec.addDiscriminator(2, PacketConfigSync.class);
		codec.addDiscriminator(3, PacketEntity.class);
		codec.addDiscriminator(4, PacketMachine.class);
		codec.addDiscriminator(5, PickupEntityPacket.class);
		codec.addDiscriminator(6, ServerCommandPacket.class);

		channels.putAll(NetworkRegistry.INSTANCE.newChannel(Blimps.MOD_CHANNEL, codec, new HandlerServer()));

		// add handlers
		if (FMLCommonHandler.instance().getSide().isClient())
		{
			// for the client
			FMLEmbeddedChannel channel = channels.get(Side.CLIENT);
			String codecName = channel.findChannelHandlerNameForType(Codec.class);
			channel.pipeline().addAfter(codecName, "ClientHandler", new HandlerClient());
		}
	}

	public static final void sentToServer(BasePacket packet) {
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeOutbound(packet);
	}

	public static final void sendToPlayer(BasePacket packet, EntityPlayer player) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		channels.get(Side.SERVER).writeOutbound(packet);
	}

	public static final void sentToNearby(BasePacket packet,int dimId, int x, int y, int z, double dist) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(new NetworkRegistry.TargetPoint(dimId, x, y, z,dist));
		channels.get(Side.SERVER).writeOutbound(packet);
	}

	private static final class Codec extends FMLIndexedMessageToMessageCodec<BasePacket> {

		//public static final BlimpPacketHandler INSTANCE = new BlimpPacketHandler();

		/*@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		try {
			EntityPlayer entityPlayer = (EntityPlayer)player;
            ByteArrayDataInput in = ByteStreams.newDataInput(packet.data);
            int packetId = in.readUnsignedByte(); // Assuming your packetId is between 0 (inclusive) and 256 (exclusive). If you need more you need to change this
            BasePacket basePacket = BasePacket.constructPacket(packetId);
            basePacket.read(in, player);
            basePacket.execute(entityPlayer, entityPlayer.worldObj.isRemote ? Side.CLIENT : Side.SERVER);
		} catch (ProtocolException e) {
			if(player instanceof EntityPlayerMP) {
				((EntityPlayerMP)player).playerNetServerHandler.kickPlayerFromServer("Protocol Exception");
				Logger.getLogger("blimps").warning("Player " + ((EntityPlayer)player).username + " caused a protocol Exception!");
			}
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Reflection exception during Packet construction!", e);
		}

	}*/

		@Override
		public void encodeInto(ChannelHandlerContext ctx, BasePacket msg,
				ByteBuf data) throws Exception {
			msg.write(data);
		}

		@Override
		public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, BasePacket packet) {


			switch (FMLCommonHandler.instance().getEffectiveSide()) {
			case CLIENT:
				packet.readClient(data);
				//packet.executeClient((EntityPlayer)Minecraft.getMinecraft().thePlayer);
				break;
			case SERVER:
				INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
				packet.read(data);
				//packet.executeServer(((NetHandlerPlayServer) netHandler).playerEntity);
				break;
			}

		}
	}

	@Sharable
	@SideOnly(Side.CLIENT)
	private static final class HandlerClient extends SimpleChannelInboundHandler<BasePacket>
	{
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, BasePacket packet) throws Exception
		{
			Minecraft mc = Minecraft.getMinecraft();
			packet.executeClient(mc.thePlayer); //actionClient(mc.theWorld, );
		}
	}
	@Sharable
	private static final class HandlerServer extends SimpleChannelInboundHandler<BasePacket>
	{
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, BasePacket packet) throws Exception
		{
			if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			{
				// nothing on the client thread
				return;
			}
			EntityPlayerMP player = ((NetHandlerPlayServer) ctx.channel().attr(NetworkRegistry.NET_HANDLER).get()).playerEntity;
			packet.executeServer(player); //(player.worldObj, player);
		}
	}
}
