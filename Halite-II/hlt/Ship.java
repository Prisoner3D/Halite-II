package hlt;

public class Ship extends Entity {

    public enum DockingStatus { Undocked, Docking, Docked, Undocking }

    private final DockingStatus dockingStatus;
    private final int dockedPlanet;
    private final int dockingProgress;
    private final int weaponCooldown;
    private int thrust; // Thrust of following turn
    private int angle; // Angle of following turn
    private Position newPosition; // New pos of following turn
    private Position groupingPos; // Saved grouping pos
    
    private int current; // Current amount of ships going for it (only used for distraction enemies)
    private boolean ignore; // Ignore ships during calculations
    private boolean completed; // Ship has made a move
    
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
        this.newPosition = null;
        this.groupingPos = null;
        
        this.current = 0;
        this.ignore = false;
        this.completed = false;
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
        return "Ship[" + super.toString() + ", dockingStatus=" + dockingStatus + ", dockedPlanet=" + dockedPlanet + ", dockingProgress=" + dockingProgress + ", weaponCooldown=" + weaponCooldown + "]";
    }
}
