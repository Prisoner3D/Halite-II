package hlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Util {

    public static int angleRadToDegClipped(final double angleRad) {
        final long degUnclipped = Math.round(Math.toDegrees(angleRad));
        // Make sure return value is in [0, 360) as required by game engine.
        return (int) (((degUnclipped % 360L) + 360L) % 360L);
    }
    
    public static int new_angle_with_message(int angle, int message)
	{
		return ((message+1)*360) + angle;
		//return angle;
	}
    
    public static boolean iAmOwner(GameMap gameMap, Entity target) {
		return target.getOwner() == gameMap.getMyPlayerId();
	}
	
	public static int determineIncrement(GameMap gameMap) {
		Map<Integer, Ship> ships = gameMap.getMyPlayer().getShips();
		if (ships.size() > 300) {
			return 10;
		}
		if (ships.size() > 200) {
			return 8;
		}
		if (ships.size() > 100) {
			return 6;
		}
		if (ships.size() > 50) {
			return 4;
		}
		return 1;
	}

	public static Player returnOwnerOf(GameMap gameMap, Entity entity) {
		List<Player> players = gameMap.getAllPlayers();
		for (Player player : players) {
			if (player.getId() == entity.getOwner()) {
				return player;
			}
		}
		return null;
	}

	public static ArrayList<Ship> sortShipsByDistanceFrom(ArrayList<Ship> myShips, Position target) {
		ArrayList<Ship> sorted = new ArrayList<Ship>();
		Map<Double, Ship> shipsByDistance = new TreeMap<>();

		for (final Ship ship : myShips) {
			if (ship.equals(target)) {
				continue;
			}
			shipsByDistance.put(target.getDistanceTo(ship), ship);
		}
		for (double distance : shipsByDistance.keySet()) {
			Ship ship = shipsByDistance.get(distance);
			sorted.add(ship);
		}
		return sorted;
	}

	public static ArrayList<Ship> getEnemiesNear(GameMap gameMap, Position entity, double userDistance) {
		ArrayList<Ship> enemyShipsByEntity = new ArrayList<Ship>();
		Map<Double, Ship> nearbys = gameMap.nearbyEnemyShipsByDistance(entity);
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			double distance = entry.getKey();
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (distance < userDistance) {
				enemyShipsByEntity.add(ship);
			}
		}
		return enemyShipsByEntity;
	}

	public static ArrayList<Planet> getPlanetsNear(GameMap gameMap, Position entity, double userDistance) {
		ArrayList<Planet> enemyPlanetsByEntity = new ArrayList<Planet>();
		Map<Double, Planet> nearbys = gameMap.nearbyPlanetsByDistance(entity);
		for (Entry<Double, Planet> entry : nearbys.entrySet()) {
			double distance = entry.getKey();
			Planet planet = entry.getValue();
			if (planet.equals(entity)) {
				continue;
			}
			if (distance < userDistance) {
				enemyPlanetsByEntity.add(planet);
			}
		}
		return enemyPlanetsByEntity;
	}

	public static Ship getClosestEnemy(GameMap gameMap, Position entity) {
		Map<Double, Ship> nearbys = gameMap.nearbyShipsByDistance(entity);
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getOwner() != gameMap.getMyPlayerId()) {
				return ship;
			}
		}
		return null;
	}

	public static Ship getClosestEnemyNotDocked(GameMap gameMap, Position entity) {
		Map<Double, Ship> nearbys = gameMap.nearbyShipsByDistance(entity);
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
				continue;
			}
			if (ship.getOwner() != gameMap.getMyPlayerId()) {
				return ship;
			}
		}
		return null;
	}

	public static Ship getClosestEnemyWithOwner(GameMap gameMap, Entity entity, int ownerId) {
		Map<Double, Ship> nearbys = gameMap.nearbyShipsByDistance(entity);
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getOwner() == ownerId) {
				return ship;
			}
		}
		return null;
	}

	public static Ship getClosestDockedEnemy(GameMap gameMap, Entity entity) {
		Map<Double, Ship> nearbys = gameMap.nearbyShipsByDistance(entity);
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getOwner() != gameMap.getMyPlayerId() && ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
				return ship;
			}
		}
		return null;
	}

	public static Ship getClosestDockedEnemyWithNoAllies(GameMap gameMap, Entity entity) {
		Map<Double, Ship> nearbys = gameMap.nearbyShipsByDistance(entity);
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getIgnore()) {
				continue;
			}
			if (ship.getOwner() != gameMap.getMyPlayerId() && ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
				if (filterOutDockedShips(gameMap, getEnemiesNear(gameMap, ship, 10)).size() > 0) {
					continue;
				}
				return ship;

			}
		}
		return null;
	}

	public static Ship getClosestDockedEnemyWithLeastAllies(GameMap gameMap, Entity entity) {
		Map<Double, Ship> nearbys = gameMap.nearbyShipsByDistance(entity);
		Ship current = null;
		int around = 100;
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getOwner() != gameMap.getMyPlayerId() && ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
				int tempAround = filterOutDockedShips(gameMap, getEnemiesNear(gameMap, ship, 10)).size();
				if (tempAround < around) {
					around = tempAround;
					current = ship;
				}
			}
		}
		return current;
	}

	public static Ship getClosestAllyNotDockedNotIgnored(GameMap gameMap, Entity entity) {
		Map<Double, Ship> nearbys = gameMap.nearbyShipsByDistance(entity);
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getIgnore()) {
				continue;
			}
			if (ship.getOwner() == gameMap.getMyPlayerId() && ship.getDockingStatus() == Ship.DockingStatus.Undocked) {
				return ship;
			}
		}
		return null;
	}

	public static Ship getClosestAllyNotDocked(GameMap gameMap, Entity entity) {
		Map<Double, Ship> nearbys = gameMap.nearbyShipsByDistance(entity);
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getOwner() == gameMap.getMyPlayerId() && ship.getDockingStatus() == Ship.DockingStatus.Undocked) {
				return ship;
			}
		}
		return null;
	}

	public static Ship getClosestAllyDocked(GameMap gameMap, Entity entity) {
		Map<Double, Ship> nearbys = gameMap.nearbyShipsByDistance(entity);
		for (Entry<Double, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			if (ship.equals(entity)) {
				continue;
			}
			if (ship.getOwner() == gameMap.getMyPlayerId() && ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
				return ship;
			}
		}
		return null;
	}

	public static Planet getClosestPlanet(GameMap gameMap, Entity entity) {
		Map<Double, Planet> nearbys = gameMap.nearbyPlanetsByDistance(entity);
		for (Entry<Double, Planet> entry : nearbys.entrySet()) {
			Planet planet = entry.getValue();
			if (planet.equals(entity)) {
				continue;
			}
			return planet;
		}
		return null;
	}

	public static ArrayList<Ship> getMyShipsNear(GameMap gameMap, Position entity, double userDistance) {
		ArrayList<Ship> otherAlliesByEntity = new ArrayList<Ship>();
		Map<Integer, Ship> nearbys = gameMap.getMyPlayer().getShips();
		for (Map.Entry<Integer, Ship> entry : nearbys.entrySet()) {
			Ship ship = entry.getValue();
			double distance = ship.getDistanceTo(entity);
			if (ship.equals(entity)) {
				continue;
			}
			if (distance <= userDistance) {
				otherAlliesByEntity.add(ship);
			}
		}
		return otherAlliesByEntity;
	}

	public static ArrayList<Ship> getDockedShipsOnPlanet(GameMap gameMap, Planet planet) {
		ArrayList<Ship> dockedShips = new ArrayList<Ship>();
		List<Integer> idsOfShips = planet.getDockedShips();
		int planetOwnerId = planet.getOwner();
		if (planetOwnerId == gameMap.getMyPlayerId()) {
			return new ArrayList<Ship>();
		}
		for (Integer ship : idsOfShips) {
			dockedShips.add(gameMap.getShip(planetOwnerId, ship));
		}

		return dockedShips;
	}

	/* Checks if the new position is within the map borders */
	public static boolean positionIsOOM(GameMap gameMap, Position position) {
		double xPos = position.getXPos();
		double yPos = position.getYPos();

		return !((gameMap.getWidth() - 0.3 > xPos) && (xPos > 0.3) && (gameMap.getHeight() - 0.3 > yPos) && (yPos > 0.3));
	}

	public static ArrayList<Ship> filterOutDockedShips(GameMap gameMap, ArrayList<Ship> ships) {
		ArrayList<Ship> filtered = new ArrayList<Ship>();
		for (Ship ship : ships) {
			if (ship.getDockingStatus() == Ship.DockingStatus.Undocked) {
				filtered.add(ship);
			}
		}
		return filtered;
	}

	public static ArrayList<Ship> filterOutIgnoreShips(GameMap gameMap, ArrayList<Ship> ships) {
		ArrayList<Ship> ignore = new ArrayList<Ship>();
		for (Ship ship : ships) {
			if (!ship.getIgnore()) {
				ignore.add(ship);
			}
		}
		return ignore;
	}
}
