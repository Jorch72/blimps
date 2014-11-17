package zmaster587.blimps.client.render;

import org.lwjgl.opengl.GL11;

import zmaster587.blimps.entity.EntityFlyingVehicle;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class RendererScoutBlimp extends Render {

	protected ModelBlimpBase modelBlimp;// = AdvancedModelLoader.loadModel("/assets/blimps/models/scoutBlimp.obj");;
	//private static final ResourceLocation texture; = new ResourceLocation("blimps:textures/entity/EntityScoutBlimp.png");
	
	public RendererScoutBlimp(ModelBase model, float idk)
	{
		super();
		//IModelCustom
		this.modelBlimp = (ModelBlimpBase) model;
	}

	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return this.modelBlimp.getTexture();
	}
	
	@Override
	public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9)
	{
		boolean hasFan = entity instanceof EntityFlyingVehicle;
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float)par2, (float)par4, (float)par6);
		GL11.glRotatef(90.0F - par8, 0.0F, 1.0F, 0.0F);
		/*float f2 = (float)par1EntityBoat.getTimeSinceHit() - par9;
		float f3 = par1EntityBoat.getDamageTaken() - par9;

		if (f3 < 0.0F)
		{
			f3 = 0.0F;
		}

		if (f2 > 0.0F)
		{
			GL11.glRotatef(MathHelper.sin(f2) * f2 * f3 / 10.0F * (float)par1EntityBoat.getForwardDirection(), 1.0F, 0.0F, 0.0F);
		}*/

		float f4 = 0.75F;
		GL11.glScalef(f4, f4, f4);
		GL11.glScalef(1.0F / f4, 1.0F / f4, 1.0F / f4);
		this.bindEntityTexture(entity);
		GL11.glScalef(-1.0F, -1.0F, 1.0F);
		this.modelBlimp.render(entity, hasFan ? ((EntityFlyingVehicle)entity).getFanPosition() : 0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		//this.modelBlimp.renderAll();
		GL11.glPopMatrix();
	}
}
