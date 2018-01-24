package hlt;

import java.util.ArrayList;
import java.util.Map;

public class Cheese {
	/* Cheese: Really dumb strat that seems to work */
	
	public static boolean initiateCheese(GameMap gameMap) {
        if (gameMap.getAllPlayers().size() == 2) {
        	Player opponent2Player = gameMap.getOtherEnemyPlayer();
        	// Gets first enemy ship
        	Map<Integer, Ship> enemyShipsMap = opponent2Player.getShips();
        	Ship enemyShip = null;
        	for (Integer id : enemyShipsMap.keySet()) {
        		if (id == 0 || id == 3) { 
        			enemyShip = enemyShipsMap.get(id);
        		}
        	}
        	// Gets my ships in terms of enemy ship
        	Map<Double, Ship> nearbyAllShips = gameMap.nearbyShipsByDistance(enemyShip);
        	ArrayList<Ship> myShipsSorted = new ArrayList<Ship>();
        	for (double distance : nearbyAllShips.keySet()) {
        		if (nearbyAllShips.get(distance).getOwner() == gameMap.getMyPlayerId()) {
        			myShipsSorted.add(nearbyAllShips.get(distance));
        		}
        	}
        	// Gets enemy ships in terms of my ships
        	Map<Double, Ship> nearbyAllShipsEnemy = gameMap.nearbyShipsByDistance(myShipsSorted.get(0));
        	ArrayList<Ship> enemyShipsSorted = new ArrayList<Ship>();
        	for (double distance : nearbyAllShipsEnemy.keySet()) {
        		if (nearbyAllShipsEnemy.get(distance).getOwner() != gameMap.getMyPlayerId()) {
        			enemyShipsSorted.add(nearbyAllShipsEnemy.get(distance));
        		}
        	}
        	ArrayList<Planet> nearPlanets = new ArrayList<Planet>();
        	Map<Double, Planet> nearbyPlanets = gameMap.nearbyPlanetsByDistance(myShipsSorted.get(0));
        	for (double distance : nearbyPlanets.keySet()) {
        		nearPlanets.add(nearbyPlanets.get(distance));
        	}
        	if (myShipsSorted.get(0).getDistanceTo(enemyShipsSorted.get(0)) <= 80) {
        		return true;
        	}
        	if (nearPlanets.get(0).getId() == 1 || nearPlanets.get(0).getId() == 0 || nearPlanets.get(0).getId() == 2 || nearPlanets.get(0).getId() == 3) {
        		//return true;
        	}
        }
		return false;
	}
	
	public static double initiateCheeseGap(GameMap gameMap) {
		Map<Integer, Ship> enemyShipsMap = gameMap.getMyPlayer().getShips();
		Ship meShip = null;
		for (Integer id : enemyShipsMap.keySet()) {
			meShip = enemyShipsMap.get(id);
			if (meShip != null) {
				break;
			}
		}
		Map<Double, Ship> enemyShips = gameMap.nearbyEnemyShipsByDistance(meShip);
		for (Double distance : enemyShips.keySet()) {
			return distance;
		}
		return -1;
	}
	
