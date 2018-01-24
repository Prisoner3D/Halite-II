package hlt;

import java.util.ArrayList;

public class Navigation {
	/* Navigation: Absolute nightmare */
	
	public static ThrustMove navigateShipToDock(final GameMap gameMap, final Ship ship, final Entity dockTarget, final int maxThrust, final int message) {
        final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
        final boolean avoidObstacles = true;
        final double angularStepRad = Math.PI/180.0;
        
        final Position targetPos = ship.getClosestPointPlanet(dockTarget);
        return navigateShipTowardsTarget(gameMap, ship, targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad, message);
    }
    
	public static ThrustMove navigateShipToShip(final GameMap gameMap, final Ship ship, final Ship target, final int maxThrust, final int message) {
		final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
		final boolean avoidObstacles = true;
		final double angularStepRad = Math.PI / 180.0;

		final Position targetPos = ship.getClosestPointShip(target);
		return navigateShipTowardsTarget(gameMap, ship, targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad, message);
	}
    
	public static ThrustMove navigateShipToShipDirectly(final GameMap gameMap, final Ship ship, final Ship target, final int maxThrust, final int message) {
		final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
		final boolean avoidObstacles = true;
		final double angularStepRad = Math.PI / 180.0;

		final Position targetPos = target.getPos();
		return navigateShipTowardsTarget(gameMap, ship, targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad, message);
	}

	public static ThrustMove navigateShipToDefend(final GameMap gameMap, final Ship ship, final Ship enemy,	final Ship target, final int maxThrust, final int message) {
		final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
		final boolean avoidObstacles = true;
		final double angularStepRad = Math.PI / 180.0;

		final Position targetPos = enemy.getClosestPoint(target);
		return navigateShipTowardsTarget(gameMap, ship, targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad, message);
	}

	public static ThrustMove navigateShipToGroup(final GameMap gameMap, final Ship ship, final ArrayList<Ship> allies, final int maxThrust, final int message) {
		final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
		final boolean avoidObstacles = true;
		final double angularStepRad = Math.PI / 180.0;

		int xSum = 0;
		int ySum = 0;
		boolean ignore = false;
		Position targetPos = null;

		for (Ship ally : allies) {
			xSum += ally.getXPos();
			ySum += ally.getYPos();
			if (ally.getGroupingPos() != null) {
				ignore = true;
				targetPos = ally.getGroupingPos();
				break;
			}
		}
		if (!ignore) {
			targetPos = new Position(xSum / allies.size(), ySum / allies.size());
		}
		ship.setGroupingPos(targetPos);
		return navigateShipTowardsTarget(gameMap, ship, ship.getClosestPointGroup(targetPos), maxThrust, avoidObstacles, maxCorrections, angularStepRad, message);
	}
    
