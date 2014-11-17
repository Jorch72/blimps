package zmaster587.blimps.entity;

import net.minecraft.entity.Entity;

public interface ICargo {
	public void mountCargo(Entity e);
	
	public void unmountCargo(int slot);
	
	public Entity getCargo(int slot);
	
	public Entity findCargo(float maxDistace);
	
	public float getSearchRadius();

	public float getVertPickup();
}
