package zmaster587.blimps.client.render;

import org.lwjgl.opengl.GL11;

import zmaster587.blimps.entity.EntityFlyingVehicle;
import zmaster587.blimps.entity.EntityCargoBlimp;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.Tessellator;
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

public class RendererCargoBlimp extends Render {

	protected ModelBlimpBase modelBlimp;
	private static final ResourceLocation texture = new ResourceLocation("blimps:textures/entity/CargoBlimp.png");

	public RendererCargoBlimp(ModelBase model, float idk)
	{
		super();
		this.modelBlimp = (ModelBlimpBase) model;
	}

	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return texture;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float par8, float par9)
	{

		EntityCargoBlimp blimp = (EntityCargoBlimp)entity;

		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		GL11.glRotatef(90.0F - par8, 0.0F, 1.0F, 0.0F);
		//float f2 = (float)par1EntityBoat.getTimeSinceHit() - par9;
		//float f3 = par1EntityBoat.getDamageTaken() - par9;

		/*if (f3 < 0.0F)
		{
			f3 = 0.0F;
		}

		if (f2 > 0.0F)
		{
			GL11.glRotatef(MathHelper.sin(f2) * f2 * f3 / 10.0F * (float)par1EntityBoat.getForwardDirection(), 1.0F, 0.0F, 0.0F);
		}*/

		//GL11.glTranslatef(0.0F, 0.0F, 0.7F);
		float f4 = 0.75F;
		GL11.glScalef(f4, f4, f4);
		GL11.glScalef(1.0F / f4, 1.0F / f4, 1.0F / f4);
		this.bindEntityTexture(blimp);
		GL11.glScalef(-1.0F, -1.0F, 1.0F);
		//this.modelBoat.render(blimp, blimp.getFanPosition(), 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		this.modelBlimp.render(entity, ((EntityFlyingVehicle)entity).getFanPosition(), 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		GL11.glPopMatrix();


		if(blimp.cargoEntity != null)
		{

			double posX1,posY1,posZ1, distToCam;

			posX1 = ((blimp.cargoEntity.posX - blimp.posX) * -MathHelper.sin((float) (par8 * (Math.PI/180.0F)))) + ((blimp.cargoEntity.posZ - blimp.posZ) * MathHelper.cos((float) (par8 * (Math.PI/180.0F))));// + blimp.cargoOffsetX;
			posY1 = blimp.cargoEntity.posY - blimp.cargoEntity.lastTickPosY  + 0.4F;// + blimp.cargoOffsetY;
			posZ1 = ((blimp.cargoEntity.posZ - blimp.posZ) * MathHelper.sin((float) (par8 * (Math.PI/180.0F)))) + ((blimp.cargoEntity.posX - blimp.posX) * MathHelper.cos((float) (par8 * (Math.PI/180.0F))));

			distToCam = this.renderManager.getDistanceToCamera(blimp.cargoEntity.posX, blimp.cargoEntity.posY, blimp.cargoEntity.posZ);
			//distToCam *= distToCam;

			//render cargo
			GL11.glPushMatrix();
			GL11.glTranslatef((float)x, (float)y, (float)z); 

			GL11.glRotatef(90.0F-par8, 0.0F, 1.0F, 0.0F);

			if(blimp.canCargoVanish) {
				blimp.cargoEntity.setInvisible(false);
				renderManager.renderEntityWithPosYaw(blimp.cargoEntity, blimp.cargoOffsetX, -blimp.cargoOffsetY - blimp.liftOffset, -blimp.cargoOffsetZ, par8, par9);
				blimp.cargoEntity.setInvisible(true);
			}


			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(0.6f, 0.6f, 0.4f, 1.0f);
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			final float radius = .125f;

			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			for(double i = 0; i < 2*Math.PI; i += Math.PI) {
				tessellator.addVertex(radius* Math.sin(i) - blimp.cargoOffsetX , blimp.cargoOffsetY + 2,   radius* Math.cos(i) - blimp.cargoOffsetZ);
				tessellator.addVertex(- radius* Math.cos(i) - blimp.cargoOffsetX, blimp.cargoOffsetY + 2, - radius* Math.cos(i) - blimp.cargoOffsetZ);
				tessellator.addVertex(- radius* Math.cos(i) - blimp.cargoOffsetX, -blimp.cargoOffsetY + blimp.cargoEntity.posY - blimp.posY + 0.2, -radius* Math.cos(i) - blimp.cargoOffsetZ);
				tessellator.addVertex(radius* Math.sin(i) - blimp.cargoOffsetX, -blimp.cargoOffsetY + blimp.cargoEntity.posY - blimp.posY + 0.2, radius* Math.cos(i) - blimp.cargoOffsetZ);
			}

			for(double i = 0; i < 2*Math.PI; i += Math.PI) {
				tessellator.addVertex(- radius* Math.cos(i) - blimp.cargoOffsetX, blimp.cargoOffsetY + 2, radius* Math.sin(i) - blimp.cargoOffsetZ);
				tessellator.addVertex(radius* Math.sin(i) - blimp.cargoOffsetX, blimp.cargoOffsetY + 2, -radius* Math.cos(i) - blimp.cargoOffsetZ);
				tessellator.addVertex(radius* Math.sin(i) - blimp.cargoOffsetX, blimp.cargoOffsetY + blimp.cargoEntity.posY - blimp.posY + 0.2, -radius* Math.cos(i)  - blimp.cargoOffsetZ);
				tessellator.addVertex(- radius* Math.cos(i) - blimp.cargoOffsetX, blimp.cargoOffsetY + blimp.cargoEntity.posY - blimp.posY + 0.2,radius* Math.sin(i)  - blimp.cargoOffsetZ);
			}
			tessellator.draw();

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPopMatrix();
		}
	}	
}
