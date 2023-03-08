package com.chess.engine.player;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Player {

    // Instance variables for the player's board, king, legal moves, and check status.
    protected final Board board;
    protected final King playerKing;
    protected final Collection<Move> legalMoves;
    private final boolean isInCheck;

    // Constructor for the Player class.
    Player(final Board board,
           final Collection<Move> legalMoves,
           final Collection<Move> opponentMoves){

        // Initializes instance variables.
        this.board = board;
        this.playerKing = establishKing();
        legalMoves.addAll(calculateKingCastles(legalMoves, opponentMoves));
        this.legalMoves = Collections.unmodifiableCollection(legalMoves);
        this.isInCheck = !Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentMoves).isEmpty();

    }

    // Getter method for the player's king.
    public King getPlayerKing() {
        return this.playerKing;
    }

    // Getter method for the player's legal moves.
    public Collection<Move> getLegalMoves() {
        return this.legalMoves;
    }

    // Calculates the moves that attack a certain tile.
    protected static Collection<Move> calculateAttacksOnTile(int piecePosition, Collection<Move> moves) {
        final List<Move> attackMoves = new ArrayList<>();
        for(final Move move: moves){
            if(piecePosition == move.getDestinationCoordinate()){
                attackMoves.add(move);
            }
        }

        return Collections.unmodifiableList(attackMoves);
    }

    // Finds and returns the player's king piece.
    private King establishKing() {
        for(final Piece piece: getActivePieces()){
            if(piece.getPieceType().isKing()){
                return (King) piece;
            }
        }
        throw new RuntimeException("Should not reach here! Not a valid board");
    }

    // Checks if a move is legal for the player.
    public boolean isMoveLegal(final Move move){
        return this.legalMoves.contains(move);
    }

    // Checks if the player is in check.
    public boolean isInCheck(){
        return this.isInCheck;
    }

    // Checks if the player is in checkmate.
    public boolean isInCheckMate(){
        return this.isInCheck && !hasEscapeMoves();
    }

    // Checks if the player is in stalemate.
    public boolean isInStaleMate(){
        return !this.isInCheck && !hasEscapeMoves();
    }

    // Checks if the player has any escape moves.
    protected boolean hasEscapeMoves() {
        for(final Move move: this.legalMoves){
            final MoveTransition transition = makeMove(move);
            if(transition.getMoveStatus().isDone()){
                return true;
            }
        }
        return false;
    }

    // Checks if the player has castled.
    public boolean isCastled(){
        return false;
    }

    // This method is used to execute a move and return a MoveTransition object that represents the result of the move.
    // The MoveTransition object contains the new board state after the move, the move itself, and a MoveStatus enum value
    // indicating whether the move was legal, left the player in check, or was successful.
    public MoveTransition makeMove(final Move move){

        // Check if the move is legal by checking if it is contained in the list of legal moves for the player.
        if(!isMoveLegal(move)){
            return new MoveTransition(this.board, move, MoveStatus.ILLEGAL_MOVE);
        }

        // Execute the move on a new board to get the new board state.
        final Board transitionBoard = move.execute();

        // Get a collection of all the moves that attack the opponent's king on the new board state.
        final Collection<Move> kingAttacks = Player.calculateAttacksOnTile(
                transitionBoard.currentPlayer().getOpponent().getPlayerKing().getPiecePosition(),
                transitionBoard.currentPlayer().getLegalMoves());

        // If there are any king attacks, the move leaves the player in check, so return a MoveTransition object
        // with a MoveStatus of LEAVES_PLAYER_IN_CHECK.
        if(!kingAttacks.isEmpty()){
            return new MoveTransition(this.board, move, MoveStatus.LEAVES_PLAYER_IN_CHECK);
        }

        // If the move is legal and doesn't leave the player in check, return a MoveTransition object with a MoveStatus of DONE.
        return new MoveTransition(transitionBoard, move, MoveStatus.DONE);
    }
    public abstract Collection<Piece> getActivePieces();
    public abstract Alliance getAlliance();
    public abstract Player getOpponent();
    protected abstract Collection<Move> calculateKingCastles(Collection<Move> playerLegals, Collection<Move> opponentLegals);


}
