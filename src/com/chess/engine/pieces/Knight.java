package com.chess.engine.pieces;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import java.util.List;

public class Knight extends Piece{

    private final static int [] CANDIDATE_MOVE_COORDINATES = {-17, -15, -10, -6, 6, 10, 15, 17};


     Knight(final int piecePosition, final Alliance pieceAlliance) {
        super(piecePosition, pieceAlliance);
    }

    @Override
    public List<Move> calculateLegalMoves(Board board) {
        return null;
    }


}
