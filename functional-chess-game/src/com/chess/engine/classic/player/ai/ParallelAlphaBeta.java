package com.chess.engine.classic.player.ai;

import com.chess.engine.classic.Alliance;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.BoardUtils;
import com.chess.engine.classic.board.Move;
import com.chess.engine.classic.board.MoveTransition;
import com.chess.engine.classic.player.Player;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import static com.chess.engine.classic.board.Move.*;

import java.util.concurrent.*;
import java.util.ArrayList;

import java.util.Observable;



public class ParallelAlphaBeta extends Observable implements MoveStrategy {

    public class MoveResult {
        private final Move move;
        private final int score;

        public MoveResult(final Move move, final int score) {
            this.move = move;
            this.score = score;
        }

        public Move getMove() {
            return this.move;
        }

        public int getScore() {
            return this.score;
        }
    }

    private final int searchDepth;
    private final int maxThreads;
    private final AtomicInteger boardsEvaluated;
    private long executionTime;
    private final AtomicInteger cutOffsProduced;
    private AtomicInteger numBoardsEvaluated;
    private final TranspositionTable transpositionTable;
    private final MoveSorter moveSorter;
    int bestScore;
    int highestSeenValue;
    int lowestSeenValue;

    public ParallelAlphaBeta(final int searchDepth, int maxThreads) {
        this.searchDepth = searchDepth;
        this.maxThreads = maxThreads;
        this.boardsEvaluated = new AtomicInteger();
        this.cutOffsProduced = new AtomicInteger();
        this.transpositionTable = new TranspositionTable();
        this.numBoardsEvaluated = new AtomicInteger(0);
        this.moveSorter = MoveSorter.SORT;
    }

