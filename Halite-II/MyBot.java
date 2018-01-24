import java.util.ArrayList;
import java.util.Map;

import hlt.*;

public class MyBot {
	public static void main(final String[] args)
	{
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("hi my name is bot");
        final ArrayList<Move> moveList = new ArrayList<>();
        
        // Stuff that I never added:
        
        // "shifting" while attacking docked ships
        // predict enemy ships movement for interception
        // in 4p game if enemyships is really big lead them to destroy other players
        // fix cheese
        // added to planet consideration
        
        boolean cheese = Cheese.initiateCheese(gameMap); // false
        int turn = -1;
        
        while (true) {
        	moveList.clear();
            gameMap.updateMap(Networking.readLineIntoMetadata());
            
            turn++;
            
            if (turn > 40) {
            	cheese = false;
            }
            
            if (cheese) {
            	Cheese.cheeseMode(gameMap, turn, moveList);
            } else {
            	/** 
            	 * Panic: When to give up and hide in corner.
            	**/
            	boolean panic = false;
                ArrayList<Ship> myShips = new ArrayList<Ship> (gameMap.getMyPlayer().getShips().values());
                if (myShips.size()  < gameMap.getAllShips().size() / Constants.PANIC) {
                	panic = true;
                }
                
                /** 
            	 * Defending: Identifying and sending ships to defend.
            	**/
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                	if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                		ArrayList<Ship> possibleEnemies = Helper.filterOutDockedShips(gameMap, Helper.getEnemiesNear(gameMap, ship, Constants.DEFENDING));
                		ArrayList<Ship> net = Helper.getMyShipsNear(gameMap, ship, Constants.DEFENDING + 15);
                		
                		if (net.size() >= possibleEnemies.size()) {
                			for (Ship enemy : possibleEnemies) {
                    			if (!enemy.getIgnore()) {
                    				Ship savior = Helper.getClosestAllyNotDockedNotIgnored(gameMap, ship);
                    				/*
                    				if (savior != null && ship.getDistanceTo(savior) > ship.getDistanceTo(enemy)) {
                    					savior = Helper.getClosestAllyNotDockedNotIgnored(gameMap, ship);
                    				}
                    				*/
                    				if (savior != null) {
                    					/** When there is no need for defending a ship **/
                    					if (savior.getDistanceTo(ship) + 5 < enemy.getDistanceTo(ship) && ship.getDistanceTo(enemy) > 10) { 
                    						continue;
                    					}
                    					
                    					/*
                    					if ((savior.getDistanceTo(ship) + 5 < enemy.getDistanceTo(ship) && savior.getDistanceTo(enemy) + 5 < ship.getDistanceTo(enemy)) && ship.getDistanceTo(enemy) > 10) { 
                    						continue;
                    					}
                    					*/
                    					if (enemy.getDistanceTo(ship) < 8 && Helper.filterOutDockedShips(gameMap, Helper.getMyShipsNear(gameMap, savior, 10)).size() > Helper.getEnemiesNear(gameMap, savior, 10).size()) {
                    						//Navigation.enemyFlee(gameMap, enemy, Helper.getClosestAllyNotDocked(gameMap, enemy)); // Prediction
                    						ThrustMove newThrustMove = Navigation.navigateShipToShip(gameMap, savior, enemy, Constants.MAX_SPEED, 12); // Defending Attacking
                    						if (newThrustMove != null) {
                                                moveList.add(newThrustMove);
                                                savior.setIgnore(true);
                                                savior.setCompleted(true);
                                                enemy.setIgnore(true);
                                            }
                    					} else {
                    						ThrustMove newThrustMove = Navigation.navigateShipToDefend(gameMap, savior, enemy, Helper.getClosestAllyDocked(gameMap, enemy), Constants.MAX_SPEED, 10); // Defending
                    						if (newThrustMove != null) {
                                                moveList.add(newThrustMove);
                                                savior.setIgnore(true);
                                                savior.setCompleted(true);
                                                enemy.setIgnore(true);
                                            }
                    					}
                    				}
                    			}
                    		}
                		}
                	}
                }
                
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                	
                	/** Undocking: Not used. **/
                	if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                		continue;
                	}
                	