	public static ThrustMove navigateShipToFlee(final GameMap gameMap, final Ship ship, final Ship target, final int maxThrust, final int message) {
        final int maxCorrections = 360;
        final boolean avoidObstacles = true;
        final double angularStepRad = Math.PI/180.0;
        final Position targetPos = ship.getClosestPoint(target);
        final int increment = Helper.determineIncrement(gameMap);
        
    	double angleRad = ship.orientTowardsInRad(target);
        double distance = ship.getDistanceTo(targetPos);
        int thrust;
        if (distance < 8) {
        	thrust = maxThrust;
        }
        else if (distance < 15) {
            // Do not round up, since overshooting might cause collision.
            thrust = (int) (15 - distance);
        }
        else {
        	thrust = maxThrust;
        }
    	double newTargetDx = Math.cos(angleRad) * thrust;
    	double newTargetDy = Math.sin(angleRad) * thrust;
    	Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
    	double newAngleRad = ship.orientTowardsInRad(newTarget);
    	
		int iteration = 0;
		while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)
				|| !Helper.filterOutDockedShips(gameMap, Helper.getEnemiesNear(gameMap, newTarget, 14)).isEmpty()) && iteration <= maxCorrections) {
			
			newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
			newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
			newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
			newAngleRad = ship.orientTowardsInRad(newTarget);

			if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)
					|| !Helper.filterOutDockedShips(gameMap, Helper.getEnemiesNear(gameMap, newTarget, 14)).isEmpty()) {
				
				newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
				newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
				newAngleRad = ship.orientTowardsInRad(newTarget);
			}
			iteration = iteration + increment;
		}
		if (iteration >= maxCorrections) {
			return null;
		}
		
        final int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(newAngleRad), message);
        ship.setNewPos(newTarget);
        return new ThrustMove(ship, angleDeg, thrust);
    }
    
	public static ThrustMove navigateShipToAnnoy(final GameMap gameMap, final Ship ship, final Ship target, final int maxThrust, final int message) {
		final int maxCorrections = 360;
		final boolean avoidObstacles = true;
		final double angularStepRad = Math.PI / 180.0;
		final Position targetPos = ship.getClosestPointShip(target);
		final int increment = Helper.determineIncrement(gameMap);

		double angleRad = ship.orientTowardsInRad(targetPos);
		int thrust = maxThrust;

		double newTargetDx = Math.cos(angleRad) * thrust;
		double newTargetDy = Math.sin(angleRad) * thrust;
		Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
		double newAngleRad = ship.orientTowardsInRad(newTarget);

		int iteration = 0;
		while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)
				|| !Helper.filterOutDockedShips(gameMap, Helper.getEnemiesNear(gameMap, newTarget, 14)).isEmpty()) && iteration <= maxCorrections) {
			
			newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
			newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
			newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
			newAngleRad = ship.orientTowardsInRad(newTarget);

			if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)
					|| !Helper.filterOutDockedShips(gameMap, Helper.getEnemiesNear(gameMap, newTarget, 14)).isEmpty()) {
				
				newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
				newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
				newAngleRad = ship.orientTowardsInRad(newTarget);
			}
			iteration = iteration + increment;
		}
		if (iteration >= maxCorrections) {
			return null;
		}

		final int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(newAngleRad), message);
		ship.setNewPos(newTarget);
		return new ThrustMove(ship, angleDeg, thrust);
	}
    
	public static ThrustMove navigateShipToPanic(final GameMap gameMap, final Ship ship, final Ship target, final int maxThrust, final int message) {
		final int maxCorrections = 360;
		final boolean avoidObstacles = true;
		final double angularStepRad = Math.PI / 180.0;
		final Position targetPos = ship.getClosestPointShip(target);
		final int increment = Helper.determineIncrement(gameMap);

		double angleRad = ship.orientTowardsInRad(targetPos) - Math.PI;
		int thrust = maxThrust;
		double newTargetDx = Math.cos(angleRad) * thrust;
		double newTargetDy = Math.sin(angleRad) * thrust;
		Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
		double newAngleRad = ship.orientTowardsInRad(newTarget);

		double absLowest = 0;
		Position lowestPos = null;

		if (avoidObstacles) {
			for (int iteration = 0; iteration < maxCorrections; iteration = iteration + increment) {
				newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
				newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);

				if (gameMap.objectsBetween(ship, newTarget).isEmpty() && !Helper.positionIsOOM(gameMap, newTarget)) {
					double current = newTarget.getDistanceTo(Helper.getClosestEnemyNotDocked(gameMap, newTarget));
					if (current > absLowest) {
						absLowest = current;
						lowestPos = newTarget;
					}
				}

				newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
				newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);

				if (gameMap.objectsBetween(ship, newTarget).isEmpty() && !Helper.positionIsOOM(gameMap, newTarget)) {
					double current = newTarget.getDistanceTo(Helper.getClosestEnemyNotDocked(gameMap, newTarget));
					if (current > absLowest) {
						absLowest = current;
						lowestPos = newTarget;
					}
				}
			}
		}
		if (lowestPos != null) {
			newAngleRad = ship.orientTowardsInRad(lowestPos);
			final int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(newAngleRad), 107);
			ship.setNewPos(lowestPos);
			return new ThrustMove(ship, angleDeg, thrust);
		}
		return null;
	}
    
	/* To lazy to do recursion so I hard coded it :( */
	public static ThrustMove navigateShipTowardsTarget(final GameMap gameMap, final Ship ship, final Position targetPos,
			final int maxThrust, final boolean avoidObstacles, final int maxCorrections, final double angularStepRad, final int message) {
		double angleRad = ship.orientTowardsInRad(targetPos);
		double distance = ship.getDistanceTo(targetPos);
		final int increment = Helper.determineIncrement(gameMap);
		
		int thrust;
		if (distance < maxThrust) {
			// Do not round up, since overshooting might cause collision.
			thrust = (int) distance;
		} else {
			thrust = maxThrust;
		}
		double newTargetDx = Math.cos(angleRad) * thrust;
		double newTargetDy = Math.sin(angleRad) * thrust;
		Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
		double newAngleRad = ship.orientTowardsInRad(newTarget);

		int iteration = 0;
		while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= maxCorrections) {
			newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
			newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
			newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
			newAngleRad = ship.orientTowardsInRad(newTarget);

			if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
				newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
				newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
				newAngleRad = ship.orientTowardsInRad(newTarget);
			}
			iteration = iteration + increment;
		}
		if (iteration >= maxCorrections) {
			if (thrust >= 1) {
				thrust--;
				newTargetDx = Math.cos(angleRad) * thrust;
				newTargetDy = Math.sin(angleRad) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
				newAngleRad = ship.orientTowardsInRad(newTarget);

				iteration = 0;
				while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= maxCorrections) {
					newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
					newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
					newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
					newAngleRad = ship.orientTowardsInRad(newTarget);

					if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
						newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
						newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
						newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
						newAngleRad = ship.orientTowardsInRad(newTarget);
					}
					iteration = iteration + increment;
				}
				if (iteration >= maxCorrections) {
					if (thrust >= 1) {
						thrust--;
						newTargetDx = Math.cos(angleRad) * thrust;
						newTargetDy = Math.sin(angleRad) * thrust;
						newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
						newAngleRad = ship.orientTowardsInRad(newTarget);

						iteration = 0;
						while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= 90) {
							newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
							newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
							newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
							newAngleRad = ship.orientTowardsInRad(newTarget);

							if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
								newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
								newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
								newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
								newAngleRad = ship.orientTowardsInRad(newTarget);
							}
							iteration = iteration + increment;
						}
						if (iteration >= 90) {
							return null;
						}
					} else {
						return null;
					}
				}
			} else {
				return null;
			}
		}

		final int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(newAngleRad), message);
		ship.setNewPos(newTarget);
		return new ThrustMove(ship, angleDeg, thrust);
	}
    
	public static ThrustMove navigateShipTowardsTargetCombat(final GameMap gameMap, final Ship ship, final Position targetPos, 
			final int maxThrust, final boolean avoidObstacles, final int maxCorrections, final double angularStepRad, final int message) {
		double angleRad = ship.orientTowardsInRad(targetPos);
		double distance = ship.getDistanceTo(targetPos);
		final int increment = Helper.determineIncrement(gameMap);
		
		int thrust;
		if (distance < maxThrust) {
			// Do not round up, since overshooting might cause collision.
			thrust = (int) distance;
		} else {
			thrust = maxThrust;
		}
		double newTargetDx = Math.cos(angleRad) * thrust;
		double newTargetDy = Math.sin(angleRad) * thrust;
		Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
		double newAngleRad = ship.orientTowardsInRad(newTarget);

		int iteration = 0;
		while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= maxCorrections) {
			newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
			newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
			newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
			newAngleRad = ship.orientTowardsInRad(newTarget);

			if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
				newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
				newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
				newAngleRad = ship.orientTowardsInRad(newTarget);
			}
			iteration = iteration + increment;
		}
		if (iteration >= maxCorrections) {
			if (thrust >= 1) {
				thrust--;
				newTargetDx = Math.cos(angleRad) * thrust;
				newTargetDy = Math.sin(angleRad) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
				newAngleRad = ship.orientTowardsInRad(newTarget);

				iteration = 0;
				while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= maxCorrections) {
					newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
					newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
					newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
					newAngleRad = ship.orientTowardsInRad(newTarget);

					if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
						newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
						newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
						newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
						newAngleRad = ship.orientTowardsInRad(newTarget);
					}
					iteration = iteration + increment;
				}
				if (iteration >= maxCorrections) {
					if (thrust >= 1) {
						thrust--;
						newTargetDx = Math.cos(angleRad) * thrust;
						newTargetDy = Math.sin(angleRad) * thrust;
						newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
						newAngleRad = ship.orientTowardsInRad(newTarget);

						iteration = 0;
						while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= maxCorrections) {
							newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
							newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
							newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
							newAngleRad = ship.orientTowardsInRad(newTarget);

							if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
								newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
								newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
								newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
								newAngleRad = ship.orientTowardsInRad(newTarget);
							}
							iteration = iteration + increment;
						}
						if (iteration >= maxCorrections) {
							if (thrust >= 1) {
								thrust--;
								newTargetDx = Math.cos(angleRad) * thrust;
								newTargetDy = Math.sin(angleRad) * thrust;
								newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
								newAngleRad = ship.orientTowardsInRad(newTarget);

								iteration = 0;
								while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= 180) {
									newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
									newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
									newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
									newAngleRad = ship.orientTowardsInRad(newTarget);

									if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
										newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
										newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
										newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
										newAngleRad = ship.orientTowardsInRad(newTarget);
									}
									iteration = iteration + increment;
								}
								if (iteration >= 180) {
									return null;
								}
							} else {
								return null;
							}
						}
					} else {
						return null;
					}
				}
			} else {
				return null;
			}
		}

		final int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(newAngleRad), message);
		ship.setNewPos(newTarget);
		return new ThrustMove(ship, angleDeg, thrust);
	}
    
	public static ThrustMove navigateShipTowardsTargetGroup(final GameMap gameMap, final Ship ship, final Position targetPos, 
			final int maxThrust, final boolean avoidObstacles, final int maxCorrections, final double angularStepRad, final int message) {
		double angleRad = ship.orientTowardsInRad(targetPos);
		double distance = ship.getDistanceTo(targetPos);
		final int increment = Helper.determineIncrement(gameMap);
		
		int thrust;
		if (distance < maxThrust) {
			// Do not round up, since overshooting might cause collision.
			thrust = (int) distance;
		} else {
			thrust = maxThrust;
		}
		double newTargetDx = Math.cos(angleRad) * thrust;
		double newTargetDy = Math.sin(angleRad) * thrust;
		Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
		double newAngleRad = ship.orientTowardsInRad(newTarget);

		int iteration = 0;
		while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= 360) {
			newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
			newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
			newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
			newAngleRad = ship.orientTowardsInRad(newTarget);

			if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
				newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
				newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
				newAngleRad = ship.orientTowardsInRad(newTarget);
			}
			iteration = iteration + 10;
		}
		if (iteration >= 360) {
			if (thrust >= 1) {
				thrust--;
				newTargetDx = Math.cos(angleRad) * thrust;
				newTargetDy = Math.sin(angleRad) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
				newAngleRad = ship.orientTowardsInRad(newTarget);

				iteration = 0;
				while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= 360) {
					newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
					newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
					newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
					newAngleRad = ship.orientTowardsInRad(newTarget);

					if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
						newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
						newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
						newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
						newAngleRad = ship.orientTowardsInRad(newTarget);
					}
					iteration = iteration + 10;
				}
				if (iteration >= 360) {
					if (thrust >= 1) {
						thrust--;
						newTargetDx = Math.cos(angleRad) * thrust;
						newTargetDy = Math.sin(angleRad) * thrust;
						newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
						newAngleRad = ship.orientTowardsInRad(newTarget);

						iteration = 0;
						while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) && iteration <= maxCorrections * 2) {
							newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
							newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
							newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
							newAngleRad = ship.orientTowardsInRad(newTarget);

							if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)) {
								newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
								newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
								newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
								newAngleRad = ship.orientTowardsInRad(newTarget);
							}
							iteration = iteration + increment;
						}
						if (iteration >= maxCorrections * 2) {
							return null;
						}
					} else {
						return null;
					}
				}
			} else {
				return null;
			}
		}

		final int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(newAngleRad), message);
		ship.setNewPos(newTarget);
		return new ThrustMove(ship, angleDeg, thrust);
	}
	
	/* UNUSED: Literally navigateShipToAnnoy but not using getClosestPointShip */
	public static ThrustMove navigateShipToRun(final GameMap gameMap, final Ship ship, final Ship target, final int maxThrust, final int message) {
		final int maxCorrections = 360;
		final boolean avoidObstacles = true;
		final double angularStepRad = Math.PI / 180.0;
		final int increment = Helper.determineIncrement(gameMap);

		double angleRad = ship.orientTowardsInRad(target);
		int thrust = maxThrust;

		double newTargetDx = Math.cos(angleRad) * thrust;
		double newTargetDy = Math.sin(angleRad) * thrust;
		Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
		double newAngleRad = ship.orientTowardsInRad(newTarget);

		int iteration = 0;
		while (avoidObstacles && (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)
				|| !Helper.filterOutDockedShips(gameMap, Helper.getEnemiesNear(gameMap, newTarget, 14)).isEmpty()) && iteration <= maxCorrections) {
			
			newTargetDx = Math.cos(angleRad + (angularStepRad * iteration)) * thrust;
			newTargetDy = Math.sin(angleRad + (angularStepRad * iteration)) * thrust;
			newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
			newAngleRad = ship.orientTowardsInRad(newTarget);

			if (!gameMap.objectsBetween(ship, newTarget).isEmpty() || Helper.positionIsOOM(gameMap, newTarget)
					|| !Helper.filterOutDockedShips(gameMap, Helper.getEnemiesNear(gameMap, newTarget, 14)).isEmpty()) {
				
				newTargetDx = Math.cos(angleRad - (angularStepRad * iteration)) * thrust;
				newTargetDy = Math.sin(angleRad - (angularStepRad * iteration)) * thrust;
				newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);
				newAngleRad = ship.orientTowardsInRad(newTarget);
			}
			iteration = iteration + increment;
		}
		if (iteration >= maxCorrections) {
			return null;
		}

		final int angleDeg = Util.new_angle_with_message(Util.angleRadToDegClipped(newAngleRad), message);
		ship.setNewPos(newTarget);
		return new ThrustMove(ship, angleDeg, thrust);
	}
}
