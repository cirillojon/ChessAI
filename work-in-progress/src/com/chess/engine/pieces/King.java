package com.chess.engine.pieces;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// King class that extends the Piece class
public class King extends Piece{

    // Array of ints to store candidate moves for the King piece
    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATES = {-9, -8, -7, -1, 1, 7, 8, 9};

    // Constructor to initialize King object
    public King(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.KING, piecePosition, pieceAlliance);
    }

    // Overridden method to calculate legal moves for the King piece
    @Override
    public Collection<Move> calculateLegalMoves(Board board) {

        // list to store the legal moves
        final List<Move> legalMoves = new ArrayList<>();

        // loop through the CANDIDATE_MOVE_VECTOR_COORDINATES array
        for(final int currentCandidateOffset : CANDIDATE_MOVE_VECTOR_COORDINATES) {

            // calculate the candidate destination coordinate
            final int candidateDestinationCoordinate;
            candidateDestinationCoordinate = this.piecePosition + currentCandidateOffset;

            // check for the first column exclusion
            if(isFirstColumnExclusion(this.piecePosition, currentCandidateOffset) ||
                    // check for the eighth column exclusion
                    isEighthColumnExclusion(this.piecePosition, currentCandidateOffset)){
                continue;
            }

            // check if the candidate destination coordinate is valid
            if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                // get the tile at the candidate destination coordinate
                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                // if the tile is not occupied
                if(!candidateDestinationTile.isTileOccupied()){
                    // add a major move to the legal moves list
                    legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
                } else {
                    // if the tile is occupied
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();
                    // if the piece alliance is not the same as the King's alliance
                    if(this.pieceAlliance != pieceAlliance){
                        // add an attack move to the legal moves list
                        legalMoves.add(new Move.AttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
                    }
                }
            }
        }

        // return the list of legal moves
        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public King movePiece(final Move move) {
        return new King(move.getDestinationCoordinate(),move.getMovedPiece().getPieceAlliance());
    }


    @Override
    public String toString()
    {
        return PieceType.KING.toString();
    }

    // helper method to check if the King is on the first column
    public static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset){
        // returns true if the King is on the first column and the candidate offset is one of -9, -1, or 7
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -9 || candidateOffset == -1 || candidateOffset == 7);
    }

    // helper method to check if the King is on the eighth column
    public static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset){
        // returns true if the King is on the eighth column and the candidate offset is one of -7, 1, or 9
        return BoardUtils.EIGHTH_COLUMN[currentPosition] && (candidateOffset == -7 || candidateOffset == 1 || candidateOffset == 9);
    }
}
