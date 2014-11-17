package zmaster587.blimps.api;

public interface IAirliftableTile {
	
	/**
	 * Checks to see if the tile can be airlifted
	 * @return true if the tile can be airlifted
	 */
	public boolean canBeAirlifted();
	
	/**
	 * Used to make sure the blimp has enough lifting power to pick up the entity
	 * @return amount of lifting power
	 */
	public int getMinRequiredLiftPower();
	
	/**
	 * Executed when a blimp picks up the tile
	 * @param entity IAirliftableEntity being created
	 */
	public void onLifted(IAirliftableEntity entity);
	
	/**
	 * Executed when the tile is created from an entity
	 * @param entity IAirliftableEntity that is creating the tile
	 */
	public void onDropped(IAirliftableEntity entity);
}
