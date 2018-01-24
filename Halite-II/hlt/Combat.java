package hlt;

import java.util.ArrayList;
import java.util.Collections;

public class Combat {
	/* Combat: Group of methods that I called combat for some reason  */
	
	public static boolean flee(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Helper.getClosestAllyDocked(gameMap, ship);
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

	public static boolean fleeAndPanic(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Helper.getClosestAllyDocked(gameMap, ship);
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

	public static boolean run(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Helper.getClosestAllyDocked(gameMap, ship);
		if (evadeTo != null) {
			ThrustMove newThrustMove = Navigation.navigateShipToRun(gameMap, ship, evadeTo, Constants.MAX_SPEED, 3);
			if (newThrustMove != null) {
				moveList.add(newThrustMove);
				ship.setCompleted(true);
				return true;
			}
		}
		return panic(gameMap, ship, moveList);
	}

	public static boolean evil(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Helper.getClosestDockedEnemy(gameMap, ship);
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
	
	public static boolean groupFlee(GameMap gameMap, ArrayList<Ship> myShips, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> myShipsSorted = Helper.sortShipsByDistanceFrom(myShips, ship);
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
				Ship evadeTo = Helper.getClosestAllyDocked(gameMap, ship);
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
	
	public static boolean groupFight(GameMap gameMap, ArrayList<Ship> myShips, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> myShipsSorted = Helper.sortShipsByDistanceFrom(myShips, ship);
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
	
	public static boolean groupFight2(GameMap gameMap, ArrayList<Ship> myShips, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> myShipsSorted = Helper.sortShipsByDistanceFrom(myShips, ship);
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
	
	public static boolean group(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> ships = Helper.filterOutDockedShips(gameMap, Helper.getMyShipsNear(gameMap, ship, 20));
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
		ArrayList<Ship> myShipsSorted = Helper.sortShipsByDistanceFrom(ships, targetPos);
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
	
	public static boolean singularGroup(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		ArrayList<Ship> ships = Helper.filterOutDockedShips(gameMap, Helper.getMyShipsNear(gameMap, ship, 20));
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

	public static boolean goThru(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Helper.getClosestDockedEnemy(gameMap, ship);
		if (evadeTo != null) {
			ThrustMove newThrustMove = Navigation.navigateShipToAnnoy(gameMap, ship, evadeTo, Constants.MAX_SPEED, 6);
			if (newThrustMove != null) {
				moveList.add(newThrustMove);
				ship.setCompleted(true);
				return true;
			}
		}
		return flee(gameMap, ship, moveList);
	}

	public static boolean prick(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Helper.getClosestDockedEnemy(gameMap, ship);
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

	public static boolean prickNear(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeTo = Helper.getClosestDockedEnemy(gameMap, ship);
		if (evadeTo != null) {
			ThrustMove newThrustMove = Navigation.navigateShipToAnnoy(gameMap, ship, evadeTo, Constants.MAX_SPEED, 8);
			if (newThrustMove != null) {
				moveList.add(newThrustMove);
				ship.setCompleted(true);
				return true;
			}
		}
		return false;
	}

	public static boolean panic(GameMap gameMap, Ship ship, ArrayList<Move> moveList) {
		Ship evadeFrom = Helper.getClosestEnemy(gameMap, ship);
		ThrustMove newThrustMove = Navigation.navigateShipToPanic(gameMap, ship, evadeFrom, Constants.MAX_SPEED, 9);
		if (newThrustMove != null) {
			moveList.add(newThrustMove);
			ship.setCompleted(true);
			return true;
		}
		return false;
	}
	
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