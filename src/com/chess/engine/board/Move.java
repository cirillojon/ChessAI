package com.chess.engine.board;

import com.chess.engine.pieces.Piece;

public abstract class Move {

    // the chessboard
    final Board Board;
    // the chess piece that is moved
    final Piece movedPiece;
    // the coordinate of the destination
    final int destinationCoordinate;

    // constructor to initialize the variables
    private Move(final Board board,
                 final Piece movedPiece,
                 final int destinationCoordinate) {
        this.Board = board;
        this.movedPiece = movedPiece;
        this.destinationCoordinate = destinationCoordinate;
    }

    public int getDestinationCoordinate() {
        return this.destinationCoordinate;
    }

    public abstract Board execute();

    public Piece getMovedPiece() {
        return this.movedPiece;
    }

    // MajorMove class that extends the Move class
    public static final class MajorMove extends Move {

        // constructor to initialize the variables from superclass
        public MajorMove(final Board board,
                         final Piece movedPiece,
                         final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : this.Board.currentPlayer().getActivePieces()) {
                //TODO hashcode and equals for pieces
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final Piece piece : this.Board.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            // move the moved piece
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.Board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }
    }
    // AttackMove class that extends the Move class
    public static final class AttackMove extends Move {

        // the chess piece that is attacked
        final Piece attackedPiece;

        // constructor to initialize the variables from superclass and attacked piece
        public AttackMove(final Board board,
                          final Piece movedPiece,
                          final int destinationCoordinate,
                          final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public Board execute() {
            return null;
        }
    }

}
