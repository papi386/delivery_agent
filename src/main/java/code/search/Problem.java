package code.search;

import java.util.List;

/**
 * Abstract Data Type for a generic search problem.
 * Concrete problems must implement these methods.
 */
public abstract class Problem {
    /** initial state */
    public abstract Object initialState();
    /** test whether state is a goal */
    public abstract boolean goalTest(Object state);
    /** return list of possible operators (domain-specific) */
    public abstract List<String> operators();
    /** apply operator to state => new state or null if not applicable */
    public abstract Object apply(Object state, String operator);
    /** cost of applying operator in given state */
    public abstract int stepCost(Object state, String operator);
}
