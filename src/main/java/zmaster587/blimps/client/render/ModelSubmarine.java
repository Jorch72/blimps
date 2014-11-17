package zmaster587.blimps.client.render;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class ModelSubmarine extends ModelBlimpBase {

	protected final IModelCustom modelBlimp = AdvancedModelLoader.loadModel(new ResourceLocation("blimps:models/submarine.obj"));
	private static final ResourceLocation texture = new ResourceLocation("blimps:textures/entity/Submarine.png");
	//fields
	ModelRenderer propHuba;
	ModelRenderer prop1a;
	ModelRenderer prop2a;
	ModelRenderer prop3a;
	ModelRenderer propHubb;
	ModelRenderer prop1b;
	ModelRenderer prop2b;
	ModelRenderer prop3b;

	public ModelSubmarine()
	{
		textureWidth = 128;
		textureHeight = 128;

	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		//setRotationAngles(f, f1, f2, f3, f4, f5,entity);

		modelBlimp.renderAll();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e)
	{
		f *= 3;
		super.setRotationAngles(f, f1, f2, f3, f4, f5, e);

		setRotation(prop1a, prop1a.rotateAngleX, prop1a.rotateAngleY, f - 1.570796F);
		setRotation(prop2a, prop2a.rotateAngleX, prop2a.rotateAngleY, f + 2.617994F);
		setRotation(prop3a, prop3a.rotateAngleX, prop3a.rotateAngleY, f + 0.5235988F);
		setRotation(propHuba, propHuba.rotateAngleX, propHuba.rotateAngleY, f);

		setRotation(prop1b, prop1b.rotateAngleX, prop1b.rotateAngleY, -f - 1.570796F);
		setRotation(prop2b, prop2b.rotateAngleX, prop2b.rotateAngleY, -f + 2.617994F);
		setRotation(prop3b, prop3b.rotateAngleX, prop3b.rotateAngleY, -f + 0.5235988F);
		setRotation(propHubb, propHubb.rotateAngleX, propHubb.rotateAngleY, -f);
	}

	@Override
	public ResourceLocation getTexture() {
		// TODO Auto-generated method stub
		return texture;
	}
}
