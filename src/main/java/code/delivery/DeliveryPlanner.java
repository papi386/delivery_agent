package code.delivery;

import code.model.*;
import java.util.*;

/**
 * Simple planner that assigns destinations to agents and computes routes for each.
 * For each destination, pick nearest store (in simple experiments store can be used as start)
 * Here we treat each agent as starting at its position and delivering to nearest destination.
 *
 * The planner will:
 * - For each agent, pick one destination to deliver to (greedy assignment by estimated cost via UCS)
 * - Plan route using chosen search strategy
 */
public class DeliveryPlanner {

    public static class PlanResult {
        public final Agent agent;
        public final Position goal;
        public final String result; // plan;cost;nodes|extras
        public PlanResult(Agent a, Position g, String r){ agent=a;goal=g;result=r; }
    }

    /**
     * Assign each agent to the closest destination (greedy) and plan route.
     */
    public static List<PlanResult> planAll(Grid grid, String strategy){
        List<PlanResult> out = new ArrayList<>();
        List<Position> remainingDest = new ArrayList<>(grid.destinations);
        for(Agent agent : grid.agents){
            if(remainingDest.isEmpty()) break;
            // choose nearest dest by Manhattan
            Position best = null;
            int bestDist = Integer.MAX_VALUE;
            for(Position d : remainingDest){
                int man = Math.abs(agent.pos.x - d.x) + Math.abs(agent.pos.y - d.y);
                if(man < bestDist){ bestDist = man; best = d; }
            }
            if(best==null) break;
            String res = DeliverySearch.solve(grid, agent.pos, best, strategy);
            out.add(new PlanResult(agent, best, res));
            remainingDest.remove(best);
        }
        return out;
    }
}
