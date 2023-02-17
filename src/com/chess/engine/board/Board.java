package com.chess.engine.board;
import com.chess.engine.Alliance;
import com.chess.engine.pieces.*;

import java.util.*;

public class Board {

    // The game board consists of a list of 64 tiles.
    private final List<Tile> gameBoard;
    // Collections of pieces of each color on the board.
    private final Collection<Piece> whitePieces;
    private final Collection<Piece> blackPieces;

    // Private constructor that creates a new board using the builder pattern.
    private Board(Builder builder){
        // The game board is created from the board configuration.
        this.gameBoard = createGameBoard(builder);
        // Calculates the white and black pieces on the board.
        this.whitePieces = calculateActivePieces(this.gameBoard, Alliance.WHITE);
        this.blackPieces = calculateActivePieces(this.gameBoard, Alliance.BLACK);
        
        final Collection<Move> whiteStandardLegalMoves = calculateLegalMoves(this.whitePieces);
        final Collection<Move> blackStandardLegalMoves = calculateLegalMoves(this.blackPieces);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < BoardUtils.NUM_TILES; i++){
            final String tileText = this.gameBoard.get(i).toString();
            builder.append(String.format("%3s", tileText));
            if((i + 1) % BoardUtils.NUM_TILES_PER_ROW == 0){
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    // Calculates all legal moves for a given collection of pieces.
    private Collection<Move> calculateLegalMoves(final Collection<Piece> pieces) {
        final List<Move> legalMoves = new ArrayList<>();
        // Loop through each piece in the collection.
        for(final Piece piece : pieces){
            // Calculate the legal moves for the current piece and add them to the list of legal moves.
            legalMoves.addAll(piece.calculateLegalMoves(this));
        }
        // Return an unmodifiable list of the legal moves.
        return Collections.unmodifiableList(legalMoves);
    }

    // Calculates all active pieces for a given alliance.
    private static Collection<Piece> calculateActivePieces(final List<Tile> gameBoard, final Alliance alliance) {
        // List of active pieces to be returned.
        final List<Piece> activePieces = new ArrayList<>();
        for(final Tile tile : gameBoard){
            // If a tile is occupied, add its piece to the list of active pieces if it belongs to the alliance.
            if(tile.isTileOccupied()){
                final Piece piece = tile.getPiece();
                if(piece.getPieceAlliance() == alliance){
                    activePieces.add(piece);
                }
            }
        }
        // Return an unmodifiable list of the active pieces.
        return Collections.unmodifiableList(activePieces);
    }

    // Creates a new game board from the board configuration.
    private static List<Tile> createGameBoard(final Builder builder) {
        final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];
        for(int i = 0; i < BoardUtils.NUM_TILES; i++){
            // Create a new tile with the appropriate position and piece.
            tiles[i] = Tile.createTile(i, builder.boardConfig.get(i));
        }
        // Return an unmodifiable list of the tiles.
        return Collections.unmodifiableList(Arrays.asList(tiles));
    }

    // Returns the tile at a given coordinate.
    public Tile getTile(final int tileCoordinate) {
        return gameBoard.get(tileCoordinate);
    }

    // Creates a standard chess board with pieces in their initial positions.
    public static Board createStandardBoard(){
        final Builder builder = new Builder();
        //Black Layout
        builder.setPiece(new Rook(0, Alliance.BLACK));
        builder.setPiece(new Knight(1, Alliance.BLACK));
        builder.setPiece(new Bishop(2, Alliance.BLACK));
        builder.setPiece(new Queen(3, Alliance.BLACK));
        builder.setPiece(new King(4, Alliance.BLACK));
        builder.setPiece(new Bishop(5, Alliance.BLACK));
        builder.setPiece(new Knight(6, Alliance.BLACK));
        builder.setPiece(new Rook(7, Alliance.BLACK));
        builder.setPiece(new Pawn(8, Alliance.BLACK));
        builder.setPiece(new Pawn(9, Alliance.BLACK));
        builder.setPiece(new Pawn(10, Alliance.BLACK));
        builder.setPiece(new Pawn(11, Alliance.BLACK));
        builder.setPiece(new Pawn(12, Alliance.BLACK));
        builder.setPiece(new Pawn(13, Alliance.BLACK));
        builder.setPiece(new Pawn(14, Alliance.BLACK));
        builder.setPiece(new Pawn(15, Alliance.BLACK));
        //White Layout
        builder.setPiece(new Pawn(48, Alliance.WHITE));
        builder.setPiece(new Pawn(49, Alliance.WHITE));
        builder.setPiece(new Pawn(50, Alliance.WHITE));
        builder.setPiece(new Pawn(51, Alliance.WHITE));
        builder.setPiece(new Pawn(52, Alliance.WHITE));
        builder.setPiece(new Pawn(53, Alliance.WHITE));
        builder.setPiece(new Pawn(54, Alliance.WHITE)) ;
        builder.setPiece(new Pawn(55, Alliance.WHITE));
        builder.setPiece(new Rook(56, Alliance.WHITE));
        builder.setPiece(new Knight(57, Alliance.WHITE));
        builder.setPiece(new Bishop(58, Alliance.WHITE));
        builder.setPiece(new Queen(59, Alliance.WHITE));
        builder.setPiece(new King(60, Alliance.WHITE));
        builder.setPiece(new Bishop(61, Alliance.WHITE));
        builder.setPiece(new Knight(62, Alliance.WHITE));
        builder.setPiece(new Rook(63, Alliance.WHITE));
        //White to move
        builder.setMoveMaker(Alliance.WHITE);
        return builder.build();
    }

    public static class Builder{
        Map<Integer, Piece> boardConfig;

        Alliance nextMoveMaker;

        public Builder(){
            this.boardConfig = new HashMap<>();
        }
        public Builder setPiece(final Piece piece){
            this.boardConfig.put(piece.getPiecePosition(), piece);
            return this;
        }

        public Builder setMoveMaker(final Alliance nextMoveMaker){
            this.nextMoveMaker = nextMoveMaker;
            return this;
        }
        public Board build()
        {
            return new Board(this);
        }
    }

}
