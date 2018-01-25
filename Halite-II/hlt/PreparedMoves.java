package hlt;

import java.util.ArrayList;
import java.util.Collections;

public class PreparedMoves {
	
	/* Flee: Runs to nearest docked ship while avoiding enemies. */
	public static boolean flee(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Util.getClosestAllyDocked(gameMap, ship);
		if (evadeTo != null) {
			ThrustMove newThrustMove = Navigation.navigateShipToFlee(gameMap, ship, evadeTo, Constants.MAX_SPEED, 1);
			if (newThrustMove != null) {
				moveList.add(newThrustMove);
				ship.setCompleted(true);
				return true;
			}
		}
		return false;
	}

	/* Flee and Panic: Runs to nearest docked ship while avoiding enemies. If not possible, PANIC! */
	public static boolean fleeAndPanic(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Util.getClosestAllyDocked(gameMap, ship);
		if (evadeTo != null) {
			ThrustMove newThrustMove = Navigation.navigateShipToFlee(gameMap, ship, evadeTo, Constants.MAX_SPEED, 2);
			if (newThrustMove != null) {
				moveList.add(newThrustMove);
				ship.setCompleted(true);
				return true;
			}
		}
		return panic(gameMap, ship, moveList);
	}

	/* Evil: Goes to nearest docked enemy and while avoiding undocked enemies. Note: Not really used */
	public static boolean evil(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Util.getClosestDockedEnemy(gameMap, ship);
		if (evadeTo != null) {
			ThrustMove newThrustMove = Navigation.navigateShipToFlee(gameMap, ship, evadeTo, Constants.MAX_SPEED, 4);
			if (newThrustMove != null) {
				moveList.add(newThrustMove);
				ship.setCompleted(true);
				return true;
			}
		}
		return panic(gameMap, ship, moveList);
	}

	/* Prick: Different version of evil? */
	public static boolean prick(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Util.getClosestDockedEnemy(gameMap, ship);
		if (evadeTo != null) {
			ThrustMove newThrustMove = Navigation.navigateShipToAnnoy(gameMap, ship, evadeTo, Constants.MAX_SPEED, 7);
			if (newThrustMove != null) {
				moveList.add(newThrustMove);
				ship.setCompleted(true);
				return true;
			}
		}
		return false;
	}

	/* Panic: When unable to flee without taking damage, go to farthest point away possible */
	public static boolean panic(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeFrom = Util.getClosestEnemy(gameMap, ship);
		ThrustMove newThrustMove = Navigation.navigateShipToPanic(gameMap, ship, evadeFrom, Constants.MAX_SPEED, 9);
		if (newThrustMove != null) {
			moveList.add(newThrustMove);
			ship.setCompleted(true);
			return true;
		}
		return false;
	}

