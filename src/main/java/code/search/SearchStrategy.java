package code.search;

/**
 * Supported search strategies. These are used by GeneralSearch to choose
 * the queueing function (QING-FUN) as presented in class.
 */
public enum SearchStrategy {
    BFS,
    DFS,
    UCS,
    IDS,
    GREEDY, // greedy with heuristic h1
    ASTAR  // A* with admissible heuristic h1
}
