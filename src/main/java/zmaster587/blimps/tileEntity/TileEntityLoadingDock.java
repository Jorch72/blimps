package zmaster587.blimps.tileEntity;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import zmaster587.blimps.Blimps;
import zmaster587.blimps.block.BlockPadCore;
import zmaster587.blimps.entity.EntityDummyBlimp;
import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.item.ItemLinker;
import zmaster587.blimps.network.BlimpPacketHandler;
import zmaster587.blimps.network.PacketMachine;
import zmaster587.libVulpes.interfaces.ILinkableTile;
import zmaster587.libVulpes.tile.IMultiblock;
import zmaster587.libVulpes.tile.TileEntityPointer;
import zmaster587.libVulpes.util.INetworkMachine;
import zmaster587.libVulpes.util.ZUtils;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityLoadingDock extends TileEntity implements INetworkMachine, IMultiblock, IInventory, ILinkableTile {

	//X coord of the primary tile
	public int primary_x;
	//Y coord of the primary tile
	public int primary_y;
	//Z coord of the primary tile
	public int primary_z;

	//Total world time of arrival(in ticks)
	public long totalTransitTime;
	//Time in ticks between start send and arrival
	public int timeDiff;

	//X coord of destination tile
	public int dest_x;
	//Y coord of destination tile
	public int dest_y;
	//Z coord of destination tile
	public int dest_z;

	//Pad size
	public static int[] padSize  = {3,1,3};
	private static final int[] busOffset = {-2,0,0};
	public static boolean canDumpToGround;
	public static boolean deleteItemsOnMissingPad;

	public boolean hasFuelInv;
	public boolean hasOutputInv;
	public boolean hasContact;
	public boolean hasEntityInput;
	public boolean hasEntityOutput;

	public NBTTagCompound sendingNBT;
	public Class sendingClass;

	public NBTTagCompound recievingNBT;
	public Class recievingClass;

	//mode: 0 for manual, 1 for automatic(any item), 2 for automatic(any full stack), 3 for automatic(100% full), 4 for auto(100% full all stacks)
	public byte mode;
	private byte xSize, ySize, zSize;
	//ItemStack array that holds the inventory to be sent
	private ItemStack[] outgoingInv;
	//ItemStack array that holds the inventory that arrived
	private ItemStack[] incomingInv;

	boolean isComplete;

	public TileEntityLoadingDock()
	{
		primary_x = 0;
		primary_y = 0;
		primary_z = 0;
		totalTransitTime = -1;
		timeDiff = -1;
		dest_x = 0;
		dest_y = 0;
		dest_z = 0;
		xSize = 0;
		ySize = 0; 
		zSize = 0;
		outgoingInv = new ItemStack[9];
		incomingInv = new ItemStack[9];
		isComplete = false;
		hasFuelInv = false;
		hasContact = false;
		sendingNBT = new NBTTagCompound();
		recievingNBT =  new NBTTagCompound();
		recievingClass = null;
		sendingClass = null;
	}

	public TileEntityLoadingDock(int[] pad) {
		this();
		padSize = pad;
	}

	TileEntityLoadingDock(int xSize, int ySize, int zSize) {
		super();
		this.xSize = (byte)xSize;
		this.ySize = (byte)ySize;
		this.zSize = (byte)zSize;
	}

	//IMachine stuff
	@Override
	public void writeDataToNetwork(ByteBuf out,byte id) {

		if(id == 0) {
			out.writeByte(this.mode);
		}
		else if(id == 100) {			
			out.writeBoolean(this.hasContact);
		}
		else if(id == 101) { //update crap MC won't (MultiblockFormPacket.updateprogress)
			out.writeShort(this.timeDiff);
			out.writeShort((short)(this.totalTransitTime - this.worldObj.getTotalWorldTime()));
			out.writeBoolean(this.hasContact);
		}
	}

	@Override
	public void readDataFromNetwork(ByteBuf in, byte id, NBTTagCompound nbt) {
		//try short 4 send 0 recived

		if(id == 0) {
			nbt.setByte("mode", in.readByte());
		}
		else if(id == 100) {
			nbt.setBoolean("contact", in.readBoolean());
		}
		else if(id == 101) {
			nbt.setShort("timeDiff", in.readShort());
			nbt.setShort("fullTime", in.readShort());
			nbt.setBoolean("contact", in.readBoolean());
		}
	}

	public int getXCoord() { return this.xCoord; }

	public int getYCoord() { return this.yCoord; }

	public int getZCoord() { return this.zCoord; }

	public int getWorldId() { return this.worldObj.provider.dimensionId; }

	public void useNetworkData(EntityPlayer player, Side side, byte id, NBTTagCompound nbt) {

		if(side.isServer()) {
			if(id == 0) {
				this.mode = nbt.getByte("mode");
			}
			else if(id == 1 && hasContactWithRemote()) {		//Used to send manual send button press to server
				if(this.mode <= 4)
					this.startMove();
				else
					this.sendEntity();
			}
		}
		if(side.isClient()) {
			if(id == 100) {
				this.hasContact = nbt.getBoolean("contact");
			}
			else if(id == 101) {
				this.timeDiff = nbt.getShort("timeDiff");
				this.totalTransitTime = this.worldObj.getTotalWorldTime() + nbt.getShort("fullTime");
				this.hasContact = nbt.getBoolean("contact");
			}
			else if(id == 102) { //Complete structure
				this.completeMultiStructure(player.worldObj);
			}
			else if(id == 103) {
				this.breakMultiStructure(player.worldObj);
			}
		}
	}


	public boolean isComplete() {
		return isComplete;
	}

	public boolean isMasterBlock() {

		return this.primary_x == this.xCoord && this.primary_y == this.yCoord && this.primary_z == this.zCoord;
	}

	public boolean isTransporting() {

		return timeDiff != -1;
	}


	public int getTransferTime(double distance) {
		return (int)(distance*0.5F);
	}

	public double getFuelConsumtionMultiplyer(double distance)
	{
		return 1.0D;
	}

	public int getScaledTimeRemaining(int i) {

		return  (int)(i * ( (totalTransitTime - this.worldObj.getTotalWorldTime())/(float)timeDiff));
	}

	public TileEntityLoadingDock getMasterBlock() {
		if(this.isComplete()) {
			TileEntityLoadingDock t = (TileEntityLoadingDock)this.worldObj.getTileEntity(this.primary_x, this.primary_y, this.primary_z);
			return t; 
		}
		else 
			return null;
	}

	public boolean hasContactWithRemote() {
		hasContact = dest_y != 0 && this.worldObj.getChunkFromBlockCoords(this.dest_x, this.dest_z).isChunkLoaded && this.worldObj.getTileEntity(dest_x, dest_y, dest_z) instanceof TileEntityLoadingDock;
		return hasContact;
	}

	public void completeMultiStructure(World world){
		this.setComplete();

		if(!world.isRemote){
			BlimpPacketHandler.sentToNearby(new PacketMachine(this, (byte)102),world.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord,128d);
			
		}

		for(int x = -1; x < padSize[0]-1; x++){
			for(int z = -1; z < padSize[2]-1; z++) {


				IMultiblock ent = (IMultiblock)world.getTileEntity(this.xCoord + x, this.yCoord, this.zCoord + z);
				ent.setComplete(this.xCoord, this.yCoord, this.zCoord);
				if(world.isRemote)
					world.markBlockForUpdate(this.xCoord + x, this.yCoord, this.zCoord + z);
			}
		}
	}

	public void attemptUnite() {

		IMultiblock multi = (IMultiblock) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1);
		if(multi != null && multi.isComplete() && multi.getMasterBlock().equals(this)) {
			this.setComplete();

			return;
		}
	}


	public IInventory getInventoryAtFuel() {
		TileEntity t = this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 2);

		return (t != null && t instanceof IInventory) ? (IInventory)t : null; 
	}

	public IInventory getInventoryAtOutput() {
		TileEntity t = this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 2);

		return (t != null && t instanceof IInventory) ? (IInventory)t : null;
	}

	public IInventory getInventoryAtInput() {
		TileEntity t = this.worldObj.getTileEntity(this.xCoord + 2, this.yCoord, this.zCoord);

		return (t != null && t instanceof IInventory) ? (IInventory)t : null;
	}

	public void setComplete()
	{
		setComplete(this.xCoord, this.yCoord, this.zCoord);
	}

	public void setComplete(int x, int y, int z) {
		this.isComplete = true;
		this.primary_x = x;
		this.primary_y = y;
		this.primary_z = z;
	}

	public void setIncomplete()
	{
		isComplete = false;
		this.primary_x = -1;
		this.primary_y = -1;
		this.primary_z = -1;
	}

	public static boolean canRecieveItem(TileEntityLoadingDock remote)
	{
		for(int i = 0; i < remote.incomingInv.length; i++) {
			if(remote.incomingInv[i] != null)
				return false;
		}
		return true;
	}

	public static boolean canRecieveEntity(TileEntityLoadingDock remote) {
		return remote.recievingNBT.hasNoTags();
	}

	public static boolean hasDestination(TileEntityLoadingDock tile) {
		return tile.dest_y != -1;
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound comp = new NBTTagCompound();

		writeToNBTHelper(comp);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, comp);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {

		if(this.worldObj.isRemote)
		{
			readFromNBTHelper(pkt.func_148857_g());
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(!isComplete() || !isMasterBlock())
			return super.getRenderBoundingBox();
		else {
			return AxisAlignedBB.getBoundingBox(this.xCoord + 2, this.yCoord + 1, this.zCoord + 2, this.xCoord - 2, this.yCoord - 1, this.zCoord - 2);
		}
	}

	private boolean isBackedup() {
		return totalTransitTime == -1 && (!ZUtils.isInvEmpty(outgoingInv) || !this.sendingNBT.hasNoTags());
	}

	@Override
	public void updateEntity() {

		if(this.worldObj.isRemote)
			return;


		if(this.worldObj.getWorldTime() % 100 == 0) {

			if(!ZUtils.isInvEmpty(incomingInv)) {
				IInventory e = this.getInventoryAtOutput();

				if(e != null)
					this.dumpItemsToChest((IInventory)e);
				else if(TileEntityLoadingDock.canDumpToGround) {

					for(ItemStack i : this.outgoingInv) {
						if(i != null){
							EntityItem entityitem = new EntityItem(this.worldObj,this.xCoord, this.yCoord, this.zCoord + 2);
							this.worldObj.spawnEntityInWorld(entityitem);
						}
					}
				}
			}

			if(this.hasEntityOutput(this.worldObj) && !this.recievingNBT.hasNoTags() && this.recievingClass != null) {

				Entity e;


				try {
					e =(Entity)this.recievingClass.getDeclaredConstructor(World.class).newInstance(this.worldObj);  //newInstance();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					this.recievingClass = null;
					this.recievingNBT = new NBTTagCompound();
					return;
				}
				e.readFromNBT(this.recievingNBT);


				e.setPositionAndRotation(this.xCoord - 2.5, this.yCoord + 0.5D, this.zCoord - 0.5D, 270f, 0f);
				e.motionX = -0.2;
				this.worldObj.spawnEntityInWorld(e);
				this.recievingClass = null;
				this.recievingNBT = new NBTTagCompound();
				totalTransitTime = -1;
				timeDiff = -1;
			}
		}
		//Do all the sending stuff
		if(isTransporting()) {

			int timeToFinish = (int)(totalTransitTime - this.worldObj.getTotalWorldTime());

			if(timeToFinish <= (60)/EntityDummyBlimp.speed && (timeToFinish+1) > (60)/EntityDummyBlimp.speed) {
				this.worldObj.spawnEntityInWorld(new EntityDummyBlimp(this.worldObj, this.dest_x, this.dest_y + 60, this.dest_z, false, this.dest_y));
			}

			if(( totalTransitTime - this.worldObj.getTotalWorldTime() < 0) || (this.worldObj.getWorldTime() % 100 == 0 && isBackedup()))
			{

				Chunk destChunk = this.worldObj.getChunkFromBlockCoords(this.dest_x, this.dest_z);
				totalTransitTime = -1;
				timeDiff = -1;

				//if the chunk is loaded attempt to dump outgoing inv to remote incoming inv and reset dest 
				if(!this.worldObj.isRemote) {
					if(destChunk.isChunkLoaded)
					{
						TileEntity remoteEntity = this.worldObj.getTileEntity(dest_x, dest_y, dest_z);

						if(remoteEntity instanceof TileEntityLoadingDock && ((TileEntityLoadingDock)remoteEntity).isComplete()) {
							TileEntityLoadingDock remoteDock = (TileEntityLoadingDock)remoteEntity;

							//Attempt to move entity from point A to point B if one exists, if not must be items
							if(ZUtils.isInvEmpty(this.outgoingInv) && canRecieveEntity(remoteDock)) {
								remoteDock.recievingNBT = this.sendingNBT;
								remoteDock.recievingClass = this.sendingClass;
								this.sendingNBT = new NBTTagCompound();
								this.sendingClass = null;
							}
							else if(canRecieveItem((TileEntityLoadingDock)remoteEntity)) {

								remoteDock.incomingInv = this.outgoingInv;

								for(int i = 0; i < this.outgoingInv.length; i++)
								{
									if(this.outgoingInv[i] != null)
										remoteDock.incomingInv[i] = this.outgoingInv[i].copy();
								}

								this.outgoingInv = new ItemStack[9];
							}
						}
						else {

							if(deleteItemsOnMissingPad) {
								for(int i = 0; i < this.outgoingInv.length; i++) {
									this.outgoingInv[i] = null;
								}
							}

							else {
								int topY = this.worldObj.getTopSolidOrLiquidBlock(dest_x, dest_z);
								for(int i = 0; i < this.outgoingInv.length; i++) {
									if(this.outgoingInv[i] != null) {

										EntityItem entityitem = new EntityItem(this.worldObj,dest_x,topY + 64,dest_z, this.outgoingInv[i]);
										this.worldObj.spawnEntityInWorld(entityitem);
										this.outgoingInv[i] = null;
									}
								}
							}
						}
					}
				}
			}
		}
		else if(!isTransporting() && this.mode != 0 && this.worldObj.getWorldTime() % 100 == 0) {

			if(this.getInventoryAtInput() != null && this.mode <= 4) {
				if(this.mode == 1) {
					if(!ZUtils.isInvEmpty(this.getInventoryAtInput()))
						startMove();
				}
				else if(this.mode == 2) {
					if(ZUtils.hasFullStack(this.getInventoryAtInput()))
						startMove();
				}
				else if(this.mode == 3) {
					if(this.getInventoryAtInput().getSizeInventory() - ZUtils.numEmptySlots(this.getInventoryAtInput()) >= outgoingInv.length)
						startMove();
				}
				else if(this.mode == 4) {
					if(ZUtils.numFilledSlots(this.getInventoryAtInput()) >= outgoingInv.length)
						startMove();
				}
			}
			else if(this.mode > 4 && hasEntityInput(this.worldObj)) {
				if(this.mode == 6) {
					sendEntity();
				}
			}
		}
	}

	/*public void sendEntity(Entity e) {
		if(e != null && canTransportEntity(e) && hasEntityInput(this.worldObj) && hasContactWithRemote()) {
			sendingClass = e.getClass();
			e.writeToNBT(sendingNBT);

			if(e.riddenByEntity != null) {
				NBTTagCompound ridingNBT = new NBTTagCompound();
				e.riddenByEntity.writeToNBT(ridingNBT);

				ridingNBT.setString("CLASSNAME", e.riddenByEntity.getClass().getCanonicalName());

				sendingNBT.setCompoundTag("mount", ridingNBT);

			}

			this.timeDiff = this.getTransferTime(Math.sqrt(this.getDistanceFrom(this.dest_x, this.dest_y, this.dest_z)));
			this.totalTransitTime = this.worldObj.getTotalWorldTime() + this.timeDiff;
			e.setPosition(e.posX, -2, e.posZ);
			this.worldObj.removeEntity(e);
		}
	}*/

	public void sendEntity() {
		if(this.sendingClass != null)
			return;

		Entity e = getEntityAtInput();
		if(e != null && canTransportEntity(e) && hasEntityInput(this.worldObj) && getInventoryAtFuel() != null && hasContactWithRemote()) {

			double distance = Math.sqrt(this.getDistanceFrom(this.dest_x, this.dest_y, this.dest_z));

			this.timeDiff = this.getTransferTime(Math.sqrt(this.getDistanceFrom(this.dest_x, this.dest_y, this.dest_z)));
			int fuelTicks = (int)(this.timeDiff * this.getFuelConsumtionMultiplyer(distance));

			if(!attemptUseFuel(this.getInventoryAtFuel(), fuelTicks)) {
				this.timeDiff = -1;
				return;
			}



			this.totalTransitTime = this.worldObj.getTotalWorldTime() + this.timeDiff;

			sendingClass = e.getClass();

			if(e.riddenByEntity != null) 
				e.riddenByEntity.mountEntity(null);

			e.writeToNBT(sendingNBT);
			e.setPosition(e.posX, -2, e.posZ);
			this.worldObj.removeEntity(e);

			if(this.timeDiff > (60/EntityDummyBlimp.speed))
				this.worldObj.spawnEntityInWorld(new EntityDummyBlimp(this.worldObj, this.xCoord, this.yCoord + 1, this.zCoord,true,255));

		}
	}

	public boolean hasEntityOutput(World world) {
		return world.getBlock(this.xCoord - 2, this.yCoord, this.zCoord - 1) == Blimps.blockEntityIOBus && world.getBlockMetadata(this.xCoord - 2, this.yCoord, this.zCoord - 1) == 0;
	}

	public boolean hasEntityInput(World world) {
		return world.getBlock(this.xCoord - 2, this.yCoord, this.zCoord + 1) == Blimps.blockEntityIOBus && world.getBlockMetadata(this.xCoord - 2, this.yCoord, this.zCoord + 1) == 0;
	}

	public Entity getEntityAtInput() {
		List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(this.xCoord - 3, this.yCoord-1, this.zCoord + 1, this.xCoord - 2, this.yCoord + 1, this.zCoord + 2));
		return list.isEmpty() ? null : list.get(0);
	}

	private boolean dumpItemsToChest(IInventory chest) {
		//Get a chest directly to the south if possible
		//TileEntity e = this.worldObj.getTileEntity(primary_x, this.primary_y,this.primary_z - ((zSize-1)/2) - 1);

		if(chest == null)
			return false;

		ZUtils.mergeInventory(incomingInv, chest);


		return true;
	}


	@Override
	public void writeToNBT(NBTTagCompound nbtCompound) {
		super.writeToNBT(nbtCompound);

		if(this.sendingClass != null) {
			nbtCompound.setString("sendingClass", this.sendingClass.getName());
			nbtCompound.setTag("sendingEntity", sendingNBT);
		}
		if(this.recievingClass != null) {
			nbtCompound.setTag("recievingEntity", recievingNBT);
			nbtCompound.setString("recievingClass", this.recievingClass.getName());
		}


		NBTTagList itemList = new NBTTagList();
		for(int i = 0; i < outgoingInv.length; i++)
		{
			ItemStack stack = outgoingInv[i];
			if(stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		nbtCompound.setTag("InventoryOut", itemList);

		itemList = new NBTTagList();
		for(int i = 0; i < incomingInv.length; i++)
		{
			ItemStack stack = incomingInv[i];
			if(stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		nbtCompound.setTag("InventoryIn", itemList);

		writeToNBTHelper(nbtCompound);
	}

	private void writeToNBTHelper(NBTTagCompound nbtCompound) {
		nbtCompound.setByte("xSize", this.xSize);
		nbtCompound.setByte("ySize", this.ySize);
		nbtCompound.setByte("zSize", this.zSize);

		nbtCompound.setInteger("dest_x", this.dest_x);
		nbtCompound.setInteger("dest_y", this.dest_y);
		nbtCompound.setInteger("dest_z", this.dest_z);

		nbtCompound.setInteger("primaryTileX", this.primary_x);
		nbtCompound.setInteger("primaryTileY", this.primary_y);
		nbtCompound.setInteger("primaryTileZ", this.primary_z);
		nbtCompound.setBoolean("complete", this.isComplete);

		nbtCompound.setByte("mode", this.mode);

		nbtCompound.setLong("transitTimeTotal", this.totalTransitTime);
		nbtCompound.setInteger("timeDiff", this.timeDiff);

	}

	public void readFromNBT(NBTTagCompound nbtCompound) {
		super.readFromNBT(nbtCompound);

		if(nbtCompound.hasKey("sendingEntity"))
			sendingNBT = (NBTTagCompound)nbtCompound.getTag("sendingEntity");
		if(nbtCompound.hasKey("recievingEntity"))
			recievingNBT = (NBTTagCompound)nbtCompound.getTag("recievingEntity");
		try
		{
			if(nbtCompound.hasKey("sendingClass"))
				sendingClass = Class.forName(nbtCompound.getString("sendingClass"));
			if(nbtCompound.hasKey("recievingClass"))
				recievingClass = Class.forName(nbtCompound.getString("recievingClass"));
		}
		catch(ClassNotFoundException e) {
			//TODO: log to console
			e.getMessage();
		}

		NBTTagList tagList = nbtCompound.getTagList("InventoryOut",8);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < outgoingInv.length) {
				outgoingInv[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}

		tagList = nbtCompound.getTagList("InventoryIn",8);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < incomingInv.length) {
				incomingInv[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
		readFromNBTHelper(nbtCompound);
	}

	private void readFromNBTHelper(NBTTagCompound nbtCompound) {
		this.xSize = nbtCompound.getByte("xSize");
		this.ySize = nbtCompound.getByte("ySize");
		this.zSize = nbtCompound.getByte("zSize");

		this.dest_x = nbtCompound.getInteger("dest_x");
		this.dest_y = nbtCompound.getInteger("dest_y");
		this.dest_z = nbtCompound.getInteger("dest_z");

		this.primary_x = nbtCompound.getInteger("primaryTileX");
		this.primary_y = nbtCompound.getInteger("primaryTileY");
		this.primary_z = nbtCompound.getInteger("primaryTileZ");
		this.isComplete = nbtCompound.getBoolean("complete");

		this.mode = nbtCompound.getByte("mode");

		this.totalTransitTime = nbtCompound.getLong("transitTimeTotal");
		this.timeDiff = nbtCompound.getInteger("timeDiff");
	}

	public void breakMultiStructure(World world) {

		if(!world.isRemote){
			//PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 128.0, world.provider.dimensionId, new PacketMachine(this, (byte)103).makePacket());

			//PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 128.0, world.provider.dimensionId, new MultiblockFormPacket(this, false, (byte)MultiblockFormPacket.Codes.COMPLETE.ordinal()).makePacket());
		}

		//TODO: add radius
		for(int offsetX = -1; offsetX < padSize[0] - 1; offsetX++) {
			for(int offsetZ = -1; offsetZ < padSize[2] - 1; offsetZ++) {
				TileEntity e = world.getTileEntity(this.xCoord - offsetX, this.yCoord, this.zCoord - offsetZ);
				if(e instanceof IMultiblock) {
					((IMultiblock)e).setIncomplete();
				}
			}
		}

	}

	public boolean canTransportEntity(Entity e) {
		//TODO: add blacklist
		return !(e instanceof EntityPlayer || e instanceof EntityItem || e instanceof EntityFlyingVehicle );
	}

	public boolean startMove() {

		//Do some checks on the server to make sure it's possible
		if(this.isBackedup())
			return false;

		//TileEntity e = this.getEntityAtFuel();

		IInventory fuelInv = this.getInventoryAtFuel(), inputInv = this.getInventoryAtInput();

		if(inputInv == null || fuelInv == null)
			return false;


		if(ZUtils.isInvEmpty(inputInv) || !ZUtils.isInvEmpty(outgoingInv))
			return false;

		double distance = Math.sqrt(this.getDistanceFrom(this.dest_x, this.dest_y, this.dest_z));
		this.timeDiff = this.getTransferTime(distance);
		int fuelTicks =  (int)(this.timeDiff * this.getFuelConsumtionMultiplyer(distance));


		//TODO: make it look for all items of type rather than slot
		//for(int i = 0; i < outgoingInv.length; i++) {
		//int g = 0;



		if(attemptUseFuel(fuelInv,fuelTicks)) {
			this.totalTransitTime = this.worldObj.getTotalWorldTime() + this.timeDiff;


			for(int i = 0, g = 0; i < outgoingInv.length && g < inputInv.getSizeInventory(); g++) {


				if(inputInv.getStackInSlot(g) != null) {
					this.outgoingInv[i] = inputInv.getStackInSlot(g).copy();
					i++;
				}
				else
					this.outgoingInv[i] = null;

				inputInv.setInventorySlotContents(g, null);
			}


			if(this.timeDiff > (60/EntityDummyBlimp.speed))
				this.worldObj.spawnEntityInWorld(new EntityDummyBlimp(this.worldObj, this.xCoord, this.yCoord + 1, this.zCoord,true,255));
		}
		else {
			this.timeDiff = -1;
			return false;
		}
		return true;
	}



	public boolean attemptUseFuel(IInventory fuelInv, int fuelTicks) {
		HashMap<Integer, Integer> slotMap = new HashMap<Integer, Integer>();

		for(int i=0; i < fuelInv.getSizeInventory(); i++) {

			ItemStack stack = fuelInv.getStackInSlot(i);

			if(stack != null)
			{
				if(fuelTicks > 0) {
					int burnTime = TileEntityFurnace.getItemBurnTime(stack);

					int numberItems = Math.min(fuelTicks / burnTime, 64);
					fuelTicks -= numberItems * burnTime;

					if(fuelTicks < burnTime) {
						fuelTicks = 0;
						numberItems += 1;
					}

					slotMap.put(i, numberItems);
				}
				else {
					break;
				}
			}
		}


		if(fuelTicks <= 0) {
			for(Entry<Integer, Integer> i : slotMap.entrySet()) {
				fuelInv.decrStackSize(i.getKey(), i.getValue());
			}
			return true;
		}
		return false;
	}


	public boolean attemptComplete() {
		return attemptComplete(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
	}

	public boolean attemptComplete(World world, int x, int y, int z) {

		boolean complete = true;

		for(int deltaZ = -1; deltaZ <= 1; deltaZ++) {
			for(int deltaX = -1; deltaX <= 1; deltaX++) {

				TileEntity e = world.getTileEntity(x + deltaX, y, z + deltaZ);
				Block tmp = world.getBlock(x + deltaX, y, z + deltaZ);

				if(e == null || !(e instanceof TileEntityPointer || e.equals(this))) {
					return false;
				}

				IMultiblock multi = (IMultiblock)e;

				if(!(tmp == Blimps.blockPadConcrete || tmp == Blimps.blockZepplinPad) || multi.isComplete()) {


					complete = false;
					break;
				}
			}
			if(!complete)
				break;
		}

		if(complete)
		{

			TileEntityLoadingDock master = (TileEntityLoadingDock) world.getTileEntity(x, y, z);
			master.completeMultiStructure(world);
			//PacketDispatcher.sendPacketToAllAround(x, y, z, 64, world.provider.dimensionId, new MultiblockFormPacket(master, true).makePacket());
			return true;
		}
		return false;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {}

	
	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
	
	@Override
	public String getInventoryName() {
		return "LoadingDock";
	}

	@Override
	public void markDirty() {
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return this == worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) &&
				entityplayer.getDistanceSq(this.xCoord, this.yCoord, this.zCoord) < 64;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public void onLinkStart(ItemStack item, TileEntity entity,
			EntityPlayer player, World world) {
		if(this.isComplete) {
			
			ItemLinker.setMasterX(item, this.xCoord);
			ItemLinker.setMasterY(item, this.yCoord);
			ItemLinker.setMasterZ(item, this.zCoord);

			if(player.worldObj.isRemote)
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage((new ChatComponentText("You program the linker with the coordinates: " + this.xCoord + " " + this.yCoord + " " + this.zCoord)));

		}
	}

	@Override
	public void onLinkComplete(ItemStack item, TileEntity entity,
			EntityPlayer player, World world) {
		if(this.isComplete) {
			if(ItemLinker.getMasterX(item) != this.primary_x || ItemLinker.getMasterY(item) != this.primary_y || ItemLinker.getMasterZ(item) != this.primary_z) {
				this.dest_x = ItemLinker.getMasterX(item);
				this.dest_y = ItemLinker.getMasterY(item);
				this.dest_z = ItemLinker.getMasterZ(item);

				ItemLinker.resetPosition(item);

				if(player.worldObj.isRemote)
					Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage((new ChatComponentText("You feed the linker's coordinates into the computer")));
			}
			else if(player.worldObj.isRemote)
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage((new ChatComponentText("I don't think sending a package in circles is worth doing...")));
		}
	}


}