	public static void cheeseMode(GameMap gameMap, int turn, ArrayList<Move> moveList)
	{
		Player opponent2Player = gameMap.getOtherEnemyPlayer();
		Map<Integer, Ship> enemyShipsMap = opponent2Player.getShips();
		ArrayList<Ship> enemyShips = new ArrayList<Ship>();
		for (Integer id : enemyShipsMap.keySet()) {
			enemyShips.add(enemyShipsMap.get(id));
		}

		Map<Double, Ship> nearbyAllShips = gameMap.nearbyShipsByDistance(enemyShips.get(0));
		ArrayList<Ship> myShipsSorted = new ArrayList<Ship>();
		for (double distance : nearbyAllShips.keySet()) {
			if (nearbyAllShips.get(distance).getOwner() == gameMap.getMyPlayerId()) {
				myShipsSorted.add(nearbyAllShips.get(distance));
			}
		}
		// Collections.reverse(myShipsSorted);
		Map<Double, Ship> nearbyAllShipsEnemy = gameMap.nearbyShipsByDistance(myShipsSorted.get(0));
		ArrayList<Ship> enemyShipsSorted = new ArrayList<Ship>();
		for (double distance : nearbyAllShipsEnemy.keySet()) {
			if (nearbyAllShipsEnemy.get(distance).getOwner() != gameMap.getMyPlayerId()) {
				enemyShipsSorted.add(nearbyAllShipsEnemy.get(distance));
			}
		}
    	// Don't make fun of my names
    	Ship captain = null;
    	Ship follower1 = null;
    	Ship follower2 = null;
		for (Ship ship : myShipsSorted) {
			if (captain == null) {
				captain = ship;
			} else if (follower1 == null) {
				follower1 = ship;
			} else if (follower2 == null) {
				follower2 = ship;
			}
		}
		// If first turn then assemble...
		if (turn == 0) {
			boolean orient;
			// Determines the correct orientation
			if (Util.angleRadToDegClipped(captain.orientTowardsInRad(enemyShipsSorted.get(0))) == 90 || Util.angleRadToDegClipped(captain.orientTowardsInRad(enemyShipsSorted.get(0))) == 270) {
				orient = true;
			} else {
				orient = false;
			}
			// Sets up the ships based on orientation
			if (orient) {
				for (Ship ship : myShipsSorted) {
					if (ship == captain) {
						double angleRad = ship.orientTowardsInRad(enemyShipsSorted.get(0));
						int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(angleRad) - 32, 99);
						ThrustMove newThrustMove = new ThrustMove(ship, angleDeg, 2);
						if (newThrustMove != null) {
							moveList.add(newThrustMove);
						}
					} else if (ship == follower1) {
						double angleRad = ship.orientTowardsInRad(enemyShipsSorted.get(0));
						int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(angleRad) + 13, 99);
						ThrustMove newThrustMove = new ThrustMove(ship, angleDeg, 4);
						if (newThrustMove != null) {
							moveList.add(newThrustMove);
						}
					} else if (ship == follower2) {
						double angleRad = ship.orientTowardsInRad(enemyShipsSorted.get(0));
						int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(angleRad), 99);
						ship.setAngle(angleDeg);
						ThrustMove newThrustMove = new ThrustMove(ship, angleDeg, 6);
						if (newThrustMove != null) {
							moveList.add(newThrustMove);
						}
					}
				}
			} else {
				for (Ship ship : myShipsSorted) {
					if (ship == captain) {
						double angleRad = ship.orientTowardsInRad(enemyShipsSorted.get(0));
						int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(angleRad), 99);
						ship.setAngle(angleDeg);
						ThrustMove newThrustMove = new ThrustMove(ship, angleDeg, 6);
						if (newThrustMove != null) {
							moveList.add(newThrustMove);
						}
					} else if (ship == follower1) {
						double angleRad = ship.orientTowardsInRad(enemyShipsSorted.get(0));
						int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(angleRad) + 15, 99);
						ThrustMove newThrustMove = new ThrustMove(ship, angleDeg, 7);
						if (newThrustMove != null) {
							moveList.add(newThrustMove);
						}
					} else if (ship == follower2) {
						double angleRad = ship.orientTowardsInRad(enemyShipsSorted.get(0));
						int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(angleRad) - 15, 99);
						ThrustMove newThrustMove = new ThrustMove(ship, angleDeg, 7);
						if (newThrustMove != null) {
							moveList.add(newThrustMove);
						}
					}
				}
			}
		}
		else {
			// ...else attack
			Ship target = enemyShipsSorted.get(0);
			for (Ship enemy : enemyShipsSorted) {
				if (enemy.getDockingStatus() != Ship.DockingStatus.Undocked) {
					target = enemy;
					break;
				}
			}
			for (Ship ship : myShipsSorted) {
				ThrustMove newThrustMove = Navigation.navigateShipToShip(gameMap, ship, target, Constants.MAX_SPEED, 99);
				if (newThrustMove != null) {
					moveList.add(newThrustMove);
				}
			}
		}
	}
}
