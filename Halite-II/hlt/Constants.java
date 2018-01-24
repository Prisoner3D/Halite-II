package hlt;

public class Constants {

	////////////////////////////////////////////////////////////////////////
	// Implementation-independent language-agnostic constants

	/** Games will not have more players than this */
	public static final int MAX_PLAYERS = 4;
	/** Max number of units of distance a ship can travel in a turn */
	public static final int MAX_SPEED = 7;
	/** Radius of a ship */
	public static final double SHIP_RADIUS = 0.5;
	/** Starting health of ship, also its max */
	public static final int MAX_SHIP_HEALTH = 255;
	/** Starting health of ship, also its max */
	public static final int BASE_SHIP_HEALTH = 255;
	/** Weapon cooldown period */
	public static final int WEAPON_COOLDOWN = 1;
	/** Weapon damage radius */
	public static final double WEAPON_RADIUS = 5.0;
	/** Weapon damage */
	public static final int WEAPON_DAMAGE = 64;
	/** Radius in which explosions affect other entities */
	public static final double EXPLOSION_RADIUS = 10.0;
	/** Distance from the edge of the planet at which ships can try to dock */
	public static final double DOCK_RADIUS = 4.0;
	/** Number of turns it takes to dock a ship */
	public static final int DOCK_TURNS = 5;
	/** Number of turns it takes to create a ship per docked ship */
	public static final int BASE_PRODUCTIVITY = 6;
	/** Distance from the planets edge at which new ships are created */
	public static final double SPAWN_RADIUS = 2.0;
	
	////////////////////////////////////////////////////////////////////////
	// Implementation-specific constants
	public static final double FORECAST_FUDGE_FACTOR = SHIP_RADIUS + 0.1;
	
	public static final int MAX_NAVIGATION_CORRECTIONS = 45;
	
	/** Used in Position.getClosestPoint() */
	public static final int MIN_DISTANCE = 2;
	
	/** Ratio of total ships to my ships until PANIC **/
	public static final int PANIC = 10;
	
	/** Area around ships to defend **/
	public static final int DEFENDING = 25;
	
	/** Area around ships considered "group" **/
	public static final int GROUP = 15;
	
	/** Area around ships considered "squad" **/
	public static final int SQUAD = 5;
}
