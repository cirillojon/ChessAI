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

public class Rook extends Piece{

    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATES = {-8, -1, 1, 8};

    Rook(int piecePosition, Alliance pieceAlliance) {
        super(piecePosition, pieceAlliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board)
    {
        // create an empty list to store legal moves
        final List<Move> legalMoves = new ArrayList<>();

        // loop through all candidate move vectors
        for(final int candidateCoordinateOffset: CANDIDATE_MOVE_VECTOR_COORDINATES)
        {
            // calculate the candidate destination coordinate
            int candidateDestinationCoordinate = this.piecePosition;

            // keep looping until an invalid tile coordinate is reached
            while(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate))
            {
                // check if the current position is in the first or eighth column
                // and the candidate offset would move the piece off the board
                if(isFirstColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset) ||
                        isEighthColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset))
                {
                    // stop looping if so
                    break;
                }

                // update the candidate destination coordinate
                candidateDestinationCoordinate += candidateCoordinateOffset;

                // check if the updated coordinate is valid
                if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate))
                {
                    // get the tile at the candidate destination coordinate
                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                    // check if the tile is unoccupied
                    if(!candidateDestinationTile.isTileOccupied())
                    {
                        // if it is unoccupied, add a MajorMove to the list of legal moves
                        legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
                    }
                    else
                    {
                        // if the tile is occupied, get the piece at the destination
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                        final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();

                        // check if the piece at the destination is of a different alliance
                        if(this.pieceAlliance != pieceAlliance)
                        {
                            // if it is of a different alliance, add an AttackMove to the list of legal moves
                            legalMoves.add(new Move.AttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
                        }
                        // stop looping since an occupied tile has been found
                        break;
                    }
                }
            }
        }

        // return the list of legal moves as an unmodifiable collection
        return Collections.unmodifiableList(legalMoves);
    }

    // check if the current position is in the first column and the candidate offset would move the piece off the board
    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset)
    {
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -1);
    }

    // check if the current position is in the eighth column and the candidate offset would move the piece off the board
    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset)
    {
        return BoardUtils.EIGHTH_COLUMN[currentPosition] && (candidateOffset == 1);
    }

}

