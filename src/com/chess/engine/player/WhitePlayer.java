package com.chess.engine.player;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Collection;


public class WhitePlayer extends Player{
        public WhitePlayer(final Board board,
                           final Collection<Move> whiteStandardLegalMoves,
                           final Collection<Move> blackStandardLegalMoves){
            super(board, whiteStandardLegalMoves, blackStandardLegalMoves);
        }

        @Override
        public Collection<Piece> getActivePieces() {
                return this.board.getWhitePieces();
        }

        @Override
        public Alliance getAlliance() {
                return Alliance.WHITE;
        }

        @Override
        public Player getOpponent() {
                return this.board.blackPlayer();
        }

        @Override
        protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals, final Collection<Move> opponentLegals) {
                final List<Move> kingCastles = new ArrayList<>();
                if(this.playerKing.isFirstMove() && !this.isInCheck()) {
                        // White king side castle
                        if(!this.board.getTile(61).isTileOccupied() &&
                                !this.board.getTile(62).isTileOccupied()) {
                                 final Piece rookTile = this.board.getTile(63).getPiece();
                                if(rookTile != null && rookTile.isFirstMove()) {
                                        if(Player.calculateAttacksOnTile(61, opponentLegals).isEmpty() &&
                                                Player.calculateAttacksOnTile(62, opponentLegals).isEmpty() &&
                                                rookTile.getPieceType().isRook())
                                                kingCastles.add(new Move.KingSideCastleMove(this.board,
                                                        this.playerKing,
                                                        62,
                                                        (Rook)rookTile,
                                                        rookTile.getPiecePosition(),
                                                        61));
                                }
                        }
                        // White queen side castle
                        if(!this.board.getTile(59).isTileOccupied() &&
                                !this.board.getTile(58).isTileOccupied() &&
                                !this.board.getTile(57).isTileOccupied()) {
                                final Piece rookTile = this.board.getTile(56).getPiece();
                                if(rookTile != null && rookTile.isFirstMove()) {
                                        kingCastles.add(new Move.QueenSideCastleMove(this.board,
                                                this.playerKing,
                                                58,
                                                (Rook)rookTile,
                                                rookTile.getPiecePosition(),
                                                59));
                                }
                        }
                }
                return Collections.unmodifiableList(kingCastles);
        }
}
