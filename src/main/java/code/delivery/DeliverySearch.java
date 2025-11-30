package code.delivery;

import code.model.Grid;
import code.model.Position;
import code.search.GeneralSearch;
import code.search.SearchStrategy;

/**
 * DeliverySearch provides the static solve method required by the assignment.
 * solve returns "plan;cost;nodesExpanded" where plan is comma-separated actions.
 */
public class DeliverySearch {

    /**
     * Solve single-source single-goal delivery problem.
     * @param grid grid
     * @param start starting position
     * @param goal goal position
     * @param strat strategy name (BFS, DFS, UCS, IDS, GREEDY, ASTAR)
     * @return result string plan;cost;nodesExpanded
     */
    public static String solve(Grid grid, Position start, Position goal, String strat){
        DeliveryProblem problem = new DeliveryProblem(grid, start, goal);
        SearchStrategy s;
        try { s = SearchStrategy.valueOf(strat.toUpperCase()); }
        catch(Exception e){ return "ERROR: unknown strategy"; }

        long t0 = System.nanoTime();
        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        GeneralSearch.Result res = GeneralSearch.generalSearch(problem, s);
        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long t1 = System.nanoTime();

        if(res.node == null) return "null;inf;" + res.nodesExpanded;

        // reconstruct plan
        StringBuilder sb = new StringBuilder();
        java.util.List<String> actions = new java.util.ArrayList<>();
        code.search.Node cur = res.node;
        while(cur.parent != null){
            actions.add(cur.action);
            cur = cur.parent;
        }
        java.util.Collections.reverse(actions);
        for(int i=0;i<actions.size();i++){
            if(i>0) sb.append(",");
            sb.append(actions.get(i));
        }
        String plan = sb.toString();
        String extra = String.format("time_ms=%.3f,mem_bytes=%d", (t1-t0)/1e6, (memAfter - memBefore));
        // return plan;cost;nodesExpanded|extras
        return plan + ";" + res.node.pathCost + ";" + res.nodesExpanded + "|" + extra;
    }
}
