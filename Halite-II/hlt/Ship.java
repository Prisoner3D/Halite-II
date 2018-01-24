package hlt;

public class Ship extends Entity {

    public enum DockingStatus { Undocked, Docking, Docked, Undocking }

    private final DockingStatus dockingStatus;
    private final int dockedPlanet;
    private final int dockingProgress;
    private final int weaponCooldown;
    private int thrust;
    private int angle;
    private Planet planet;
    private Ship ship;
    private int current;
    private boolean ignore;
    private boolean completed;
    private Position newPosition;
    private Position newPositionEnemy;
    private Position groupingPos;

	public Ship(final int owner, final int id, final double xPos, final double yPos,
                final int health, final DockingStatus dockingStatus, final int dockedPlanet,
                final int dockingProgress, final int weaponCooldown) {

        super(owner, id, xPos, yPos, health, Constants.SHIP_RADIUS);

        this.dockingStatus = dockingStatus;
        this.dockedPlanet = dockedPlanet;
        this.dockingProgress = dockingProgress;
        this.weaponCooldown = weaponCooldown;
        this.thrust = 0;
        this.angle = 0;
        this.planet = null;
        this.ship = null;
        this.current = 0;
        this.ignore = false;
        this.completed = false;
        this.newPosition = null;
        this.newPositionEnemy = null;
        this.groupingPos = null;

    }
	
	public boolean getCompleted() {
		return completed;
	}
	
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public Position getPos() {
		return new Position (getXPos(),getYPos());
	}
	
	public Position getNewPos() {
		return newPosition;
	}
	
	public void setNewPos(Position newPosition) {
		this.newPosition = newPosition;
	}
	
	public Position getGroupingPos() {
		return groupingPos;
	}
	
	public void setGroupingPos(Position targetPos) {
		this.groupingPos = targetPos;
	}
	
	public Position getNewEnemyPos() {
		return newPositionEnemy;
	}
	
	public void setNewEnemyPos(Position newPosition) {
		this.newPositionEnemy = newPosition;
	}
	
	public Ship getShip() {
		return ship;
	}
	
	public void setShip(Ship ship) {
		this.ship = ship;
	}
	
	public Planet getPlanet() {
		return planet;
	}
	
	public void setPlanet(Planet planet) {
		this.planet = planet;
	}
	
	public int getCurrent() {
		return current;
	}
	
	public void incrementCurrent() {
		this.current++;
	}
	
	public void setThrust(int thrust) {
		this.thrust = thrust;
	}
	public int getThrust() {
		return thrust;
	}
	
	public void setAngle(int angle) {
		this.angle = angle;
	}
	public int getAngle() {
		return angle;
	}
	
    public int getWeaponCooldown() {
        return weaponCooldown;
    }
    
    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }
    
    public boolean getIgnore() {
        return ignore;
    }
    public DockingStatus getDockingStatus() {
        return dockingStatus;
    }

    public int getDockingProgress() {
        return dockingProgress;
    }

    public int getDockedPlanet() {
        return dockedPlanet;
    }
    
    public boolean canDock(final Planet planet) {
        return getDistanceTo(planet) <= Constants.DOCK_RADIUS + planet.getRadius();
    }

    @Override
    public String toString() {
        return "Ship[" +
                super.toString() +
                ", dockingStatus=" + dockingStatus +
                ", dockedPlanet=" + dockedPlanet +
                ", dockingProgress=" + dockingProgress +
                ", weaponCooldown=" + weaponCooldown +
                "]";
    }
}
