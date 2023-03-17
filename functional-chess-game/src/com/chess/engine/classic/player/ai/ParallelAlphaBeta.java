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

import java.util.Observable;



public class ParallelAlphaBeta extends Observable implements MoveStrategy {
    private final int searchDepth;
    private final int maxThreads;
    private final AtomicInteger boardsEvaluated;
    private long executionTime;
    private final AtomicInteger cutOffsProduced;
    private AtomicInteger numBoardsEvaluated;
    private final TranspositionTable transpositionTable;

    public ParallelAlphaBeta(final int searchDepth, int maxThreads) {
        this.searchDepth = searchDepth;
        this.maxThreads = maxThreads;
        this.boardsEvaluated = new AtomicInteger();
        this.cutOffsProduced = new AtomicInteger();
        this.transpositionTable = new TranspositionTable();
        this.numBoardsEvaluated = new AtomicInteger(0);
    }

    public Move execute(final Board board) {
        final long startTime = System.currentTimeMillis();
        final Player currentPlayer = board.currentPlayer();
        final Alliance alliance = currentPlayer.getAlliance();

        ForkJoinPool pool = new ForkJoinPool(maxThreads);

        System.out.println(board.currentPlayer() + " THINKING with depth = " + this.searchDepth);

        List<Move> orderedMoves = orderMoves(currentPlayer.getLegalMoves(), board, alliance);

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : orderedMoves) {
            MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                int value = alphaBeta(moveTransition.getToBoard(), this.searchDepth - 1, bestScore, Integer.MAX_VALUE, !alliance.isWhite(), pool);
                if (value > bestScore) {
                    bestScore = value;
                    bestMove = move;
                }
            }
        }

        if (bestMove != null) {
            System.out.println("\t" + toString() + "(" + this.searchDepth + "), best: " + bestMove);
        }

        executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("%s SELECTS %s [#boards evaluated = %d, time taken = %d ms, eval rate = %.1f cutoffCount = %d prune percent = %.2f\n", board.currentPlayer(),
                bestMove, boardsEvaluated.get(), executionTime, (1000 * ((double) boardsEvaluated.get() / executionTime)), cutOffsProduced.get(), 100 * ((double) cutOffsProduced.get() / boardsEvaluated.get()));
                return bestMove;
            }
            
            private List<Move> orderMoves(Collection<Move> legalMoves, Board board, Alliance alliance) {
                return legalMoves.stream()
                        .map(move -> {
                            MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                            if (moveTransition.getMoveStatus().isDone()) {
                                int value = transpositionTable.get(moveTransition.getToBoard().getZobristHash());
                                return new MoveScore(move, value);
                            } else {
                                return new MoveScore(move, Integer.MIN_VALUE);
                            }
                        })
                        .sorted(Comparator.comparing(moveScore -> moveScore.score * (alliance.isWhite() ? -1 : 1)))
                        .map(moveScore -> moveScore.move)
                        .collect(Collectors.toList());
            }
            
            
            @Override
            public String toString() {
                return "StockAB";
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
            
            private static class MoveScore {
                private final Move move;
                private final int score;
            
                private MoveScore(final Move move, final int score) {
                    this.move = move;
                    this.score = score;
                }
            }
        }            