                	if (ship.getCompleted()) {
						continue;
					}
                	if (panic && gameMap.getAllPlayers().size() == 4) {
                		if (ship.getDistanceTo(Helper.getClosestEnemyNotDocked(gameMap, ship)) < 20) {
                			boolean move = Combat.panic(gameMap, ship, moveList);
                    		if (move) continue;
                		} else {
                			boolean move = Combat.hide(gameMap, ship, moveList);
                    		if (move) continue;
                		}
                	}
                	// ArrayList<Ship> nearbyAlliesAroundMe = Helper.filterOutIgnoreShips(gameMap, Helper.getMyShipsNear(gameMap, ship, Constants.GROUP));
                	ArrayList<Ship> nearbyAlliesAroundMe = Helper.getMyShipsNear(gameMap, ship, Constants.GROUP);
					nearbyAlliesAroundMe.add(ship);
					ArrayList<Ship> reallyCloseAllies = Helper.getMyShipsNear(gameMap, ship, Constants.SQUAD);
					// ArrayList<Ship> reallyCloseAllies = Helper.filterOutIgnoreShips(gameMap, Helper.getMyShipsNear(gameMap, ship, Constants.SQUAD));
					reallyCloseAllies.add(ship);
					ArrayList<Ship> squad = Helper.filterOutDockedShips(gameMap, nearbyAlliesAroundMe);
					