    @Override
    public Move execute(final Board board) {
        final long startTime = System.currentTimeMillis();
        final Player currentPlayer = board.currentPlayer();
        final Alliance alliance = currentPlayer.getAlliance();
        Move bestMove = MoveFactory.getNullMove();
        highestSeenValue = Integer.MIN_VALUE;
        lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;
        final int numMoves = this.moveSorter.sort(board.currentPlayer().getLegalMoves()).size();
        System.out.println(board.currentPlayer() + " THINKING with depth = " + this.searchDepth);
        System.out.println("\tOrdered moves! : " + this.moveSorter.sort(board.currentPlayer().getLegalMoves()));

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        List<Future<MoveResult>> futures = new ArrayList<>();

        for (final Move move : this.moveSorter.sort(board.currentPlayer().getLegalMoves())) {
            futures.add(executorService.submit(() -> {
                final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                //this.quiescenceCount = 0;
                if (moveTransition.getMoveStatus().isDone()) {
                    int score = alliance.isWhite() ?
                            min(moveTransition.getToBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue) :
                            max(moveTransition.getToBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue);
                    return new MoveResult(move, score);
                }
                return new MoveResult(move, Integer.MIN_VALUE);
            }));
        }

        for (Future<MoveResult> future : futures) {
            try {
                MoveResult moveResult = future.get();
                int score = moveResult.getScore();
                //currentValue = alliance.isWhite() ? Math.abs(score) : -Math.abs(score);
                currentValue =Math.abs(score);
                if (alliance.isWhite() && currentValue < lowestSeenValue) {
                    lowestSeenValue = currentValue;
                    bestMove = moveResult.getMove();
                } else if (alliance.isBlack() && currentValue > highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = moveResult.getMove();
                }

                System.out.println("\t" + toString() + ", score: " + currentValue + ", best: " + bestMove);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();


        this.executionTime = System.currentTimeMillis() - startTime;
        double boardsEvaluatedAsDouble = this.boardsEvaluated.get();
        double cutOffsProducedAsDouble = this.cutOffsProduced.get();

        System.out.printf("%s SELECTS %s [#boards evaluated = %d, time taken = %d ms, eval rate = %.1f cutoffCount = %d prune percent = %.2f\n",
                board.currentPlayer(),
                bestMove,
                this.boardsEvaluated.get(),
                this.executionTime,
                (1000 * (boardsEvaluatedAsDouble / this.executionTime)),
                this.cutOffsProduced.get(),
                100 * (cutOffsProducedAsDouble / boardsEvaluatedAsDouble)
        );
        return bestMove;

    }

    private int min(final Board board, final int depth, int highestSeenValue, int lowestSeenValue) {
        if (depth == 0 || isEndGame(board)) {
            this.boardsEvaluated.incrementAndGet();
            return -board.currentPlayer().getAlliance().getDirection() *
                    StandardBoardEvaluator.get().evaluate(board, depth);
        }
        int currentLowestValue = lowestSeenValue;
        for (final Move move : this.moveSorter.sort(board.currentPlayer().getLegalMoves())) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getToBoard(), depth - 1, highestSeenValue, currentLowestValue);
                if (currentValue <= highestSeenValue) {
                    return currentValue;
                }
                currentLowestValue = Math.min(currentLowestValue, currentValue);
            }
        }
        return currentLowestValue;
    }

    private int max(final Board board, final int depth, int highestSeenValue, int lowestSeenValue) {
        if (depth == 0 || isEndGame(board)) {
            this.boardsEvaluated.incrementAndGet();
            return board.currentPlayer().getAlliance().getDirection() *
                    StandardBoardEvaluator.get().evaluate(board, depth);
        }
        int currentHighestValue = highestSeenValue;
        for (final Move move : this.moveSorter.sort(board.currentPlayer().getLegalMoves())) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getToBoard(), depth - 1, currentHighestValue, lowestSeenValue);
                if (currentValue >= lowestSeenValue) {
                    return currentValue;
                }
                currentHighestValue = Math.max(currentHighestValue, currentValue);
            }
        }
        return currentHighestValue;
    }
    private static boolean isEndGame(final Board board) {
        return board.currentPlayer().isInCheckMate() ||
                board.currentPlayer().isInStaleMate();
    }



    // MoveSorter class from AlphaBetaWithMoveOrdering
    private enum MoveSorter {
        SORT {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return Ordering.from(SMART_SORT).immutableSortedCopy(moves);
            }
        };

        public static Comparator<Move> SMART_SORT = new Comparator<Move>() {
            @Override
            public int compare(final Move move1, final Move move2) {
                return ComparisonChain.start()
                        .compareTrueFirst(BoardUtils.isThreatenedBoardImmediate(move1.getBoard()), BoardUtils.isThreatenedBoardImmediate(move2.getBoard()))
                        .compareTrueFirst(move1.isAttack(), move2.isAttack())
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(move2.getMovedPiece().getPieceValue(), move1.getMovedPiece().getPieceValue())
                        .result();
            }
        };

        abstract Collection<Move> sort(Collection<Move> moves);
    }


    @Override
    public String toString() {
        return "ParallelAB";
    }

    @Override
    public long getNumBoardsEvaluated() {
        return numBoardsEvaluated.get();
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizingPlayer, ForkJoinPool pool) {
        if (depth == 0 || BoardUtils.isEndGame(board)) {
            int evaluation = StandardBoardEvaluator.get().evaluate(board, depth);
            boardsEvaluated.incrementAndGet();
            return maximizingPlayer ? evaluation : -evaluation;
        }

        int valueFromTable = transpositionTable.get(board.getZobristHash());
        if (valueFromTable != TranspositionTable.EMPTY_VALUE) {
            return valueFromTable;
        }

        int value;
        if (maximizingPlayer) {
            value = Integer.MIN_VALUE;
            for (final Move move : board.currentPlayer().getLegalMoves()) {
                MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    int eval = alphaBeta(moveTransition.getToBoard(), depth - 1, alpha, beta, false, pool);
                    value = Math.max(value, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) {
                        cutOffsProduced.incrementAndGet();
                        break;
                    }
                }
            }
        } else {
            value = Integer.MAX_VALUE;
            for (final Move move : board.currentPlayer().getLegalMoves()) {
                MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    int eval = alphaBeta(moveTransition.getToBoard(), depth - 1, alpha, beta, true, pool);
                    value = Math.min(value, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
                        cutOffsProduced.incrementAndGet();
                        break;
                    }
                }
            }
        }

        transpositionTable.put(board.getZobristHash(), value);
        return value;
    }


    private class AlphaBetaTask extends RecursiveTask<Integer> {
        private final Move move;
        private final Board board;
        private final int depth;
        private final int alpha;
        private final int beta;
        private final boolean maximizingPlayer;

        AlphaBetaTask(Move move, Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
            this.move = move;
            this.board = board;
            this.depth = depth;
            this.alpha = alpha;
            this.beta = beta;
            this.maximizingPlayer = maximizingPlayer;
        }

        Move getMove() {
            return move;
        }

        @Override
        protected Integer compute() {
            MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                return maximizingPlayer ?
                        min(moveTransition.getToBoard(), depth, alpha, beta) :
                        max(moveTransition.getToBoard(), depth, alpha, beta);
            }
            return maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
    }


    private static class MoveScore {
        private final Move move;
        private final int score;

        private MoveScore(final Move move, final int score) {
            this.move = move;
            this.score = score;
        }
    }
}            