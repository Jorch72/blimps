package zmaster587.blimps.client.render;

import org.lwjgl.opengl.GL11;

import zmaster587.blimps.Blimps;
import zmaster587.blimps.tileEntity.TileEntityLoadingDock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RendererMultiBlock extends TileEntitySpecialRenderer {


	ResourceLocation texture;
	public RendererMultiBlock() {
		texture = new ResourceLocation("blimps:textures/entity/landingPad.png");
	}



	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {

		TileEntityLoadingDock tile = (TileEntityLoadingDock)tileentity;

		if(!tile.isComplete() || !tile.isMasterBlock())
			return;


		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		Tessellator tess = Tessellator.instance;

		f = Blimps.blockZepplinPad.getLightValue(tileentity.getWorldObj(), tileentity.xCoord -2, tileentity.yCoord, tileentity.zCoord - 2);

		int l = tileentity.getWorldObj().getLightBrightnessForSkyBlocks(tileentity.xCoord -2, tileentity.yCoord, tileentity.zCoord -2,0);
		int l1 = l % 65536;
		float l2 = l / 65536.0f;
		tess.setColorOpaque_F(f, f, f);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)l1, l2);

		GL11.glPushMatrix();
		GL11.glTranslated(x-1, y, z-1);

		tess.startDrawingQuads();
		
		//top
		tess.addVertexWithUV(0, 1, 0, 0, 0);
		tess.addVertexWithUV(0, 1, 3, 0, 0.375);
		tess.addVertexWithUV(3, 1, 3, 1, 0.375);
		tess.addVertexWithUV(3, 1, 0, 1, 0);

		//north
		tess.addVertexWithUV(0, 1, 0, 0, 0.5);
		tess.addVertexWithUV(3, 1, 0, 1, 0.5);
		tess.addVertexWithUV(3, 0, 0, 1, 0.6171875);
		tess.addVertexWithUV(0, 0, 0, 0, 0.6171875);

		//south
		tess.addVertexWithUV(0, 1, 3, 0, 0.375);
		tess.addVertexWithUV(0, 0, 3, 0, 0.5);
		tess.addVertexWithUV(3, 0, 3, 1, 0.5);
		tess.addVertexWithUV(3, 1, 3, 1, 0.375);

		//east
		tess.addVertexWithUV(3, 1, 0, 0, 0.375);
		tess.addVertexWithUV(3, 1, 3, 1, 0.375);
		tess.addVertexWithUV(3, 0, 3, 1, 0.5);
		tess.addVertexWithUV(3, 0, 0, 0, 0.5);

		//west
		tess.addVertexWithUV(0, 0, 0, 0, 0.6171875);
		tess.addVertexWithUV(0, 0, 3, 1, 0.6171875);
		tess.addVertexWithUV(0, 1, 3, 1, 0.5);
		tess.addVertexWithUV(0, 1, 0, 0, 0.5);

		//bottom
		tess.addVertexWithUV(3, 0, 0, 0, 1);
		tess.addVertexWithUV(3, 0, 3, 1, 1);
		tess.addVertexWithUV(0, 0, 3, 1, 0.6171875);
		tess.addVertexWithUV(0, 0, 0, 0, 0.6171875);


		tess.draw();
		GL11.glPopMatrix();
	}

	private int average(World world,int x, int y, int z, int dx,int dy,int dz) {

		int average = 0;

		if(dy == 0) {
			for(int i = 0; i < dz; i++)
				average += averageHelper(world, x, y, z + i, dx, 1, 0, 0);
		}
		else if(dz == 0) {
			for(int i = 0; i < dy; i++)
				average += averageHelper(world, x, y + i, z, dx, 1, 0, 0);
		}
		else if(dx == 0) {
			for(int i = 0; i < dy; i++)
				average += averageHelper(world, x, y + i, z, dz, 0, 0, 1);
		}
		
		return average / (Math.max(1, dx)*Math.max(1, dy)*Math.max(1, dz));
	}
	private int averageHelper(World world, int x, int y, int z, int da, int i, int j, int k) {
		int avg = 0;
		if(da == 0) {
			avg = world.getLightBrightnessForSkyBlocks(x,y,z, 0);
		}
		else {
			for(int iter = 0; iter < da; iter++) {
				avg += world.getLightBrightnessForSkyBlocks(x + (i*da),y + (j*da) ,z + (k*da), 0);
			}
		}
		return avg;
	}
}
