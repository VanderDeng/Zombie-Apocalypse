package com.vcg.zombie.service;

import com.vcg.zombie.entity.Creature;
import com.vcg.zombie.entity.Position;
import com.vcg.zombie.entity.World;
import com.vcg.zombie.entity.WorldOutput;
import com.vcg.zombie.entity.Zombie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class WorldService {

    private static final Logger logger = LogManager.getLogger(WorldService.class);

    private final Map<Character, Position> positionOffsetMap =
        Map.of(Commands.RIGHT.getAbbreviation(), new Position(1, 0),
            Commands.LEFT.getAbbreviation(), new Position(-1, 0),
            Commands.UP.getAbbreviation(), new Position(0, -1),
            Commands.DOWN.getAbbreviation(), new Position(0, 1));

    public WorldOutput execute(World world, List<Zombie> zombies) {
        zombies.add(world.getZombie());
        printStatus(world, zombies, "Start:");
        start(world, zombies);
        printStatus(world, zombies, "End:");
        return new WorldOutput(zombies, world.getCreatures());
    }

    private void start(World world, List<Zombie> zombies) {
        for (int i = 0; i < world.getCommands().length(); i++) {
            zombieMove(world, world.getCommands().charAt(i));
            zombieDetectCreature(world, zombies);
        }
    }

    private void printStatus(World world, List<Zombie> zombies, String s) {
        System.out.println();
        logger.info("---------" + s + "----------");
        logger.info("gridSize = " + world.gridSize);
        zombies.forEach(z -> logger.info("zombie position: " + z.getX() + " " + z.getY()));
        world.getCreatures().forEach(h -> logger.info("human position: " + h.getX() + " " + h.getY()));
        logger.info("commands = " + world.getCommands());
    }

    private void zombieMove(World world, char moveDirection) {
        if (positionOffsetMap.get(moveDirection) == null) {
            logger.info("zombie 0 halted");
        } else {
            zombieMoveByPosition(world, positionOffsetMap.get(moveDirection));
        }
    }

    private void zombieMoveByPosition(World world, Position position) {
        world.getZombie().setX(Math.floorMod(world.getZombie().getX() + position.getX(), world.getGridSize()));
        world.getZombie().setY(Math.floorMod(world.getZombie().getY() + position.getY(), world.getGridSize()));
        zombieLog(world.getZombie().getX(), world.getZombie().getY(), "move");
    }

    private void zombieLog(int x, int y, String type) {
        String phrase = switch (type) {
            case "move" -> "moved to";
            case "infect" -> "infected creature at";
            default -> "halted";
        };
        logger.info("zombie 0 {} ({},{})", phrase, x, y);
    }

    void zombieDetectCreature(World world, List<Zombie> zombies) {
        int x = world.getZombie().getX();
        int y = world.getZombie().getY();
        for (Creature creature : world.getCreatures()) {
            if (creature.getX() == x && creature.getY() == y) {
                zombieInfect(world, zombies, x, y, creature);
                break;
            }
        }
    }

    private void zombieInfect(World world, List<Zombie> zombies, int x, int y, Creature creature) {
        zombies.add(new Zombie(x, y));
        world.getCreatures().remove(creature);
        zombieLog(x, y, "infect");
    }
}

enum Commands {
    LEFT('L'), RIGHT('R'), UP('U'), DOWN('D');

    Commands(Character abbreviation) {

        this.abbreviation = abbreviation;
    }

    public Character getAbbreviation() {
        return abbreviation;
    }

    final Character abbreviation;
}