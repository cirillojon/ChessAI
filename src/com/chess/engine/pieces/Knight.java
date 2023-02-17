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

// Class representing the knight piece in a chess game
public class Knight extends Piece {

    // Array of possible move offsets for the knight piece
    private final static int[] CANDIDATE_MOVE_COORDINATES = {-17, -15, -10, -6, 6, 10, 15, 17};

    // Constructor to create a new knight piece
    public Knight(final int piecePosition, final Alliance pieceAlliance) {
        super(piecePosition, pieceAlliance);
    }

    // Method to calculate legal moves for the knight piece
    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        // List to store legal moves for the knight piece
        final List<Move> legalMoves = new ArrayList<>();

        // Loop through the possible move offsets
        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            // Calculate the candidate destination coordinate
            final int candidateDestinationCoordinate = this.piecePosition + currentCandidateOffset;

            // Check if the candidate destination coordinate is valid
            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                // Check if the move is not allowed in the first, second, seventh, or eighth column
                if (isFirstColumnExclusion(this.piecePosition, currentCandidateOffset) ||
                        isSecondColumnExclusion(this.piecePosition, currentCandidateOffset) ||
                        isSeventhColumnExclusion(this.piecePosition, currentCandidateOffset) ||
                        isEighthColumnExclusion(this.piecePosition, currentCandidateOffset)) {
                    // Skip to the next iteration if the move is not allowed
                    continue;
                }

                // Get the tile at the candidate destination coordinate
                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                // Check if the destination tile is unoccupied
                if (!candidateDestinationTile.isTileOccupied()) {
                    // Add a new legal move if the destination tile is unoccupied
                    legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
                } else {
                    // Get the piece at the destination tile
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();

                    // Check if the piece at the destination has a different alliance
                    if (this.pieceAlliance != pieceAlliance) {
                        // Add a new legal move if the piece at the destination has a different alliance
                        legalMoves.add(new Move.AttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
                    }
                }
            }
        }
        // Return the unmodifiable list of legal moves
        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public String toString()
    {
        return PieceType.KNIGHT.toString();
    }


    //EDGE CASES
    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset) {
        // Check if the current position is in the first column
        // and if the candidate offset is one of the following values
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -17
                || candidateOffset == -10
                || candidateOffset == 6
                || candidateOffset == 15);
    }

    private static boolean isSecondColumnExclusion(final int currentPosition, final int candidateOffset) {
        // Check if the current position is in the second column
        // and if the candidate offset is one of the following values
        return BoardUtils.SECOND_COLUMN[currentPosition] && (candidateOffset == -10
                || candidateOffset == 6);
    }

    private static boolean isSeventhColumnExclusion(final int currentPosition, final int candidateOffset) {
        // Check if the current position is in the seventh column
        // and if the candidate offset is one of the following values
        return BoardUtils.SEVENTH_COLUMN[currentPosition] && (candidateOffset == -6
                || candidateOffset == 10);
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset) {
        // Check if the current position is in the eighth column
        // and if the candidate offset is one of the following values
        return BoardUtils.EIGHTH_COLUMN[currentPosition] && (candidateOffset == -15
                || candidateOffset == -6
                || candidateOffset == 10
                || candidateOffset == 17);
    }

}