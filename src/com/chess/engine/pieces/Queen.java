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

public class Queen extends Piece {

    private final static int[] CANDIDATE_MOVE_VECTOR_COORDINATES = {-9, -8, -7, -1, 1, 7, 8, 9};
    public Queen(final int piecePosition, final Alliance pieceAlliance) {
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
                if(isFirstColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset) ||
                        isEighthColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset))
                {
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

    @Override
    public String toString()
    {
        return PieceType.QUEEN.toString();
    }

    //EDGE CASES

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset)
    {
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -1 || candidateOffset == -9 || candidateOffset == 7);
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset)
    {
        return BoardUtils.EIGHTH_COLUMN[currentPosition] && (candidateOffset == 1 || candidateOffset == -7 || candidateOffset == 9);
    }


}
