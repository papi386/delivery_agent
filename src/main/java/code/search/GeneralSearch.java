package code.search;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Implementation of GENERAL-SEARCH from the lecture slides.
 * - Minimizes redundant states using a visited set (for graph-search)
 * - Supports different queueing functions via SearchStrategy
 *
 * For IDS we implement iterative deepening using depth-limited DFS.
 */
public class GeneralSearch {

    public static class Result {
        public final Node node;
        public final int nodesExpanded;
        public Result(Node n, int expanded){ this.node = n; this.nodesExpanded = expanded; }
    }

    public static Result generalSearch(Problem problem, SearchStrategy strat){
        switch(strat){
            case BFS: return bfs(problem);
            case DFS: return dfs(problem);
            case UCS: return ucs(problem);
            case IDS: return ids(problem, 50); // default max depth
            case GREEDY: return greedy(problem);
            case ASTAR: return aStar(problem);
            default: return new Result(null,0);
        }
    }

    // BFS: queue FIFO
    private static Result bfs(Problem problem){
        Queue<Node> frontier = new ArrayDeque<>();
        Set<Object> explored = new HashSet<>();
        frontier.add(new Node(problem.initialState()));
        int nodesExpanded = 0;
        while(!frontier.isEmpty()){
            Node node = frontier.poll();
            if(problem.goalTest(node.state)) return new Result(node, nodesExpanded);
            if(explored.contains(node.state)) continue;
            explored.add(node.state);
            nodesExpanded++;
            for(String op : problem.operators()){
                Object s2 = problem.apply(node.state, op);
                if(s2==null) continue;
                Node child = new Node(s2, node, op, problem.stepCost(node.state, op));
                frontier.add(child);
            }
        }
        return new Result(null, nodesExpanded);
    }

    // DFS: stack LIFO (graph-search with visited check)
    private static Result dfs(Problem problem){
        Deque<Node> frontier = new ArrayDeque<>();
        Set<Object> explored = new HashSet<>();
        frontier.addFirst(new Node(problem.initialState()));
        int nodesExpanded = 0;
        while(!frontier.isEmpty()){
            Node node = frontier.removeFirst();
            if(problem.goalTest(node.state)) return new Result(node, nodesExpanded);
            if(explored.contains(node.state)) continue;
            explored.add(node.state);
            nodesExpanded++;
            // push children in reverse order to keep natural operator order
            List<String> ops = problem.operators();
            for(int i=ops.size()-1;i>=0;i--){
                String op = ops.get(i);
                Object s2 = problem.apply(node.state, op);
                if(s2==null) continue;
                Node child = new Node(s2, node, op, problem.stepCost(node.state, op));
                frontier.addFirst(child);
            }
        }
        return new Result(null, nodesExpanded);
    }

    // UCS
    private static Result ucs(Problem problem){
        Comparator<Node> cmp = Comparator.comparingInt(n -> n.pathCost);
        PriorityQueue<Node> frontier = new PriorityQueue<>(cmp);
        Map<Object, Integer> best = new HashMap<>();
        frontier.add(new Node(problem.initialState()));
        int nodesExpanded = 0;
        while(!frontier.isEmpty()){
            Node node = frontier.poll();
            if(problem.goalTest(node.state)) return new Result(node, nodesExpanded);
            Integer prev = best.get(node.state);
            if(prev != null && prev <= node.pathCost) continue;
            best.put(node.state, node.pathCost);
            nodesExpanded++;
            for(String op : problem.operators()){
                Object s2 = problem.apply(node.state, op);
                if(s2==null) continue;
                int step = problem.stepCost(node.state, op);
                Node child = new Node(s2, node, op, step);
                frontier.add(child);
            }
        }
        return new Result(null, nodesExpanded);
    }

    // Greedy: priority by heuristic only (not f = g+h)
    private static Result greedy(Problem problem){
        Comparator<Node> cmp = Comparator.comparingInt(n -> {
            if(problem instanceof code.delivery.DeliveryProblem){
                return Heuristics.deliveryAdmissible((code.delivery.DeliveryProblem)problem, n.state);
            }
            return 0;
        });
        PriorityQueue<Node> frontier = new PriorityQueue<>(cmp);
        Set<Object> explored = new HashSet<>();
        frontier.add(new Node(problem.initialState()));
        int nodesExpanded = 0;
        while(!frontier.isEmpty()){
            Node node = frontier.poll();
            if(problem.goalTest(node.state)) return new Result(node, nodesExpanded);
            if(explored.contains(node.state)) continue;
            explored.add(node.state);
            nodesExpanded++;
            for(String op : problem.operators()){
                Object s2 = problem.apply(node.state, op);
                if(s2==null) continue;
                int step = problem.stepCost(node.state, op);
                Node child = new Node(s2, node, op, step);
                frontier.add(child);
            }
        }
        return new Result(null, nodesExpanded);
    }

    // A*: priority by f = g + h
    private static Result aStar(Problem problem){
        Comparator<Node> cmp = Comparator.comparingInt(n -> {
            int h = 0;
            if(problem instanceof code.delivery.DeliveryProblem){
                h = Heuristics.deliveryAdmissible((code.delivery.DeliveryProblem)problem, n.state);
            }
            return n.pathCost + h;
        });
        PriorityQueue<Node> frontier = new PriorityQueue<>(cmp);
        Map<Object,Integer> best = new HashMap<>();
        frontier.add(new Node(problem.initialState()));
        int nodesExpanded = 0;
        while(!frontier.isEmpty()){
            Node node = frontier.poll();
            if(problem.goalTest(node.state)) return new Result(node, nodesExpanded);
            Integer prev = best.get(node.state);
            if(prev != null && prev <= node.pathCost) continue;
            best.put(node.state, node.pathCost);
            nodesExpanded++;
            for(String op : problem.operators()){
                Object s2 = problem.apply(node.state, op);
                if(s2==null) continue;
                int step = problem.stepCost(node.state, op);
                Node child = new Node(s2, node, op, step);
                frontier.add(child);
            }
        }
        return new Result(null, nodesExpanded);
    }

    // IDS: iterative deepening with DFS depth-limited
    private static Result ids(Problem problem, int maxDepth){
        for(int depth=0; depth<=maxDepth; depth++){
            Result r = depthLimitedSearch(problem, depth);
            if(r.node != null) return r;
        }
        return new Result(null, 0);
    }

    private static Result depthLimitedSearch(Problem problem, int limit){
        Deque<Node> frontier = new ArrayDeque<>();
        Set<Object> explored = new HashSet<>();
        frontier.addFirst(new Node(problem.initialState()));
        int nodesExpanded = 0;
        while(!frontier.isEmpty()){
            Node node = frontier.removeFirst();
            if(problem.goalTest(node.state)) return new Result(node, nodesExpanded);
            if(node.depth >= limit) continue;
            if(explored.contains(node.state)) continue;
            explored.add(node.state);
            nodesExpanded++;
            List<String> ops = problem.operators();
            for(int i=ops.size()-1;i>=0;i--){
                String op = ops.get(i);
                Object s2 = problem.apply(node.state, op);
                if(s2==null) continue;
                Node child = new Node(s2, node, op, problem.stepCost(node.state, op));
                frontier.addFirst(child);
            }
        }
        return new Result(null, nodesExpanded);
    }
}
