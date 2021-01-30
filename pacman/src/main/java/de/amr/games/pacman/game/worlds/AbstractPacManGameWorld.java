package de.amr.games.pacman.game.worlds;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Logging.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.game.core.PacManGameWorld;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Common base of Pac-Man classic and Ms. Pac-man worlds. All maps used in these game variants have
 * the same ghost house structure and location.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractPacManGameWorld implements PacManGameWorld {

	private static byte decode(char c) {
		switch (c) {
		case ' ':
			return SPACE;
		case '#':
			return WALL;
		case 'T':
			return TUNNEL;
		case '-':
			return DOOR;
		case '.':
			return PILL;
		case '*':
			return ENERGIZER;
		default:
			throw new RuntimeException();
		}
	}

	private static final V2i HOUSE_ENTRY = new V2i(13, 14);
	private static final V2i HOUSE_LEFT = new V2i(11, 17);
	private static final V2i HOUSE_CENTER = new V2i(13, 17);
	private static final V2i HOUSE_RIGHT = new V2i(15, 17);

	private static final V2i PAC_HOME = new V2i(13, 26);

	private static final V2i[] GHOST_HOME_TILES = { HOUSE_ENTRY, HOUSE_CENTER, HOUSE_LEFT, HOUSE_RIGHT };
	private static final V2i[] GHOST_SCATTER_TILES = { new V2i(25, 0), new V2i(2, 0), new V2i(27, 35), new V2i(27, 35) };

	protected final V2i sizeTiles = new V2i(28, 36);

	protected final List<V2i> portalsLeft = new ArrayList<>(2);
	protected final List<V2i> portalsRight = new ArrayList<>(2);

	protected List<V2i> energizerTiles;
	protected BitSet intersections;

	protected byte[][] map;

	protected void loadMap(String path) {
		int lineNumber = 0, errors = 0;
		map = new byte[sizeTiles.y][sizeTiles.x];
		try (InputStream is = getClass().getResourceAsStream(path)) {
			if (is == null) {
				throw new RuntimeException("Resource not found: " + path);
			}
			BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
			int y = 0;
			for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
				++lineNumber;
				if (line.startsWith("!") || line.isBlank()) {
					continue; // skip comments and blank lines
				}
				for (int x = 0; x < sizeTiles.x; ++x) {
					char c = line.charAt(x);
					try {
						map[y][x] = decode(c);
					} catch (Exception cause) {
						++errors;
						map[y][x] = SPACE;
						log("*** Error in map '%s': Illegal char '%c' at line %d, column %d", path, c, lineNumber, x + 1);
					}
				}
				++y;
			}
		} catch (IOException e) {
			throw new RuntimeException(String.format("Error reading map '%s'", path, e));
		}

		// find portals
		portalsLeft.clear();
		portalsRight.clear();
		for (int y = 0; y < sizeTiles.y; ++y) {
			if (map[y][0] == TUNNEL && map[y][sizeTiles.x - 1] == TUNNEL) {
				portalsLeft.add(new V2i(-1, y));
				portalsRight.add(new V2i(sizeTiles.x, y));
			}
		}

		// find intersections ("waypoints"), i.e. tiles with at least 3 accessible neighbor tiles
		intersections = new BitSet();
		//@formatter:off
		tiles()
			.filter(tile -> !isInsideGhostHouse(tile))
			.filter(tile -> !isGhostHouseDoor(tile.sum(DOWN.vec)))
			.filter(tile -> neighborTiles(tile).filter(this::isAccessible).count() >= 3)
			.map(tile -> index(tile))
			.forEach(intersections::set);
		//@formatter:on

		// find energizer tiles
		energizerTiles = tiles().filter(tile -> data(tile) == ENERGIZER).collect(Collectors.toList());

		log("Use map '%s' (%d errors), (%d energizers)", path, errors, energizerTiles.size());
	}

	private Stream<V2i> neighborTiles(V2i tile) {
		return Stream.of(Direction.values()).map(dir -> tile.sum(dir.vec));
	}

	@Override
	public int xTiles() {
		return sizeTiles.x;
	}

	@Override
	public int yTiles() {
		return sizeTiles.y;
	}

	@Override
	public byte data(V2i tile) {
		return map[tile.y][tile.x];
	}

	@Override
	public boolean inMapRange(V2i tile) {
		return 0 <= tile.x && tile.x < sizeTiles.x && 0 <= tile.y && tile.y < sizeTiles.y;
	}

	@Override
	public V2i pacHome() {
		return PAC_HOME;
	}

	@Override
	public V2i ghostHome(int ghost) {
		return GHOST_HOME_TILES[ghost];
	}

	@Override
	public V2i ghostScatterTile(int ghost) {
		return GHOST_SCATTER_TILES[ghost];
	}

	@Override
	public int numPortals() {
		return portalsLeft.size();
	}

	@Override
	public V2i portalLeft(int i) {
		return portalsLeft.get(i);
	}

	@Override
	public V2i portalRight(int i) {
		return portalsRight.get(i);
	}

	@Override
	public V2i houseEntry() {
		return HOUSE_ENTRY;
	}

	@Override
	public V2i houseCenter() {
		return HOUSE_CENTER;
	}

	@Override
	public V2i houseLeft() {
		return HOUSE_LEFT;
	}

	@Override
	public V2i houseRight() {
		return HOUSE_RIGHT;
	}

	@Override
	public boolean isAccessible(V2i tile) {
		if (isPortal(tile)) {
			return true;
		}
		if (!inMapRange(tile)) {
			return false;
		}
		if (isGhostHouseDoor(tile)) {
			return false;
		}
		return data(tile) != WALL;
	}

	@Override
	public boolean isTunnel(V2i tile) {
		return inMapRange(tile) && data(tile) == TUNNEL;
	}

	@Override
	public boolean isGhostHouseDoor(V2i tile) {
		return inMapRange(tile) && data(tile) == DOOR;
	}

	@Override
	public boolean isPortal(V2i tile) {
		return portalsLeft.contains(tile) || portalsRight.contains(tile);
	}

	private boolean isInsideGhostHouse(V2i tile) {
		return tile.x >= 10 && tile.x <= 17 && tile.y >= 15 && tile.y <= 22;
	}

	@Override
	public boolean isIntersection(V2i tile) {
		return intersections.get(index(tile));
	}

	@Override
	public boolean isFoodTile(V2i tile) {
		return inMapRange(tile) && (data(tile) == PILL || data(tile) == ENERGIZER);
	}

	@Override
	public boolean isEnergizerTile(V2i tile) {
		return energizerTiles.contains(tile);
	}

	@Override
	public Stream<V2i> energizerTiles() {
		return energizerTiles.stream();
	}
}