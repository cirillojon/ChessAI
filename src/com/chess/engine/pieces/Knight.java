package com.chess.engine.pieces;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Knight extends Piece{

    // Candidate move offsets for the knight piece
    private final static int [] CANDIDATE_MOVE_COORDINATES = {-17, -15, -10, -6, 6, 10, 15, 17};

    // Constructor
    Knight(final int piecePosition, final Alliance pieceAlliance) {
        super(piecePosition, pieceAlliance);
    }

    // Override method to calculate legal moves for the knight piece
    @Override
    public List<Move> calculateLegalMoves(Board board) {

        // Candidate destination coordinate
        int candidateDestinationCoordinate;
        final List<Move> legalMoves = new ArrayList<>();

        // Loop through candidate move offsets
        for(final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            candidateDestinationCoordinate = this.piecePosition + currentCandidateOffset;

            // Check if the candidate destination coordinate is valid
            if(true /*isValidTileCoordinate*/) {
                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                // Check if the destination tile is unoccupied
                if(!candidateDestinationTile.isTileOccupied()) {
                    legalMoves.add(new Move());
                } else {
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();

                    // Check if the piece at the destination has a different alliance
                    if(this.pieceAlliance != pieceAlliance) {
                        legalMoves.add(new Move());
                    }
                }
            }
        }

        // Return the unmodifiable list of legal moves
        return Collections.unmodifiableList(legalMoves);
    }
}
