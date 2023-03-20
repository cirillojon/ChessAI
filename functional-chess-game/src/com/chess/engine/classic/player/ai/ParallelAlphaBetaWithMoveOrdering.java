package com.chess.engine.classic.player.ai;

import static com.chess.engine.classic.board.Move.*;
import com.chess.engine.classic.Alliance;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.BoardUtils;
import com.chess.engine.classic.board.Move;
import com.chess.engine.classic.board.MoveTransition;
import com.chess.engine.classic.player.Player;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.Comparator;
import java.util.Observable;
import java.util.ArrayList;
import java.util.List;

// AtomicInteger is used for thread-safe operations on integers.
import java.util.concurrent.atomic.AtomicInteger;

// ConcurrentHashMap is added for thread-safe use as a transposition table.
import java.util.concurrent.ConcurrentHashMap;

// Necessary imports to implement parellelism
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ParallelAlphaBetaWithMoveOrdering extends Observable implements MoveStrategy {

    private final BoardEvaluator evaluator;
    private final int searchDepth;
    private final MoveSorter moveSorter;
    private long boardsEvaluated;
    private long executionTime;
    private int quiescenceCount;
    private int cutOffsProduced;

    // Added: Transposition table to store previously computed values.
    private final ConcurrentHashMap<Long, Integer> transpositionTable = new ConcurrentHashMap<>();

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

    public ParallelAlphaBetaWithMoveOrdering(final int searchDepth) {
        this.evaluator = StandardBoardEvaluator.get();
        this.searchDepth = searchDepth;
        this.moveSorter = MoveSorter.SORT;
        this.boardsEvaluated = 0;
        this.quiescenceCount = 0;
        this.cutOffsProduced = 0;
    }

    @Override
    public String toString() {
        return "PA+AB+MO";
    }

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    @Override
    public Move execute(final Board board) {
        final long startTime = System.currentTimeMillis();


        // Use a fixed thread pool via an ExecutorService to parallelize the search.
        final int numThreads = Runtime.getRuntime().availableProcessors();
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        final Player currentPlayer = board.currentPlayer();
        final Alliance alliance = currentPlayer.getAlliance();
        final Move[] bestMove = {MoveFactory.getNullMove()};

        //AtomicIntegers are used for thread-safe operations
        AtomicInteger highestSeenValue = new AtomicInteger(Integer.MIN_VALUE);
        AtomicInteger lowestSeenValue = new AtomicInteger(Integer.MAX_VALUE);
        AtomicInteger moveCounter = new AtomicInteger(1);

        final int numMoves = this.moveSorter.sort(board.currentPlayer().getLegalMoves()).size();
        System.out.println(board.currentPlayer() + " THINKING with depth = " + this.searchDepth);
        System.out.println("\tOrdered moves! : " + this.moveSorter.sort(board.currentPlayer().getLegalMoves()));

        List<Future<MoveResult>> futures = new ArrayList<>();
        for (final Move move : this.moveSorter.sort(board.currentPlayer().getLegalMoves())) {
            // Submit tasks to the executor for parallel execution.
            futures.add(executor.submit(() -> {
                final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                this.quiescenceCount = 0;
                if (moveTransition.getMoveStatus().isDone()) {
                    final long candidateMoveStartTime = System.nanoTime();
                    int currentValue = alliance.isWhite() ?
                            min(moveTransition.getToBoard(), this.searchDepth - 1, highestSeenValue.get(), lowestSeenValue.get()) :
                            max(moveTransition.getToBoard(), this.searchDepth - 1, highestSeenValue.get(), lowestSeenValue.get());

                    final String quiescenceInfo = " [h: " + highestSeenValue + " l: " + lowestSeenValue + "] q: " + this.quiescenceCount;
                    final String s = "\t" + toString() + "(" + this.searchDepth + "), m: (" + moveCounter.getAndIncrement() + "/" + numMoves + ") " + move + ", best:  " + bestMove[0]
                            + quiescenceInfo + ", t: " + calculateTimeTaken(candidateMoveStartTime, System.nanoTime()) + ", thread: " + Thread.currentThread().getName();

                    System.out.println(s);

                    setChanged();
                    notifyObservers(s);

                    return new MoveResult(move, currentValue);
                } else {
                    return new MoveResult(move, alliance.isWhite() ? Integer.MIN_VALUE : Integer.MAX_VALUE);
                }
            }));
        }

        // Collect the results from the futures and update the best move.
        try {
            for (Future<MoveResult> future : futures) {
                MoveResult moveResult = future.get();
                Move move = moveResult.move;
                int currentValue = moveResult.value;

                if (alliance.isWhite() && currentValue > highestSeenValue.get()) {
                    highestSeenValue.set(currentValue);
                    bestMove[0] = move;
                } else if (alliance.isBlack() && currentValue < lowestSeenValue.get()) {
                    lowestSeenValue.set(currentValue);
                    bestMove[0] = move;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Shutdown the executor after all tasks are done.
        executor.shutdown();

        this.executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("%s SELECTS %s [#boards evaluated = %d, time taken = %d ms, eval rate = %.1f cutoffCount = %d prune percent = %.2f\n", board.currentPlayer(),
                bestMove[0], this.boardsEvaluated, this.executionTime, (1000 * ((double) this.boardsEvaluated / this.executionTime)), this.cutOffsProduced, 100 * ((double) this.cutOffsProduced / this.boardsEvaluated));
        return bestMove[0];
    }


    public int max(final Board board,
                   final int depth,
                   final int highest,
                   final int lowest) {
        if (depth == 0 || BoardUtils.isEndGame(board)) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }

    /*
     - Calculates a Zobrist hash for a given game board configuration.
     - A Zobrist hash is a unique identifier for the board state, generated
     - by XOR-ing the precomputed hash values of each piece on the board.
     - This allows for efficient comparison of board states without having
     - to check each piece individually.

     - A method was added to the Board class to allow the transpoisiton table to be implemented using zobrist hashing
     */
        final long boardHash = board.getZobristHash();

        // Changed: Use the transposition table to cache and retrieve previously computed values.
        if (transpositionTable.containsKey(boardHash)) {
            return transpositionTable.get(boardHash);
        }
        int currentHighest = highest;
        for (final Move move : this.moveSorter.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                currentHighest = Math.max(currentHighest, min(moveTransition.getToBoard(),
                        calculateQuiescenceDepth(board, move, depth), currentHighest, lowest));
                if (lowest <= currentHighest) {
                    this.cutOffsProduced++;
                    break;
                }
            }
        }
        transpositionTable.put(boardHash, currentHighest);
        return currentHighest;
    }

    public int min(final Board board,
                   final int depth,
                   final int highest,
                   final int lowest) {
        if (depth == 0 || BoardUtils.isEndGame(board)) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }

        //Calculates a Zobrist hash for a given game board configuration.
        final long boardHash = board.getZobristHash();

        // Use the transposition table to cache and retrieve previously computed values.
        if (transpositionTable.containsKey(boardHash)) {
            return transpositionTable.get(boardHash);
        }
        int currentLowest = lowest;
        for (final Move move : this.moveSorter.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                currentLowest = Math.min(currentLowest, max(moveTransition.getToBoard(),
                        calculateQuiescenceDepth(board, move, depth), highest, currentLowest));
                if (currentLowest <= highest) {
                    this.cutOffsProduced++;
                    break;
                }
            }
        }
        transpositionTable.put(boardHash, currentLowest);
        return currentLowest;
    }

    private int calculateQuiescenceDepth(final Board board,
                                         final Move move,
                                         final int depth) {
        return depth - 1;
    }

    private static String calculateTimeTaken(final long start, final long end) {
        final long timeTaken = (end - start) / 1000000;
        return timeTaken + " ms";
    }

    private static class MoveResult {
        private final Move move;
        private final int value;

        public MoveResult(Move move, int value) {
            this.move = move;
            this.value = value;
        }
    }
}