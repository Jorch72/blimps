package zmaster587.blimps.client.render;

import org.lwjgl.opengl.GL11;

import zmaster587.blimps.entity.EntityAirlifted;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RendererCrate extends Render {

	ResourceLocation texture;
	public RendererCrate() {
		texture = new ResourceLocation("blimps:textures/entity/Crate.png");
	}

	public void doRender(Entity entity, double x, double y, double z, float par8, float par9)
	{
		//System.out.println(tile);
		if(entity.isInvisible())
			return;
		else {

			float f = 0.5F;
			float f1 = 1.0F;
			float f2 = 0.8F;
			float f3 = 0.6F;
			Tessellator tess = Tessellator.instance;

			GL11.glPushMatrix();
			GL11.glTranslated(x, y, z);
			GL11.glRotated(-entity.rotationYaw, 0, 1, 0);

			Minecraft.getMinecraft().renderEngine.bindTexture(texture);

			tess.startDrawingQuads();
			tess.setBrightness(entity.getBrightnessForRender(0));
			//top
			tess.addVertexWithUV(-.5, 0.5, -.5, 0, 0);
			tess.addVertexWithUV(-.5, 0.5, 0.5, 0, 1);
			tess.addVertexWithUV(0.5, 0.5, 0.5, 1, 1);
			tess.addVertexWithUV(0.5, 0.5, -.5, 1, 0);

			//north
			tess.addVertexWithUV(-0.5, 0.5, -.5, 0, 0);
			tess.addVertexWithUV(0.5, 0.5, -.5, 1, 0);
			tess.addVertexWithUV(0.5, -.5, -.5, 1, 1);
			tess.addVertexWithUV(-.5, -.5, -.5, 0, 1);

			//south
			tess.addVertexWithUV(-.5, 0.5, 0.5, 0, 0);
			tess.addVertexWithUV(-.5, -.5, 0.5, 0, 1);
			tess.addVertexWithUV(0.5, -.5, 0.5, 1, 1);
			tess.addVertexWithUV(0.5, 0.5, 0.5, 1, 0);

			//east
			tess.addVertexWithUV(0.5, 0.5, -.5, 0, 0);
			tess.addVertexWithUV(0.5, 0.5, 0.5, 1, 0);
			tess.addVertexWithUV(0.5, -.5, 0.5, 1, 1);
			tess.addVertexWithUV(0.5, -.5, -.5, 0, 1);

			//west
			tess.addVertexWithUV(-.5, -.5, -.5, 0, 1);
			tess.addVertexWithUV(-.5, -.5, 0.5, 1, 1);
			tess.addVertexWithUV(-.5, 0.5, 0.5, 1, 0);
			tess.addVertexWithUV(-.5, 0.5, -.5, 0, 0);

			//bottom
			tess.addVertexWithUV(0.5, -.5, -.5, 0, 1);
			tess.addVertexWithUV(0.5, -.5, 0.5, 1, 1);
			tess.addVertexWithUV(-.5, -.5, 0.5, 1, 0);
			tess.addVertexWithUV(-.5, -.5, -.5, 0, 0);

			tess.draw();
			GL11.glPopMatrix();
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		// TODO Auto-generated method stub
		return new ResourceLocation("blimps:/textures");
	}
}
