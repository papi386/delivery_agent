package code;

import code.model.*;
import code.delivery.*;
import code.viz.SwingVisualizer;

import java.io.IOException;
import java.util.*;

/**
 * Main runner:
 * Usage: java -jar delivery-planner.jar [path-to-world] [STRATEGY]
 * Strategy default: UCS
 *
 * Steps:
 *  - parse world
 *  - plan for each agent using DeliveryPlanner
 *  - print results and time/memory statistics
 *  - animate routes
 */
public class Main {
    public static void main(String[] args) throws IOException {
        String worldFile = "src/main/resources/sample.world";
        String strategy = "UCS";
        if(args.length >= 1) worldFile = args[0];
        if(args.length >= 2) strategy = args[1];

        Grid grid = WorldParser.parse(worldFile);

        System.out.println("Parsed world:");
        System.out.println("Grid " + grid.width + "x" + grid.height);
        System.out.println("Stores: " + grid.stores);
        System.out.println("Destinations: " + grid.destinations);
        System.out.println("Agents: " + grid.agents);

        // Plan for each agent (one destination per agent)
        List<DeliveryPlanner.PlanResult> plans = DeliveryPlanner.planAll(grid, strategy);

        System.out.println("\nPlans (strategy=" + strategy + "):");
        Map<Agent, List<Position>> routes = new LinkedHashMap<>();
        for(DeliveryPlanner.PlanResult pr : plans){
            System.out.println("Agent " + pr.agent.id + " -> " + pr.goal + " : " + pr.result);
            // convert plan to positions for animation
            String[] parts = pr.result.split("\\|")[0].split(";");
            String plan = parts[0];
            List<Position> route = reconstructRoute(pr.agent.pos, plan, grid);
            routes.put(pr.agent, route);
        }

        // Show Swing animation
        SwingVisualizer.showFrame(grid, routes, 250,true);

        // Comparison summary: run each strategy for first agent->dest pair and print time & memory
        if(!grid.agents.isEmpty() && !grid.destinations.isEmpty()){
            Agent a = grid.agents.get(0);
            Position dest = grid.destinations.get(0);
            String[] strategies = {"BFS","DFS","UCS","IDS","GREEDY","ASTAR"};
            System.out.println("\nStrategy comparison for " + a.id + " from " + a.pos + " to " + dest);
            for(String s : strategies){
                String r = DeliverySearch.solve(grid, a.pos, dest, s);
                System.out.println(s + " => " + r);
            }
        }
    }

    private static List<Position> reconstructRoute(Position start, String plan, Grid grid){
        List<Position> route = new ArrayList<>();
        route.add(start);
        Position cur = start;
        if(plan==null || plan.equals("") || plan.equals("null")) return route;
        String[] moves = plan.split(",");
        for(String m : moves){
            Position next = null;
            switch(m){
                case "up": next = new Position(cur.x, cur.y-1); break;
                case "down": next = new Position(cur.x, cur.y+1); break;
                case "left": next = new Position(cur.x-1, cur.y); break;
                case "right": next = new Position(cur.x+1, cur.y); break;
                case "tunnel":
                    Position partner = grid.tunnelPartner(cur);
                    if(partner != null) next = partner;
                    else next = cur;
                    break;
                default: next = cur;
            }
            if(next==null) next = cur;
            route.add(next);
            cur = next;
        }
        return route;
    }
}
