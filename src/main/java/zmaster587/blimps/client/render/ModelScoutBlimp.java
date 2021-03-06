// Date: 1/8/2014 9:23:55 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package zmaster587.blimps.client.render;

import zmaster587.blimps.entity.EntityFlyingVehicle;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class ModelScoutBlimp extends ModelBlimpBase
{
	//fields
	ModelRenderer prop1;
	ModelRenderer prop2;
	ModelRenderer prop3;
	ModelRenderer propHub;
	IModelCustom model;
	private static final ResourceLocation texture = new ResourceLocation("blimps:textures/entity/EntityScoutBlimp.png");
	
	public ModelScoutBlimp()
	{
		textureWidth = 256;
		textureHeight = 128;

		prop1 = new ModelRenderer(this, 76, 113);
		prop1.addBox(1F, 0F, 0F, 16, 2, 1);
		prop1.setRotationPoint(0F, 4F, 24.8F);
		prop1.setTextureSize(256, 128);
		prop1.mirror = true;
		setRotation(prop1, 0F, 0F, -1.570796F);
		prop2 = new ModelRenderer(this, 76, 113);
		prop2.addBox(1F, 0F, 0F, 16, 2, 1);
		prop2.setRotationPoint(0F, 4F, 24.8F);
		prop2.setTextureSize(256, 128);
		prop2.mirror = true;
		setRotation(prop2, 0F, 0F, 2.617994F);
		prop3 = new ModelRenderer(this, 76, 113);
		prop3.addBox(1F, 0F, 0F, 16, 2, 1);
		prop3.setRotationPoint(0F, 4F, 24.8F);
		prop3.setTextureSize(256, 128);
		prop3.mirror = true;
		setRotation(prop3, 0F, 0F, 0.5235988F);
		propHub = new ModelRenderer(this, 76, 108);
		propHub.addBox(-1F, -1F, -1F, 2, 2, 2);
		propHub.setRotationPoint(0F, 4F, 24.8F);
		propHub.setTextureSize(256, 128);
		propHub.mirror = true;
		setRotation(propHub, 0F, 0F, 0F);
		
		model = AdvancedModelLoader.loadModel(new ResourceLocation("blimps:models/scoutBlimp.obj"));
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5);
		
		model.renderAll();
		
		prop1.render(f5);
		prop2.render(f5);
		prop3.render(f5);
		propHub.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	private void addRotation(ModelRenderer model, float x, float y, float z)
	{

		//Yeah, it's radians, but we just don't want to overflow
		model.rotateAngleX = MathHelper.wrapAngleTo180_float(x + model.rotateAngleX);
		model.rotateAngleY = MathHelper.wrapAngleTo180_float(y + model.rotateAngleY);
		model.rotateAngleZ = MathHelper.wrapAngleTo180_float(z + model.rotateAngleZ);;
	}

	protected void animationTick(Entity e)
	{
		float speed = (float) ((EntityFlyingVehicle)e).getSpeed();

		addRotation(prop1, 0F, 0F, 0.8F * speed);
		addRotation(prop2, 0F, 0F, 0.8F * speed);
		addRotation(prop3, 0F, 0F, 0.8F * speed);
		addRotation(propHub, 0F, 0F, 0.8F * speed);
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5)
	{
		setRotation(prop1, prop1.rotateAngleX, prop1.rotateAngleY, f - 1.570796F);
		setRotation(prop2, prop2.rotateAngleX, prop2.rotateAngleY, f + 2.617994F);
		setRotation(prop3, prop3.rotateAngleX, prop3.rotateAngleY, f + 0.5235988F);
		setRotation(propHub, propHub.rotateAngleX, propHub.rotateAngleY, f);
	}

	@Override
	public ResourceLocation getTexture() {
		return texture;
	}
}
