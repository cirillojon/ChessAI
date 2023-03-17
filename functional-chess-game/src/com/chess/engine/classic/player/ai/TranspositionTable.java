package com.chess.engine.classic.player.ai;
import java.util.Map;
import java.util.HashMap;

public class TranspositionTable {
    public static final int EMPTY_VALUE = Integer.MIN_VALUE;
    public final Map<Long, Integer> transpositionMap;

    public TranspositionTable() {
        this.transpositionMap = new HashMap<>();
    }

    public int get(long zobristHash) {
        return transpositionMap.getOrDefault(zobristHash, EMPTY_VALUE);
    }

    public void put(long zobristHash, int value) {
        transpositionMap.put(zobristHash, value);
    }

    public static int emptyValue() {
        return EMPTY_VALUE;
    }
}