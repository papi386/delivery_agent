package code.viz;

import code.model.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * Improved Swing visualizer for 25x25 grids and single-agent step-by-step mode.
 */
public class SwingVisualizer extends JPanel {

    private final Grid grid;
    private final Map<String, Color> agentColor = new HashMap<>();
    private final Map<String, List<Position>> routes = new HashMap<>();
    private final Map<String, Integer> indices = new HashMap<>();

    private int cellSize;

    public SwingVisualizer(Grid g){
        this.grid = g;

        // Auto compute cell size so the full grid fits on screen
        int maxDim = Math.max(grid.width, grid.height);
        this.cellSize = Math.max(20, 600 / maxDim);

        setPreferredSize(new Dimension(grid.width * cellSize, grid.height * cellSize));

        // Assign unique colors to agents
        Color[] palette = {
                Color.RED, Color.BLUE, Color.MAGENTA,
                Color.ORANGE, Color.CYAN, Color.GREEN.darker(),
                Color.PINK, Color.YELLOW, Color.GRAY
        };
        int i = 0;
        for(Agent a : g.agents){
            agentColor.put(a.id, palette[i % palette.length]);
            i++;
        }
    }

    public void setRoute(Agent agent, List<Position> route){
        routes.put(agent.id, route);
        indices.put(agent.id, 0);
    }

    /** Animate all agents simultaneously */
    public void startAnimation(int delayMs){
        Timer timer = new Timer(delayMs, e -> {
            boolean any = false;
            for(String id : routes.keySet()){
                int idx = indices.get(id);
                List<Position> r = routes.get(id);
                if(idx < r.size() - 1){
                    indices.put(id, idx + 1);
                    any = true;
                }
            }
            repaint();
            if(!any){
                ((Timer)e.getSource()).stop();
            }
        });
        timer.start();
    }

    /** NEW: Animate each agent one at a time */
    public void startAnimationSequential(int delayMs){
        java.util.List<String> agentOrder = new ArrayList<>(routes.keySet());

        Timer[] timers = new Timer[1]; // work-around to modify inside lambda

        timers[0] = new Timer(delayMs, new AbstractAction() {
            int agentIndex = 0;

            @Override
            public void actionPerformed(ActionEvent e) {

                if(agentIndex >= agentOrder.size()){
                    timers[0].stop();
                    return;
                }

                String currentAgent = agentOrder.get(agentIndex);
                List<Position> r = routes.get(currentAgent);
                int idx = indices.get(currentAgent);

                if(idx < r.size() - 1){
                    indices.put(currentAgent, idx + 1);
                    repaint();
                } else {
                    agentIndex++;
                }
            }
        });

        timers[0].start();
    }


    @Override protected void paintComponent(Graphics g0){
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        // smooth graphics
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g);
        drawBlockedEdges(g);
        drawAgents(g);
    }

    /** Draw the background grid and labels */
    private void drawGrid(Graphics2D g){
        for(int y = 0; y < grid.height; y++){
            for(int x = 0; x < grid.width; x++){
                int sx = x * cellSize;
                int sy = y * cellSize;

                Position p = new Position(x, y);

                if(grid.stores.contains(p)) g.setColor(new Color(80,130,230));
                else if(grid.destinations.contains(p)) g.setColor(new Color(60,200,100));
                else g.setColor(Color.WHITE);

                g.fillRect(sx, sy, cellSize, cellSize);

                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(sx, sy, cellSize, cellSize);
            }
        }
    }

    /** Blocked edges appear as thick black lines */
    private void drawBlockedEdges(Graphics2D g){
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4f));

        for(int y=0; y<grid.height; y++){
            for(int x=0; x<grid.width; x++){
                Position p = new Position(x,y);

                // Check neighbors for blocked edges
                Position[] neigh = {
                        new Position(x+1,y),
                        new Position(x,y+1)
                };

                for(Position n : neigh){
                    if(!grid.inBounds(n)) continue;

                    int cost = grid.moveCost(p, n);

                    if(cost == Integer.MAX_VALUE/4 || cost == 0){
                        int cx1 = x*cellSize + cellSize/2;
                        int cy1 = y*cellSize + cellSize/2;
                        int cx2 = n.x*cellSize + cellSize/2;
                        int cy2 = n.y*cellSize + cellSize/2;
                        g.drawLine(cx1, cy1, cx2, cy2);
                    }
                }
            }
        }
    }

    /** Draw agents (circles + text) */
    private void drawAgents(Graphics2D g){
        for(Agent a : grid.agents){
            List<Position> r = routes.get(a.id);
            int idx = indices.getOrDefault(a.id, 0);

            Position cur = (r == null || r.isEmpty()) ? a.pos : r.get(Math.min(idx, r.size()-1));

            int rx = cur.x * cellSize + 5;
            int ry = cur.y * cellSize + 5;

            g.setColor(agentColor.get(a.id));
            g.fillOval(rx, ry, cellSize-10, cellSize-10);

            g.setColor(Color.BLACK);
            g.drawString(a.id, rx + cellSize/4, ry + cellSize/2);
        }
    }

    public static void showFrame(Grid grid, Map<Agent,List<Position>> routes, int delayMs, boolean sequential){
        JFrame frame = new JFrame("Grid Visualizer");

        SwingVisualizer vis = new SwingVisualizer(grid);

        for(Map.Entry<Agent,List<Position>> e : routes.entrySet()){
            vis.setRoute(e.getKey(), e.getValue());
        }

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(vis);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if(sequential) vis.startAnimationSequential(delayMs);
        else vis.startAnimation(delayMs);
    }
}
