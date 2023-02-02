package com.chess.engine.pieces;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pawn extends Piece{
    // constructor that calls the super class constructor with the given piece position and alliance
    Pawn(final int piecePosition, final Alliance pieceAlliance) {
        super(piecePosition, pieceAlliance);
    }

    // constant array of the possible move vectors for the pawn
    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATES = {8,16};

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        // list to hold all legal moves
        final List<Move> legalMoves = new ArrayList<>();

        // loop through the possible move vectors
        for(final int currentCandidateOffset : CANDIDATE_MOVE_VECTOR_COORDINATES) {
            // calculate the destination coordinate by adding the move vector to the current position
            final int candidateDestinationCoordinate = this.piecePosition + (this.getPieceAlliance().getDirection() * currentCandidateOffset);

            // if the destination coordinate is not valid, skip this iteration
            if(!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                continue;
            }
            // if the move vector is 8 and the destination tile is not occupied
            if(currentCandidateOffset == 8 && !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                // add the move to the list of legal moves
                // need to deal with promotions
                legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
            }
            // if the move vector is 16, the pawn is in its first move, and it is either in the second row (black)
            // or the seventh row (white)
            else if (currentCandidateOffset == 16 && this.isFirstMove()
                    && (BoardUtils.SECOND_ROW[this.piecePosition]  && this.getPieceAlliance().isBlack())
                    || (BoardUtils.SEVENTH_ROW[this.piecePosition] && this.getPieceAlliance().isWhite())) {
                // calculate the coordinate behind the destination tile
                final int behindCandidateDestinationCoordinate = this.piecePosition + (this.pieceAlliance.getDirection() * 8);
                // if both the destination tile and the tile behind it are not occupied
                if(!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() &&
                        !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    // add the move to the list of legal moves
                    legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
                } else if (currentCandidateOffset == 7 && (BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite()  )){
                    if(board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                        final Piece pieceAtDestination = board.getTile(candidateDestinationCoordinate).getPiece();
                        if(this.pieceAlliance != pieceAtDestination.getPieceAlliance()) {
                            legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
                        }
                    }
                } else if (currentCandidateOffset == 9 && (BoardUtils.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite() )) {
                    if(board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                        final Piece pieceAtDestination = board.getTile(candidateDestinationCoordinate).getPiece();
                        if(this.pieceAlliance != pieceAtDestination.getPieceAlliance()) {
                            legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
                        }
                    }
                } else if (currentCandidateOffset == 7 && (BoardUtils.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack() )) {
                    if(board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                        final Piece pieceAtDestination = board.getTile(candidateDestinationCoordinate).getPiece();
                        if(this.pieceAlliance != pieceAtDestination.getPieceAlliance()) {
                            legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
                        }
                    }
                } else if (currentCandidateOffset == 9 && (BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack() )) {
                    if(board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                        final Piece pieceAtDestination = board.getTile(candidateDestinationCoordinate).getPiece();
                        if(this.pieceAlliance != pieceAtDestination.getPieceAlliance()) {
                            legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
                        }
                    }
                }
            }
        }
        // return the list of legal moves
        return legalMoves;
    }
}
