package hlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Collection;

public class GameMap {
    private final int width, height;
    private final int playerId;
    private final List<Player> players;
    private final List<Player> playersUnmodifiable;
    private final Map<Integer, Planet> planets;
    private final List<Ship> allShips;
    private final List<Ship> allShipsUnmodifiable;

    // used only during parsing to reduce memory allocations
    private final List<Ship> currentShips = new ArrayList<>();

    public GameMap(final int width, final int height, final int playerId) {
        this.width = width;
        this.height = height;
        this.playerId = playerId;
        players = new ArrayList<>(Constants.MAX_PLAYERS);
        playersUnmodifiable = Collections.unmodifiableList(players);
        planets = new TreeMap<>();
        allShips = new ArrayList<>();
        allShipsUnmodifiable = Collections.unmodifiableList(allShips);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getMyPlayerId() {
        return playerId;
    }
    
    /* Gets other enemy player - Note: Only works on 2 Player */
    public Player getOtherEnemyPlayer() {
    	for (Player player : getAllPlayers()) {
    		if (player != getAllPlayers().get(getMyPlayerId())) {
    			return player;
    		}
    	}
    	return null;
    }
    
	public List<Player> getAllPlayers() {
		return playersUnmodifiable;
	}

	public Player getMyPlayer() {
		return getAllPlayers().get(getMyPlayerId());
	}

	public Player getPlayer(int id) {
		for (Player player : players) {
			if (player.getId() == id) {
				return player;
			}
		}
		return null;
	}

    public Ship getShip(final int playerId, final int entityId) throws IndexOutOfBoundsException {
        return players.get(playerId).getShip(entityId);
    }

    public Planet getPlanet(final int entityId) {
        return planets.get(entityId);
    }

    public Map<Integer, Planet> getAllPlanets() {
        return planets;
    }

    public List<Ship> getAllShips() {
        return allShipsUnmodifiable;
    }

    public ArrayList<Entity> objectsBetween(Position start, Position target) {
        final ArrayList<Entity> entitiesFound = new ArrayList<>();
        addEntitiesBetween(entitiesFound, start, target, planets.values());
        addEntitiesBetween(entitiesFound, start, target, allShips);
        return entitiesFound;
    }
    
    public ArrayList<Entity> objectsBetweenNoShips(Position start, Position target) {
        final ArrayList<Entity> entitiesFound = new ArrayList<>();
        addEntitiesBetween(entitiesFound, start, target, planets.values());
        return entitiesFound;
    }
    
    /* Checks if the ships will be able to shoot */
    public ArrayList<Entity> shipsCanShootAt(Position start, Position target) {
        final ArrayList<Entity> entitiesFound = new ArrayList<>();
        addEntitiesThatShootBetween(entitiesFound, start, target, allShips);
        return entitiesFound;
    }
    
	private static void addEntitiesBetween(final List<Entity> entitiesFound, final Position start,
			final Position target, final Collection<? extends Entity> entitiesToCheck) {

		for (final Entity entity : entitiesToCheck) {
			if (entity.equals(start) || entity.equals(target)) {
				continue;
			}
			if (Collision.segmentCircleIntersect(start, target, entity, Constants.FORECAST_FUDGE_FACTOR)) {
				entitiesFound.add(entity);
			}
		}
	}
    
	private static void addEntitiesThatShootBetween(final List<Entity> entitiesFound, final Position start,
			final Position target, final Collection<? extends Entity> entitiesToCheck) {

		for (final Entity entity : entitiesToCheck) {
			if (entity.equals(start) || entity.equals(target)) {
				continue;
			}
			if (entity instanceof Ship) {
				if (((Ship) entity).getDockingStatus() != Ship.DockingStatus.Undocked) {
					continue;
				}
			}
			if (Collision.segmentCircleIntersect(start, target, entity, 7.5)) {
				entitiesFound.add(entity); // 15 circle
			}

		}
	}
    
	public Map<Double, Entity> nearbyEntitiesByDistance(final Position entity) {
		final Map<Double, Entity> entityByDistance = new TreeMap<>();

		for (final Planet planet : planets.values()) {
			if (planet.equals(entity)) {
				continue;
			}
			entityByDistance.put(entity.getDistanceTo(planet), planet);
		}

		for (final Ship ship : allShips) {
			if (ship.equals(entity)) {
				continue;
			}
			entityByDistance.put(entity.getDistanceTo(ship), ship);
		}

		return entityByDistance;
	}
    
	public Map<Double, Ship> nearbyShipsByDistance(final Position entity) {
		final Map<Double, Ship> shipsByDistance = new TreeMap<>();

		for (final Ship ship : allShips) {
			if (ship.equals(entity)) {
				continue;
			}
			shipsByDistance.put(entity.getDistanceTo(ship), ship);
		}

		return shipsByDistance;
	}

	public Map<Double, Ship> nearbyDockedShipsByDistance(final Position entity) {
		final Map<Double, Ship> shipsByDistance = new TreeMap<>();

		for (final Ship ship : allShips) {
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getDockingStatus() == Ship.DockingStatus.Undocked) {
				continue;
			}
			shipsByDistance.put(entity.getDistanceTo(ship), ship);
		}

		return shipsByDistance;
	}
    
	public Map<Double, Ship> nearbyEnemyShipsByDistance(final Position entity) {
		final Map<Double, Ship> shipsByDistance = new TreeMap<>();

		for (final Ship ship : allShips) {
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getOwner() == getMyPlayerId()) {
				continue;
			}
			shipsByDistance.put(entity.getDistanceTo(ship), ship);
		}

		return shipsByDistance;
	}

	public Map<Double, Planet> nearbyPlanetsByDistance(final Position entity) {
		final Map<Double, Planet> planetsByDistance = new TreeMap<>();

		for (final Planet planet : planets.values()) {
			if (planet.equals(entity)) {
				continue;
			}
			planetsByDistance.put(entity.getDistanceTo(planet), planet);
		}
		return planetsByDistance;
	}
    
    public GameMap updateMap(final Metadata mapMetadata) {
        DebugLog.addLog("--- NEW TURN ---");
        final int numberOfPlayers = MetadataParser.parsePlayerNum(mapMetadata);

        players.clear();
        planets.clear();
        allShips.clear();

        // Updates Players Info
        for (int i = 0; i < numberOfPlayers; ++i) {
            currentShips.clear();
            final Map<Integer, Ship> currentPlayerShips = new TreeMap<>();
            final int playerId = MetadataParser.parsePlayerId(mapMetadata);

            final Player currentPlayer = new Player(playerId, currentPlayerShips);
            MetadataParser.populateShipList(currentShips, playerId, mapMetadata);
            allShips.addAll(currentShips);

            for (final Ship ship : currentShips) {
                currentPlayerShips.put(ship.getId(), ship);
            }
            players.add(currentPlayer);
        }

        final int numberOfPlanets = Integer.parseInt(mapMetadata.pop());

        for (int i = 0; i < numberOfPlanets; ++i) {
            final List<Integer> dockedShips = new ArrayList<>();
            final Planet planet = MetadataParser.newPlanetFromMetadata(dockedShips, mapMetadata);
            planets.put(planet.getId(), planet);
        }
        if (!mapMetadata.isEmpty()) {
            throw new IllegalStateException("Failed to parse data from Halite game engine. Please contact maintainers.");
        }
        return this;
    }
}