	/* Group Flee: Makes group of ships flee but in reverse order */
	public static boolean groupFlee(GameMap gameMap, ArrayList<Ship> myShips, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> myShipsSorted = Util.sortShipsByDistanceFrom(myShips, ship);
		Collections.reverse(myShipsSorted);
		for (Ship myShip : myShipsSorted) {
			if (myShip.getCompleted() || myShip.getIgnore()	|| myShip.getDockingStatus() != Ship.DockingStatus.Undocked) {
				continue;
			}
			for (Ship myShipsAgain : myShipsSorted) {
				if (myShipsAgain.getNewPos() != null) {
					ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, myShip, myShipsAgain.getNewPos(), Constants.MAX_SPEED, true, 90, Math.PI / 180, 69);
					if (newThrustMove != null) {
						moveList.add(newThrustMove);
						myShip.setCompleted(true);
						break;
					}
				}
			}
			if (!myShip.getCompleted()) {
				Ship evadeTo = Util.getClosestAllyDocked(gameMap, ship);
				if (evadeTo != null) {
					ThrustMove newThrustMove = Navigation.navigateShipToFlee(gameMap, myShip, evadeTo, Constants.MAX_SPEED, 70);
					if (newThrustMove != null) {
						moveList.add(newThrustMove);
						myShip.setCompleted(true);
						continue;
					}
				}
			}
		}
		return true;
	}
	
	/* Group Fight: Generic fighting when my ships around me are larger than enemies */
	public static boolean groupFight(GameMap gameMap, ArrayList<Ship> myShips, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> myShipsSorted = Util.sortShipsByDistanceFrom(myShips, ship);
		for (Ship myShip : myShipsSorted) {
			if (myShip.getCompleted() || myShip.getIgnore() || myShip.getDockingStatus() != Ship.DockingStatus.Undocked) {
				continue;
			}
			for (Ship myShipsAgain : myShipsSorted) {
				if (myShipsAgain.getNewPos() != null) {
					ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, myShip, myShipsAgain.getNewPos(), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180, 57);
					if (newThrustMove != null) {
						moveList.add(newThrustMove);
						myShip.setCompleted(true);
						break;
					}
				}
			}
			if (!myShip.getCompleted()) {
				ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, myShip, myShip.getClosestPointShip(ship), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180, 51);
				if (newThrustMove != null) {
					moveList.add(newThrustMove);
					myShip.setCompleted(true);
					continue;
				}
			}
		}
		return true;
	}
	
	/* Group Fight 2: Generic fighting when my ships around me are going to dive for enemie's docked ships */
	public static boolean groupFight2(GameMap gameMap, ArrayList<Ship> myShips, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> myShipsSorted = Util.sortShipsByDistanceFrom(myShips, ship);
		for (Ship myShip : myShipsSorted) {
			if (myShip.getCompleted() || myShip.getIgnore() || myShip.getDockingStatus() != Ship.DockingStatus.Undocked) {
				continue;
			}
			for (Ship myShipsAgain : myShipsSorted) {
				if (myShipsAgain.getNewPos() != null) {
					ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, myShip, myShipsAgain.getNewPos(), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180, 57);
					if (newThrustMove != null) {
						moveList.add(newThrustMove);
						myShip.setCompleted(true);
						break;
					}
				}
			}
			if (!myShip.getCompleted()) {
				ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, myShip, myShip.getClosestPointShip(ship), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180, 54);
				if (newThrustMove != null) {
					moveList.add(newThrustMove);
					myShip.setCompleted(true);
					continue;
				}
			}
		}
		return true;
	}
	
	/* Group: Groups ships together by going to the avg pos */
	public static boolean group(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> ships = Util.filterOutDockedShips(gameMap, Util.getMyShipsNear(gameMap, ship, 20));
		ships.add(ship);
		int xSum = 0;
        int ySum = 0;
        boolean ignore = false;
        Position targetPos = null;
		for (Ship ally : ships) {
        	if (ally.getNewPos() != null) {
        		xSum += ally.getNewPos().getXPos();
            	ySum += ally.getNewPos().getYPos();
        	}
        	else {
        		xSum += ally.getXPos();
            	ySum += ally.getYPos();
        	}
        	if (ally.getGroupingPos() != null) {
        		ignore = true;
        		targetPos = ally.getGroupingPos();
        		break;
        	}
        }
		if (!ignore) {
        	targetPos = new Position(xSum / ships.size(), ySum / ships.size());
        }
		ArrayList<Ship> myShipsSorted = Util.sortShipsByDistanceFrom(ships, targetPos);
		for (Ship myShip : myShipsSorted) {
			if (myShip.getCompleted() || myShip.getIgnore() || myShip.getDockingStatus() != Ship.DockingStatus.Undocked) {
				continue;
			}
			ThrustMove newThrustMove = Navigation.navigateShipTowardsTargetGroup(gameMap, myShip, myShip.getClosestPointGroup(targetPos), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180 , 5);
			if (newThrustMove != null) {
		        moveList.add(newThrustMove); 
		    }
			myShip.setGroupingPos(targetPos);
	        myShip.setCompleted(true);
		}
		return true;
	}
	
	/* Singular Group: Moves ship to avg pos of new pos or current pos */
	public static boolean singularGroup(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> ships = Util.filterOutDockedShips(gameMap, Util.getMyShipsNear(gameMap, ship, 20));
		if (ships.size() > 0) {
			ThrustMove newThrustMove = Navigation.navigateShipToGroup(gameMap, ship, ships, Constants.MAX_SPEED, 13);
			if (newThrustMove != null) {
				moveList.add(newThrustMove);
				ship.setCompleted(true);
				return true;
			}
		}
		return true;
	}
	
	/* Hide: Hides in corner */
	public static boolean hide(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Position hide = new Position(gameMap.getWidth(), gameMap.getHeight());
		Position corner2 = new Position(gameMap.getWidth(), 0);
		Position corner3 = new Position(0, gameMap.getHeight());
		Position corner4 = new Position(0, 0);
		if (ship.getDistanceTo(hide) > ship.getDistanceTo(corner2)) {
			hide = corner2;
		}
		if (ship.getDistanceTo(hide) > ship.getDistanceTo(corner3)) {
			hide = corner3;
		}
		if (ship.getDistanceTo(hide) > ship.getDistanceTo(corner4)) {
			hide = corner4;
		}
		ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship, hide, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI / 180, 15);
		if (newThrustMove != null) {
			moveList.add(newThrustMove);
			ship.setCompleted(true);
			return true;
		}
		return false;
	}
}