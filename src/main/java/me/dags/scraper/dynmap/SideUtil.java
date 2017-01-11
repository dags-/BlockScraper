package me.dags.scraper.dynmap;

import org.dynmap.modsupport.BlockSide;
import org.dynmap.modsupport.TextureFile;

import java.util.Arrays;
import java.util.BitSet;

/**
 * @author dags <dags@dags.me>
 */
public class SideUtil {

    private static final int DOWN = 0, UP = 1, NORTH = 2, SOUTH = 3, EAST = 4, WEST = 5;
    private static final BlockSide[] SIDES = {
            BlockSide.BOTTOM,
            BlockSide.TOP,
            BlockSide.NORTH,
            BlockSide.SOUTH,
            BlockSide.EAST,
            BlockSide.WEST,
    };

    private final BitSet faces = new BitSet(6);

    private int fallbackOrdinal = -1;
    private TextureFile fallbackTexture = null;

    public boolean hasFallback() {
        return fallbackTexture != null;
    }

    public TextureFile getFallbackTexture() {
        return fallbackTexture;
    }

    public BlockSide[] getMissingSides() {
        BlockSide[] sides = new BlockSide[6];
        int j = 0;
        for (int i = 0; i < SIDES.length; i++) {
            if (!faces.get(i)) {
                sides[j++] = SIDES[i];
            }
        }
        return Arrays.copyOf(sides, j);
    }

    public SideUtil recordSide(BlockSide side, TextureFile textureFile) {
        if (side.ordinal() > fallbackOrdinal) {
            this.fallbackTexture = textureFile;
            this.fallbackOrdinal = side.ordinal();
        }

        switch (side) {
            case ALLFACES:
                return all();
            case ALLSIDES:
                return sides();
            case BOTTOM:
                return bottom();
            case TOP:
                return top();
            case NORTH:
                return north();
            case SOUTH:
                return south();
            case EAST:
                return east();
            case WEST:
                return west();
        }
        return this;
    }

    private SideUtil all() {
        for (int i = DOWN; i < 5; i++) {
            faces.set(i, true);
        }
        return this;
    }

    private SideUtil sides() {
        for (int i = NORTH; i < 5; i++) {
            faces.set(i, true);
        }
        return this;
    }

    private SideUtil bottom() {
        faces.set(DOWN, true);
        return this;
    }

    private SideUtil top() {
        faces.set(UP, true);
        return this;
    }

    private SideUtil north() {
        faces.set(NORTH, true);
        return this;
    }

    private SideUtil south() {
        faces.set(SOUTH, true);
        return this;
    }

    private SideUtil east() {
        faces.set(EAST, true);
        return this;
    }

    private SideUtil west() {
        faces.set(WEST, true);
        return this;
    }

    static BlockSide fromName(String in) {
        switch (in) {
            case "down":
            case "bottom":
                return BlockSide.BOTTOM;
            case "up":
            case "top":
                return BlockSide.TOP;
            case "north":
                return BlockSide.NORTH;
            case "south":
                return BlockSide.SOUTH;
            case "east":
                return BlockSide.EAST;
            case "west":
                return BlockSide.WEST;
            case "recordSide":
            case "sides":
                return BlockSide.ALLSIDES;
        }
        return BlockSide.ALLSIDES;
    }
}
