package code.search;

import code.model.Position;
import code.model.Grid;
import code.delivery.DeliveryProblem;

/**
 * Heuristics used by Greedy and A*
 */
public final class Heuristics {
    private Heuristics(){}

    /** Manhattan distance between pos and goal */
    public static int manhattan(Position a, Position b){
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /** Admissible heuristic for delivery: Manhattan * minEdgeCost (lower bound) */
    public static int deliveryAdmissible(DeliveryProblem p, Object state){
        Position pos = (Position) state;
        return manhattan(pos, p.goal) * Math.max(1, p.grid.minEdgeCost());
    }
}
