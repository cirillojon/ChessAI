package com.chess.engine.board;
import com.chess.engine.pieces.Piece;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Tile
{
    //Variables
    protected final int tileCoordinate;
    private static final Map<Integer, EmptyTile> EMPTY_TILES_CACHE = createAllPossibleEmptyTiles();

    //Methods
    private static Map<Integer, EmptyTile> createAllPossibleEmptyTiles()
    {
        final Map<Integer, EmptyTile> emptyTileMap = new HashMap<>();
        for (int i = 0; i < 64; i++)
        {
            emptyTileMap.put(i, new EmptyTile(i));
        }
        return Collections.unmodifiableMap(emptyTileMap);
    }
    private static Tile createTile(final int tileCoordinate, final Piece piece)
    {
        if(piece != null)
            return new EmptyTile.OccupiedTile(tileCoordinate, piece);
        else
            return EMPTY_TILES_CACHE.get(tileCoordinate);
    }
    private Tile(int tileCoordinate) { this.tileCoordinate = tileCoordinate; }

    public abstract boolean isTileOccupied();

    public abstract Piece getPiece();

    public static final class EmptyTile extends Tile
    {

        private EmptyTile(final int coordinate) {
            super(coordinate);
        }

        @Override
        public boolean isTileOccupied() {
            return false;
        }

        @Override
        public Piece getPiece() {
            return null;
        }

        public static final class OccupiedTile extends Tile
        {

           private final Piece pieceOnTile;

            private OccupiedTile(int coordinate, Piece pieceOnTile)
            {
                super(coordinate);
                this.pieceOnTile = pieceOnTile;
            }

            @Override
            public boolean isTileOccupied() {
                return true;
            }

            @Override
            public Piece getPiece() {
                return this.pieceOnTile;
            }
        }
    }
}