                	Map<Double, Entity> nearbyEntities = gameMap.nearbyEntitiesByDistance(ship);
                	for (double distance : nearbyEntities.keySet()) {
                		Entity targetEntity = nearbyEntities.get(distance);
                		ThrustMove newThrustMove;
                		
                		if (targetEntity instanceof Planet) {
                			Planet planet = (Planet) targetEntity;
                			
                			Ship closestEnemy = Helper.getClosestEnemyNotDocked(gameMap, ship);
                			Ship closestAlly = Helper.getClosestAllyNotDocked(gameMap, ship);
                			
                			if (closestEnemy != null && closestAlly != null) {
                				if (ship.getDistanceTo(closestEnemy) < ship.getDistanceTo(closestAlly) + 5 && ship.getDistanceTo(closestEnemy) < 30) {
                    				continue;
                    			}
                			}
                			
                			/** Don't dock if defenseless **/
                			ArrayList<Ship> enemyShips = Helper.filterOutDockedShips(gameMap, Helper.getEnemiesNear(gameMap, ship, 30));
                			ArrayList<Ship> allyShips = Helper.filterOutIgnoreShips(gameMap, Helper.filterOutDockedShips(gameMap, Helper.getMyShipsNear(gameMap, ship, 15)));
                			if (enemyShips.size() > 0) {
                				if (enemyShips.size() > allyShips.size() - 1  && distance < planet.getRadius() + 20) {
                    				continue;
                    			}
                			}
                			
                			if (Helper.iAmOwner(gameMap, planet)) { 
								if (planet.getDockingSpots() > planet.getDockedShips().size() + planet.getCurrentGoing()) {
                            		if (ship.canDock(planet)) {
                            			planet.addToCurrentGoing();
                                        moveList.add(new DockMove(ship, planet));
                                        ship.setCompleted(true);
                                        ship.setIgnore(true);
                                        break;
                                    }
                                	newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED, 11); // Docking
                                	if (newThrustMove != null) {
                                		planet.addToCurrentGoing();
                                		ship.setCompleted(true);
                                        moveList.add(newThrustMove);
                                        break;
                                    }
                            	}
                			} else if (!planet.isOwned() && planet.getDockingSpots() > planet.getCurrentGoing()) {
                				/*
                				if (turn < 10) {
                					ArrayList<Planet> possiblePlanets = Helper.getPlanetsNear(gameMap, ship, distance + 20);
                    				for (Planet possPlanet : possiblePlanets) {
                    					if (possPlanet != planet && possPlanet.getDockingSpots() > planet.getDockingSpots()) {
                    						if (ship.canDock(possPlanet)) {
                    							possPlanet.addToCurrentGoing();
                                                moveList.add(new DockMove(ship, possPlanet));
                                                ship.setCompleted(true);
                                                ship.setIgnore(true);
                                                break;
                                            }
                        					newThrustMove = Navigation.navigateShipToDock(gameMap, ship, possPlanet, Constants.MAX_SPEED, 11); // Docking
                        					if (newThrustMove != null) {
                        						possPlanet.addToCurrentGoing();
                        						ship.setCompleted(true);
                                                moveList.add(newThrustMove);
                                                break;
                                            }
                    					}
                    				}
                    				if (ship.getCompleted())
                    				{
                    					break;
                    				}
                				}
                				*/
                				if (turn < 10 && planet.getDockingSpots() < gameMap.getAllPlanets().get(0).getDockingSpots() && gameMap.getAllPlayers().size() == 2) {
            						continue;
            					}
                				if (turn < 10 && planet.getDistanceTo(Helper.getClosestPlanet(gameMap, planet)) > 100 && distance > 35 && gameMap.getAllPlayers().size() == 2) {
                					continue;
                				}
                				if (turn < 10 && ship.getDistanceTo(Helper.getClosestEnemy(gameMap, ship)) < 60) {
                					continue;
                				}
                				// ^ Code that determines what inital planets to go for
                				if (ship.canDock(planet)) {
                        			planet.addToCurrentGoing();
                                    moveList.add(new DockMove(ship, planet));
                                    ship.setCompleted(true);
                                    ship.setIgnore(true);
                                    break;
                                }
                            	newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED, 11); // Docking
                            	if (newThrustMove != null) {
                            		planet.addToCurrentGoing();
                            		ship.setCompleted(true);
                                    moveList.add(newThrustMove);
                                    break;
                                }
            				}
                		} else {
                			if (targetEntity instanceof Ship) {
                				Ship target = (Ship) targetEntity;
                				
                				if ((target).getIgnore() && distance > 5) {
            						continue;
            					}
                				if (Helper.iAmOwner(gameMap, target)) { // If I own the ship
                					continue;
                				} else {
                					ArrayList<Ship> enemyShips = Helper.getEnemiesNear(gameMap, target, Constants.GROUP);
        							enemyShips.add(target);
        							ArrayList<Ship> reallyCloseEnemies = Helper.filterOutDockedShips(gameMap, Helper.getEnemiesNear(gameMap, target, Constants.SQUAD));
        							reallyCloseEnemies.add(target);
        							ArrayList<Ship> notDockedEnemyShips = Helper.filterOutDockedShips(gameMap, enemyShips);
        							
        							Ship closDockEnemyTar = Helper.getClosestDockedEnemy(gameMap, target);
        							Ship closDockAllyTar = Helper.getClosestAllyDocked(gameMap, target);
        							
        							if (closDockEnemyTar != null && closDockAllyTar != null && nearbyAlliesAroundMe.size() > enemyShips.size()) {
        								/** Detects if ship is a "distraction" **/
        								if (target.getDistanceTo(closDockEnemyTar) > 20  && target.getDistanceTo(closDockAllyTar) > 20  && target.getDistanceTo(Helper.getClosestPlanet(gameMap, target)) > 20) {
                    						continue;
                    					}
                					}
        							
        							if (nearbyAlliesAroundMe.size() == 1) {			
                            			if (notDockedEnemyShips.size() > 1) {
                            				if (distance > 20) {
                            					newThrustMove = Navigation.navigateShipToShip(gameMap, ship, target, Constants.MAX_SPEED, 55); // Leading on
                            					if (newThrustMove != null) {
                                                    moveList.add(newThrustMove);
                                                    ship.setCompleted(true);
                                                    break;
                                                }
                            				} else {
                            					if (Helper.getClosestAllyDocked(gameMap, ship) != null) {
                            						if (Helper.getClosestAllyDocked(gameMap, ship).getDistanceTo(ship) < 20) {
                                						boolean move = Combat.flee(gameMap, ship, moveList); 
                				                		if (move) {
                				                			break;
                				                		}
                                					} else {
                                						boolean move = Combat.evil(gameMap, ship, moveList);
                				                		if (move) {
                				                			break;
                				                		}
                                					}
                            					} else {
                            						boolean move = Combat.fleeAndPanic(gameMap, ship, moveList);
            				                		if (move) {
            				                			break;
            				                		}
                            					}
                            				}
                            			} else if (notDockedEnemyShips.size() == 1) {
                            				if (distance > 20) {
                            					newThrustMove = Navigation.navigateShipToShip(gameMap, ship, target, Constants.MAX_SPEED, 55); // Leading on
                            					if (newThrustMove != null) {
                                                    moveList.add(newThrustMove);
                                                    (target).setIgnore(true);
                                                    ship.setCompleted(true);
                                                    break;
                                                }
                            				} else {
                            					boolean move = Combat.prick(gameMap, ship, moveList); //Possibly this is the issue
        				                		if (move) {
        				                			break;
        				                		}
        				                		move = Combat.fleeAndPanic(gameMap, ship, moveList); //Possibly this is the issue
        				                		if (move) {
        				                			break;
        				                		}
                            				}
                            			}
                        			}
                        			
                        			if (nearbyAlliesAroundMe.size() >= notDockedEnemyShips.size()) { 
                        				if (reallyCloseAllies.size() < Helper.filterOutDockedShips(gameMap, reallyCloseEnemies).size() && distance < 20) {
                        					ArrayList<Ship> group = Helper.filterOutDockedShips(gameMap, Helper.getMyShipsNear(gameMap, ship, 20));
                        					if (group.size() > 1) {
                        						Combat.group(gameMap, ship, moveList);
                        						if (ship.getCompleted()) {
        				                			break;
        				                		}
                        					} else {
                        						if (Helper.filterOutDockedShips(gameMap, notDockedEnemyShips).size() == 0) {
                        							newThrustMove = Navigation.navigateShipToShip(gameMap, ship, target, Constants.MAX_SPEED, 58); // Attacking no defender
                                					if (newThrustMove != null) {
                                						moveList.add(newThrustMove);
                                						ship.setCompleted(true);
                                                        break;
                                                    }
                        						}
                        						boolean move = Combat.fleeAndPanic(gameMap, ship, moveList);
        				                		if (move) {
        				                			break;
        				                		}
                        					}
                        				}
                        				
                        				if (Helper.getClosestDockedEnemy(gameMap, ship) != null && squad.size() > Helper.getEnemiesNear(gameMap, ship, 20).size()) {
                        					for (Ship enemyShip : Helper.getEnemiesNear(gameMap, ship, 60))
                        					{
                        						if (enemyShip.getDockingStatus() != Ship.DockingStatus.Undocked && squad.size() > 1.2 * Helper.getEnemiesNear(gameMap, enemyShip, 20).size())
                        						{
                        							boolean move = Combat.groupFight(gameMap, reallyCloseAllies, enemyShip, moveList);
                            						if (ship.getCompleted()) {
            				                			break;
            				                		}
                            						move = Combat.singularGroup(gameMap, ship, moveList); //Possibly this is the issue
        				                			if (move) {
            				                			break;
            				                		}
                        						}
                        					}
                        					if (ship.getCompleted()) {
    				                			break;
    				                		}
                        				}
                        				if (squad.size() > notDockedEnemyShips.size() * 2) {
                        					if (Helper.getClosestAllyNotDocked(gameMap, ship) != null) {
                        						newThrustMove = Navigation.navigateShipToShipDirectly(gameMap, ship, target, Constants.MAX_SPEED, 52); // Possibly could be distraction
                            					if (newThrustMove != null) {
                                                    moveList.add(newThrustMove);
                                                    ship.setShip(target);
                                                    ship.setCompleted(true);
                                                    (target).incrementCurrent();
                                                    if ((target).getCurrent() >= 2) {
                                                    	(target).setIgnore(true);
                                                    }
                                                    break;
                                                }
                        					}
                        				}
                        				boolean move = Combat.groupFight2(gameMap, Helper.filterOutDockedShips(gameMap, reallyCloseAllies), target, moveList);
				                		if (ship.getCompleted()) {
				                			break;
				                		} else {
				                			move = Combat.singularGroup(gameMap, ship, moveList);
				                			if (move) {
    				                			break;
    				                		}
				                		}
        							} else {
        								if (nearbyAlliesAroundMe.size() >= notDockedEnemyShips.size() && squad.size() <= notDockedEnemyShips.size()) {
        									if (Helper.getClosestAllyDocked(gameMap, ship) != null) {
        										newThrustMove = Navigation.navigateShipToShipDirectly(gameMap, ship, Helper.getClosestAllyDocked(gameMap, ship), Constants.MAX_SPEED, 56); // Meat shield support
                            					if (newThrustMove != null) {
                                                    moveList.add(newThrustMove);
                                                    ship.setCompleted(true);
                                                    break;
                                                }
        									}
        								}
        								/*
										if (distance < 20) {
        									
        								}
        								 */
        								if (distance < 20) {
        									if (gameMap.getAllPlayers().size() == 4) {
        										boolean move = Combat.groupFlee(gameMap, reallyCloseAllies, (Ship) target, moveList);
            									if (ship.getCompleted()) {
        				                			break;
        				                		} else {
        				                			move = Combat.fleeAndPanic(gameMap, ship, moveList);
            				                		if (move) {
            				                			break;
            				                		}
        				                		}
        									} else {
        										boolean move = Combat.prick(gameMap, ship, moveList);
        				                		if (move) {
        				                			break;
        				                		} else {
        				                			move = Combat.groupFlee(gameMap, reallyCloseAllies, target, moveList);
                									if (ship.getCompleted()) {
            				                			break;
            				                		} else {
            				                			move = Combat.fleeAndPanic(gameMap, ship, moveList);
                				                		if (move) {
                				                			break;
                				                		}
            				                		}
        				                		}
        									}
        								}
        								/*else if (distance < 30) {
        									Combat.group(gameMap, ship, moveList);
        									if (ship.getCompleted()) {
    				                			break;
    				                		}
        								}*/ else {
        									newThrustMove = Navigation.navigateShipToShip(gameMap, ship, target, Constants.MAX_SPEED, 55); // Leading on
                        					if (newThrustMove != null) {
                        						moveList.add(newThrustMove);
                        						ship.setCompleted(true);
                                                break;
                                            }
        								}
            						}
                				}
                			}
                		}
                	}
                }
            }
            Networking.sendMoves(moveList);
        }
	}
}