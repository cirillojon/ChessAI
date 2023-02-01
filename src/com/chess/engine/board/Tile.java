package com.chess.engine.board;
import com.chess.engine.pieces.Piece;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//The Tile class abstracts the concept of a tile on a chess board
public abstract class Tile
{
    //tileCoordinate stores the coordinate of the tile on the chess board
    protected final int tileCoordinate;
    //A cache of all possible empty tiles to improve performance
    private static final Map<Integer, EmptyTile> EMPTY_TILES_CACHE = createAllPossibleEmptyTiles();

    //Create a map of all possible empty tiles, one for each coordinate
    private static Map<Integer, EmptyTile> createAllPossibleEmptyTiles()
    {
        final Map<Integer, EmptyTile> emptyTileMap = new HashMap<>();
        for (int i = 0; i < BoardUtils.NUM_TILES; i++)
        {
            emptyTileMap.put(i, new EmptyTile(i));
        }
        return Collections.unmodifiableMap(emptyTileMap);
    }

    //Return either an EmptyTile or an OccupiedTile, depending on whether there's a piece on the tile
    private static Tile createTile(final int tileCoordinate, final Piece piece)
    {
        if(piece != null)
            return new EmptyTile.OccupiedTile(tileCoordinate, piece);
        else
            return EMPTY_TILES_CACHE.get(tileCoordinate);
    }

    private Tile(final int tileCoordinate) { this.tileCoordinate = tileCoordinate; }

    //Abstract methods for checking if the tile is occupied and getting the piece on it
    public abstract boolean isTileOccupied();
    public abstract Piece getPiece();

    //The EmptyTile class represents an empty tile on the chess board
    public static final class EmptyTile extends Tile
    {
        private EmptyTile(final int coordinate) {
            super(coordinate);
        }

        //An empty tile is not occupied
        @Override
        public boolean isTileOccupied() {
            return false;
        }

        //No piece on an empty tile
        @Override
        public Piece getPiece() {
            return null;
        }

        //The OccupiedTile class represents a tile occupied by a piece
        public static final class OccupiedTile extends Tile
        {
            //pieceOnTile stores the piece on the tile
            private final Piece pieceOnTile;

            private OccupiedTile(int coordinate, Piece pieceOnTile)
            {
                super(coordinate);
                this.pieceOnTile = pieceOnTile;
            }

            //An occupied tile is occupied
            @Override
            public boolean isTileOccupied() {
                return true;
            }

            //Return the piece on the tile
            @Override
            public Piece getPiece() {
                return this.pieceOnTile;
            }
        }
    }
}
