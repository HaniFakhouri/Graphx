package keerov.graphx;

import keerov.graphx.Graph.*;
import keerov.graphx.ds.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
*
* @author Keerov
* thekeerov@gmail.com
*/
public class Drawing extends javax.swing.JPanel {

	private static final Color ONE = Color.RED;
    private static final Color TWO = Color.GREEN;
    private static final int STATE_IDLE = -1;
    private static final int STATE_PRESSED = 0;
    private static final int STATE_DROPPED = 1;

    private static int GUI_EXECUTION_PAUSE = 1000;
    private static int GUI_RESULT_PAUSE = 10000;
    private static int SQUARE_SIZE = 20;
    
    private Graphics g;
    private int x0, y0, x1, y1;
    private int click_state;
    private int current_x, current_y;
    private HashMap<Point, String> coordToName = new HashMap<Point, String>();
    private GraphGUI gui;
    private boolean edit;

    private boolean doFillVertex;
    private boolean drawAxis;

    private boolean showEdgeCost;
    private boolean showEdgeFlow;
    private boolean showVertexDist;
    private boolean showVertexName;

    int width, height;

    private Graph graph;
    
    private Thread currentThread;

    /**
     * Creates new form Drawing
     */
    public Drawing(GraphGUI gui) {
        initComponents();

        graph = new Graph();

        this.gui = gui;
        edit = false;
        doFillVertex = false;
        drawAxis = false;

        showEdgeCost = true;
        showEdgeFlow = true;
        showVertexDist = true;
        showVertexName = true;

        click_state = STATE_IDLE;
        current_x = -1;
        current_y = -1;

        width = 0;
        height = 0;

        test();

        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (edit) {
                    if (click_state == STATE_IDLE) {
                        x0 = e.getX();
                        y0 = e.getY();
                        click_state = STATE_PRESSED;
                    } else if (click_state == STATE_PRESSED) {
                        x1 = e.getX();
                        y1 = e.getY();
                        click_state = STATE_DROPPED;
                    }
                }

                repaint();

                current_x = e.getX();
                current_y = e.getY();

                getClickedVertex();

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (edit) {
                    current_x = e.getX();
                    current_y = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (edit) {
                    Vertex v = getClickedVertex();
                    if (v != null) {
                        if (v.x != rel(e.getX()) || v.y != rel(e.getY())) {
                            Vertex to = getClickedVertex(e.getX(), e.getY());
                            if (to == null) { // the new position is empty

                                Point oldPoint = new Point(v.x, v.y);
                                coordToName.remove(oldPoint);

                                current_x = e.getX();
                                current_y = e.getY();
                                graph.getVertex(v.name).x = rel(current_x);
                                graph.getVertex(v.name).y = rel(current_y);

                                Point p = new Point(rel(current_x), rel(current_y));
                                if (!coordToName.containsKey(p)) {
                                    coordToName.put(p, v.name);
                                }

                                repaint();
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //System.out.println("3");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //System.out.println("2");
            }

        });

    }

    public void clearGraph() {
        current_x = -1;
        coordToName.clear();
        graph.clearGraph();
        repaint();
    }

    public void showEdgeCost(boolean show) {
        showEdgeCost = show;
        repaint();
    }

    public void showEdgeFlow(boolean show) {
        showEdgeFlow = show;
        repaint();
    }

    public void showVertexDist(boolean show) {
        showVertexDist = show;
        repaint();
    }
    
    public void showVertexName(boolean show) {
    	showVertexName = show;
    	repaint();
    }

    private Vertex getClickedVertex(int currentX, int currentY) {
        Point p = new Point(rel(currentX), rel(currentY));
        if (coordToName.containsKey(p)) {
            String vertexName = coordToName.get(p);
            Vertex v = graph.getVertex(vertexName);
            return v;
        }
        return null;
    }

    private Vertex getClickedVertex() {
        Point p = new Point(relX(), relY());
        List<String> adj = new ArrayList<String>();
        List<String> adjEnt = new ArrayList<String>();
        if (coordToName.containsKey(p)) {
            String vertexName = coordToName.get(p);
            Vertex v = graph.getVertex(vertexName);
            gui.console(v.name);
            for (Edge e : v.adj) {
                adj.add(e.dest.name);
                gui.console(e.toString());
            }
            for (Edge e : v.adjEntering) {
                adjEnt.add(e.src.name);
                gui.console(e.toString());
            }

            gui.info(vertexName, -1, -1, v.dist, v.visited, adj, adjEnt);

            return v;
        }
        gui.info("", -1, -1, -1, false, adj, adjEnt);
        return null;
    }

    public void markVisitedUnvisited() {
        if (getClickedVertex().visited) {
            getClickedVertex().visited = false;
        } else {
            getClickedVertex().visited = true;
        }
        getClickedVertex();
        repaint();
    }
    
    public void createMaze(int w, int h) {
    	if (!edit) {
    		gui.console("Click \"Edit Graph\" button");
    		return;
    	}
    	
    	clearGraph();
    	
    	boolean directed = graph.isDirected();
    	graph.setDirected(false);
    	
    	for (int j=1; j<h+1; j++) {
    		for (int i=1; i<w; i++) {
    			
    			Random rand = new Random();
    			int max = w*h;
    			int min = 1;
    			int r = rand.nextInt((max - min) + 1) + min;
    			
        		Point p1 = new Point(i, j);
        		Point p2 = new Point(i+1, j);
        		addVertex(p1, String.valueOf(i+""+j+"_"+r));
        		addVertex(p2, String.valueOf((i+1)+""+(j+1)+"_"+r));
        		addEdge(p1, p2, 1);
        	}
    	}
    	
    	for (int j=1; j<w+1; j++) {
    		for (int i=1; i<h+1; i++) {
        		Point p1 = new Point(j, i);
        		Point p2 = new Point(j, i+1);
        		Point p3 = new Point(j+1, i+1);
        		addEdge(p1, p2, 1);
        		addEdge(p1, p3, 1);
        	}
    	}
    	
    	graph.setDirected(directed);
    	
    	repaint();
    }

    public void makeComplete() {
        graph.makeCompelete();
        repaint();
    }

    public void createRandomGraph(int nrVertices) {

        clearGraph();

        int max_x = width - SQUARE_SIZE;
        int max_y = height - SQUARE_SIZE;
        int min_x = SQUARE_SIZE;
        int min_y = SQUARE_SIZE;

        Random rand = new Random();
        Set<Point> pts = new HashSet<Point>();

        while (pts.size() < nrVertices) {
            int rand_x = rand.nextInt((max_x - min_x) + 1) + min_x;
            int rand_y = rand.nextInt((max_y - min_y) + 1) + min_y;
            int x = rel(rand_x);
            int y = rel(rand_y);
            pts.add(new Point(x, y));
        }

        int index = 0;
        for (Point p : pts) {
            addVertex(p, String.valueOf(index++));
        }

        for (int i = 0; i < (pts.size() / 2); i++) {
            int rand_1 = rand.nextInt((index - 1) + 1);
            int rand_2 = rand.nextInt((index - 1) + 1);
            while (rand_1 == rand_2) {
                rand_2 = rand.nextInt((index - 1) + 1);
            }

            Vertex u = graph.getVertex(String.valueOf(rand_1));
            Vertex v = graph.getVertex(String.valueOf(rand_2));

            addEdge(u, v, 1);
        }

        clearAll();

        repaint();

    }

    public void updateGUIExecutionPause(int pause) {
        GUI_EXECUTION_PAUSE = pause;
    }
    
    public void updateGUIResultPause(int pause) {
    	GUI_RESULT_PAUSE = pause;
    }

    public int getGUIExecutionPause() {
        return GUI_EXECUTION_PAUSE;
    }
    
    public int getGUIResultPause() {
    	return GUI_RESULT_PAUSE;
    }

    public void changeScale(int newScale) {
        SQUARE_SIZE = newScale;
        repaint();
    }

    public int getScale() {
        return SQUARE_SIZE;
    }

    public void done() {
        edit = false;
        current_x = -1;
        current_y = -1;
    }

    public void edit() {
        edit = true;
    }

    public void addVertex(String newName) {
    	if (graph.containsVertex(newName)) {
            gui.console("Vertex " + newName + " already exists!");
        } else {
        	addVertex(relX(), relY(), newName);
        }
    }
    
    public void addVertexRandName() {
    	String n = "1";
    	while (graph.containsVertex(n))
    		n = String.valueOf(Integer.valueOf(n)+1);
    	addVertex(relX(), relY(), n);
    }

    private void addVertex(Point p, String newName) {
        addVertex(p.x, p.y, newName);
    }

    // x and y are not scaled here.
    // x and y are assumed to be scaled i.e. relative
    private void addVertex(int x, int y, String newName) {
        Point p = new Point(x, y);
        if (!coordToName.containsKey(p)) {
            coordToName.put(p, newName);
            graph.addVertex(x, y, newName);
        }
        repaint();
    }

    public void addEdge(int cost) {
        Point p1 = new Point(rel(x0), rel(y0));
        Point p2 = new Point(rel(x1), rel(y1));
        addEdge(p1, p2, cost);
        repaint();
    }

    private void addEdge(Vertex v1, Vertex v2, int cost) {
        addEdge(new Point(v1.x, v1.y), new Point(v2.x, v2.y), cost);
    }

    private void addEdge(Point p1, Point p2, int cost) {
        if (coordToName.containsKey(p1) && coordToName.containsKey(p2)) {
            String source = coordToName.get(p1);
            String dest = coordToName.get(p2);
            graph.addEdge(source, dest, cost);
        }
    }
    
    public void stopExecution() {
    	if (currentThread != null)
    		currentThread.interrupt();
    	currentThread = null;
    	clearAll();
    	repaint();
    }

    public void BFS() {
    	(currentThread = new BFS()).start();;
    }

    public void DFS() {
        (currentThread = new DFS()).start();
    }

    public void Dijkstra(String s, String d) {
        (currentThread = new Dijkstra(s, d)).start();
    }

    public void Kruskal_MST() {
        (currentThread = new Kruskal_MST()).start();
    }

    public void FordFulkerson_MaxFlow(boolean detectPerfectMatching) {
        (currentThread = new MaxFlow_FordFulkerson(detectPerfectMatching)).start();
    }

    private class BFS extends Thread {

        @Override
        public void run() {
            try {
                Vertex v = getClickedVertex();
                if (v == null) {
                    gui.console("Choose a vertex");
                    return;
                }
                doFillVertex = true;
                Queue<Vertex> q = new LinkedList<Vertex>();
                v.visited = true;
                q.add(v);
                while (!q.isEmpty()) {
                    v = q.poll();
                    v.vColor = Color.RED;
                    for (Edge e : v.adj) {
                        Vertex w = e.dest;
                        if (!w.visited) {
                            w.visited = true;
                            q.add(w);
                        }
                    }
                    repaint();
                    sleep(GUI_EXECUTION_PAUSE);
                }
            } catch (Exception e) {
            }
            clearAll();
            repaint();
        }
    }

    private class DFS extends Thread {

        @Override
        public void run() {
            try {
                Vertex v = getClickedVertex();
                if (v == null) {
                    gui.console("Choose a vertex");
                    return;
                }
                doFillVertex = true;
                Stack<Vertex> s = new Stack<Vertex>();
                s.push(v);
                while (!s.isEmpty()) {
                    v = s.topAndPop();
                    if (!v.visited) {
                        v.visited = true;
                        v.vColor = Color.RED;
                        for (Edge e : v.adj) {
                            s.push(e.dest);
                        }
                    }
                    repaint();
                    sleep(GUI_EXECUTION_PAUSE);
                }
            } catch (Exception e) {
            }
            clearAll();
            repaint();
        }
    }

    private class Dijkstra extends Thread {

        String source, dest;

        public Dijkstra(String source, String dest) {
            this.source = source;
            this.dest = dest;
        }

        @Override
        public void run() {

            try {

                Vertex v = graph.getVertex(source);
                if (v == null) {
                    print("Select a vertex");
                    return;
                }

                doFillVertex = true;

                PairingHeap<Path> ph = new PairingHeap<Path>();
                v.dist = 0;
                v.pos = ph.insert(new Path(v, v.dist));
                int nodesSeen = 0;

                while (!ph.isEmpty() && nodesSeen < graph.size()) {
                    Path p = ph.deleteMin();
                    v = p.dest;
                    if (v.visited) {
                        continue;
                    }
                    v.visited = true;
                    nodesSeen++;
                    v.vColor = Color.RED;
                    for (Edge e : v.adj) {
                        Vertex w = e.dest;
                        int uwv = e.cost;
                        if (w.dist > uwv + v.dist) {
                            e.eColor = Color.RED;
                            w.dist = uwv + v.dist;
                            w.prev = v;
                            Path newPath = new Path(w, w.dist);
                            if (w.pos == null) {
                                ph.insert(newPath);
                            } else {
                                ph.decreaseKey(w.pos, newPath);
                            }
                            repaint();
                            sleep(GUI_EXECUTION_PAUSE);
                        }
                    }
                }
                sleep(3000);

                graph.resetGraphColors();

                List<Vertex> shortestPath = new ArrayList<Vertex>();

                Vertex vv = graph.getVertex(dest);
                while (vv != null) {
                    shortestPath.add(vv);
                    vv = vv.prev;
                }

                for (int i = shortestPath.size() - 1; i >= 0; i--) {
                    shortestPath.get(i).vColor = Color.RED;
                    for (Edge e : shortestPath.get(i).adj) {
                    	if (i-1 >= 0 && e.dest.equals(shortestPath.get(i-1))) {
                    		e.eColor = Color.RED;
                    		break;
                    	}
                    }
                    repaint();
                    sleep(GUI_EXECUTION_PAUSE);
                }
                
                sleep(GUI_RESULT_PAUSE);

                clearAll();
                repaint();

            } catch (Exception e) {
            }

        }
    }

    private class Kruskal_MST extends Thread {

        @Override
        public void run() {
            try {

                doFillVertex = true;

                DisjointSet ds = new DisjointSet(graph.size());
                List<Edge> edges_list = new ArrayList<Edge>();
                int index = 0;
                for (Vertex v : graph.getVertices()) {
                    v.disjointSetIndex = index;
                    index++;
                    ds.find(v.disjointSetIndex);
                    for (Edge e : v.adj) {
                        edges_list.add(e);
                    }
                }

                Edge[] edges = new Edge[edges_list.size()];
                edges = edges_list.toArray(edges);

                //Arrays.sort(edges, new Edge.EdgeComparator());
                
                QuickSort qs = new QuickSort();
                qs.setComparator(new Edge.EdgeComparator());
                qs.sort(edges);

                edges_list.clear();

                for (int i = 0; i < edges.length; i++) {
                    Edge e = edges[i];
                    Vertex u = e.src;
                    Vertex v = e.dest;
                    if (!ds.sameSet(u.disjointSetIndex, v.disjointSetIndex)) {
                        ds.union(ds.find(u.disjointSetIndex), ds.find(v.disjointSetIndex));
                        edges_list.add(e);
                    }
                }

                for (Edge e : edges_list) {
                    e.eColor = Color.RED;
                    e.src.vColor = Color.RED;
                    e.dest.vColor = Color.RED;
                }

                repaint();
                sleep(GUI_RESULT_PAUSE);

            } catch (Exception e) {

            }
            clearAll();
            repaint();
        }
    }

    private class MaxFlow_FordFulkerson extends Thread {

        private boolean detectPerfectMatching;

        public MaxFlow_FordFulkerson(boolean detectPerfectMatching) {
            this.detectPerfectMatching = detectPerfectMatching;
        }

        @Override
        public void run() {

            doFillVertex = true;

            /*
             Graph residual = graph.clone();
             residual.setDirected(true);
             */
            boolean temp = graph.isDirected();
            graph.setDirected(true);

            try {

                String source = "s";
                String sink = "t";

                Graph residual = graph.clone();

                while (true) {

                    residual.clear();
                    graph.resetGraphColors();
                    
                    // Perform a BFS and update edges in the residual graph
                    Vertex source_v = residual.getVertex(source);
                    Queue<Vertex> q = new LinkedList<Vertex>();
                    q.add(source_v);

                    boolean pathFound = false;
                    int bottleneck = Integer.MAX_VALUE;

                    graph.getVertex(source_v.name).vColor = Color.RED;
                    gui.info(source_v.name, -1, -1, -1, true, null, null);
                    repaint();
                    sleep(GUI_EXECUTION_PAUSE);

                    while (!q.isEmpty()) {

                        Vertex v = q.poll();

                        for (Edge e : v.adj) {
                            Vertex w = e.dest;
                            if (!w.visited) {
                                w.visited = true;
                                w.prev = v;
                                q.add(w);

                                graph.getVertex(w.name).vColor = Color.RED;
                                gui.info(w.name, -1, -1, -1, true, null, null);
                                repaint();
                                sleep(GUI_EXECUTION_PAUSE);

                                if (w.name.equals(sink)) {
                                    pathFound = true;
                                    q.clear();
                                    break;
                                }
                            }
                        }

                    }

                    if (!pathFound) {
                        break;
                    }

                    Vertex u = residual.getVertex(sink);
                    while (u != null) {
                        if (u.prev != null) {
                            for (Edge ein : u.adjEntering) {
                                if (ein.src.name.equals(u.prev.name)) {
                                    if (ein.cost < bottleneck) {
                                        bottleneck = ein.cost;
                                        break;
                                    }
                                }
                            }
                        }
                        if (u.name.equals(source)) {
                            break;
                        }
                        u = u.prev;
                    }
                    
                    u = residual.getVertex(sink);
                    while (u != null) {
                        graph.getVertex(u.name).vColor = Color.GREEN;
                        gui.info(u.name, -1, -1, -1, true, null, null);
                        repaint();
                        sleep(GUI_EXECUTION_PAUSE);
                        if (u.prev != null) {
                            for (Edge ein : u.adjEntering) {
                                if (ein.src.name.equals(u.prev.name)) {
                                    if (ein.cost == bottleneck) {
                                        residual.removeEdge(ein.src.name, ein.dest.name);
                                        boolean updated = false;
                                        for (Edge e : ein.dest.adj) {
                                            if (ein.src.name.equals(e.dest.name)) {
                                                residual.updateEdgeCost(e.src.name, e.dest.name, e.cost + bottleneck);
                                                updated = true;
                                            }
                                        }
                                        if (!updated) {
                                            residual.addEdge(ein.dest.name, ein.src.name, bottleneck);
                                        }
                                    } else if (ein.cost > bottleneck) {
                                        residual.addEdge(ein.dest.name, ein.src.name, bottleneck);
                                        residual.updateEdgeCost(ein.src.name, ein.dest.name, ein.cost - bottleneck);
                                    }
                                    graph.updateEdgeFlow(u.prev.name, u.name, bottleneck);
                                    break;
                                }
                            }
                        }
                        u = u.prev;
                        if (u.name.equals(source)) {
                        	graph.getVertex(u.name).vColor = Color.GREEN;
                            gui.info(u.name, -1, -1, -1, true, null, null);
                            repaint();
                            sleep(GUI_EXECUTION_PAUSE);
                            break;
                        }
                    }
                    System.out.println();
                }

                graph.resetGraphColors();
                graph.getVertex(source).vColor = Color.RED;
                for (Vertex v : graph.getVertices()) {
                	for (Edge e : v.adj)
                		if (e.flow > 0)
                			e.eColor = Color.RED;
                }
                repaint();

                if (detectPerfectMatching) {
                    // Color the perfect matching
                    Vertex src = graph.getVertex(source);
                    for (Edge e : src.adj) {
                        if (e.flow == 1) {
                            Vertex w = e.dest;

                            w.vColor = Color.RED;
                            gui.info(w.name, -1, -1, -1, true, null, null);
                            repaint();
                            sleep(GUI_EXECUTION_PAUSE);

                            for (Edge ew : w.adj) { // there must be 1 such edge with flow 1
                                if (ew.flow == 1) {
                                    ew.eColor = Color.RED;
                                    ew.dest.vColor = Color.RED;
                                    repaint();
                                    sleep(GUI_EXECUTION_PAUSE);
                                }
                            }
                        }
                    }
                }

                sleep(GUI_RESULT_PAUSE);

            } catch (Exception ex) {
                Logger.getLogger(Drawing.class.getName()).log(Level.SEVERE, null, ex);
            }
            graph.setDirected(temp);
            getClickedVertex();
            clearAll();
            repaint();

        }
    }

    public void isBipartite() {
        (currentThread = new IsBipartite()).start();
    }

    private class IsBipartite extends Thread {

        @Override
        public void run() {
            gui.console(graph.isBipartite() + " ");
            graph.clear();
            doFillVertex = true;
            boolean isBipartite = true;

            for (Vertex v : graph.getVertices()) {
                if (v.visited) {
                    continue;
                }
                Queue<Vertex> q = new LinkedList<Vertex>();

                v.color = 1;
                v.visited = true;
                v.vColor = ONE;
                q.add(v);

                while (!q.isEmpty()) {

                    v = q.poll();

                    repaint();
                    try {
                        sleep(GUI_EXECUTION_PAUSE);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Drawing.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    for (Edge e : v.adj) {
                        Vertex w = e.dest;
                        if (v.color == w.color) {
                            gui.console(">> NOT BIPARTITE");
                            isBipartite = false;
                            q.clear();
                            break;
                        }
                        if (!w.visited) {

                            w.visited = true;
                            if (v.color == 1) {
                                w.color = 2;
                                w.vColor = TWO;
                            } else {
                                w.color = 1;
                                w.vColor = ONE;
                            }
                            q.add(w);
                            repaint();
                            try {
                                sleep(GUI_EXECUTION_PAUSE);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Drawing.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
                if (!isBipartite) {
                    break;
                }
            }
            if (isBipartite) {
                gui.console(">> IS BIPARTITE");
            }
            try {
            	sleep(GUI_RESULT_PAUSE);
            } catch (InterruptedException ex) {
                Logger.getLogger(Drawing.class.getName()).log(Level.SEVERE, null, ex);
            }
            doFillVertex = false;
            graph.clear();
            repaint();
        }
    }

    private void clearAll() {
        doFillVertex = false;
        graph.clear();
    }

    private int relX() {
        return rel(current_x);
    }

    private int relY() {
        return rel(current_y);
    }

    private int rel(int coord) {
        return coord / SQUARE_SIZE;
    }

    private void print(Object msg) {
        System.out.println(msg.toString());
    }

    private void removeVertex(String vertexName) {
        Vertex v = graph.getVertex(vertexName);
        Point p = new Point(v.x, v.y);
        graph.removeVertex(vertexName);
        coordToName.remove(p);
        repaint();
    }

    private void removeEdges(Vertex v) {
        graph.removeEdges(v);
    }

    public void removeVertex() {
        Point p = new Point(relX(), relY());
        String current = coordToName.get(p);
        removeVertex(current);
    }

    public void setDirected(boolean isDirected) {
        graph.setDirected(isDirected);
    }

    private void drawLine(int x0, int y0, int x1, int y1, Color color) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);
        g2d.drawLine(x0, y0, x1, y1);
    }

    private void drawVertex(int x, int y, String name, int dist, Color color) {
        Graphics2D g2d = (Graphics2D) g;

        int xx = x * SQUARE_SIZE;
        int yy = y * SQUARE_SIZE;

        Ellipse2D e = new Ellipse2D.Double(xx, yy, SQUARE_SIZE, SQUARE_SIZE);
        g2d.setColor(color);
        if (doFillVertex) {
            g2d.fill(e);
        } else {
            g2d.draw(e);
        }
        g2d.setColor(Color.BLACK);
        int fontSize = 20;
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));

        String s = name;
        if (showVertexDist) {
            s += "," + dist;
        }

        if (showVertexName) {
        	g2d.drawString(s, xx + SQUARE_SIZE / 2, yy + SQUARE_SIZE / 2);
        }
    }

    private void drawEdge2(int x0, int y0, int x1, int y1, int cost, int flow, Color color) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(color);
        if (color.equals(Color.RED))
        	g2d.setStroke(new BasicStroke(3));
        else
        	g2d.setStroke(new BasicStroke(1));

        x0 = x0 * SQUARE_SIZE + SQUARE_SIZE / 2;
        y0 = y0 * SQUARE_SIZE + SQUARE_SIZE / 2;
        x1 = x1 * SQUARE_SIZE + SQUARE_SIZE / 2;
        y1 = y1 * SQUARE_SIZE + SQUARE_SIZE / 2;

        int xx = (x0 + x1) / 2;
        int yy = (y0 + y1) / 2;

        g2d.drawLine(x0, y0, x1, y1);
        int fontSize = 20;
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));

        String s = "";
        if (showEdgeCost) {
            s = String.valueOf(cost);
        }
        if (showEdgeFlow) {
            if (showEdgeCost) {
                s = flow + "/" + cost;
            } else {
                s = String.valueOf(flow);
            }
        }

        g2d.drawString(s, xx, yy);
    }

    private void drawEdge(int x0, int y0, int x1, int y1, Color color) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);

        x0 = rel(x0) * SQUARE_SIZE + SQUARE_SIZE / 2;
        y0 = rel(y0) * SQUARE_SIZE + SQUARE_SIZE / 2;
        x1 = rel(x1) * SQUARE_SIZE + SQUARE_SIZE / 2;
        y1 = rel(y1) * SQUARE_SIZE + SQUARE_SIZE / 2;

        g2d.drawLine(x0, y0, x1, y1);
    }

    private void drawVerticals() {
        int w = getWidth();
        int h = getHeight();
        for (int i = 0; i < w; i += SQUARE_SIZE) {
            drawLine(i, 0, i, h, Color.DARK_GRAY);
        }
        for (int i = 0; i < h; i += SQUARE_SIZE) {
            drawLine(0, i, w, i, Color.DARK_GRAY);
        }
    }

    public void drawAxis(boolean draw) {
        drawAxis = draw;
        repaint();
    }

    private void drawPointer(int x, int y, Color color) {
        Graphics2D g2d = (Graphics2D) g;
        Ellipse2D ee = new Ellipse2D.Double(x - SQUARE_SIZE / 2, y - SQUARE_SIZE / 2, SQUARE_SIZE - 5, SQUARE_SIZE - 5);
        g2d.setColor(color);
        g2d.fill(ee);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.g = g;

        width = getWidth();
        height = getHeight();

        if (drawAxis) {
            drawVerticals();
        }

        if (click_state == STATE_DROPPED && edit) {
            drawEdge(x0, y0, x1, y1, Color.RED);
            click_state = STATE_IDLE;
        }

        drawPointer(current_x, current_y, Color.LIGHT_GRAY);
        if (edit) {
            drawVertex(relX(), relY(), "", -1, Color.GREEN);
        }

        for (Vertex v : graph.getVertices()) {
        	drawVertex(v.x, v.y, v.name, v.dist, v.vColor);
        	for (Edge e : v.adj) {
        		drawEdge2(e.src.x, e.src.y, e.dest.x, e.dest.y, e.cost, e.flow, e.eColor);
        	}
        }
        
        /*
        Iterator it = coordToName.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String name = (String) pairs.getValue();

            Vertex v = graph.getVertex(name);

            drawVertex(v.x, v.y, name, v.dist, v.vColor);
            for (Edge e : v.adj) {
                drawEdge2(e.src.x, e.src.y, e.dest.x, e.dest.y, e.cost, e.flow, e.eColor);
            }
        }
        */

    }

    public void save(String name) {
        try {
        	System.out.println(graph.getVertexMap().size());
        	FileOutputStream fos = new FileOutputStream(name);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            List<Vertex> vs = new ArrayList<Vertex>(graph.getVertices());
            List<SerializableVertex> ll = new ArrayList<SerializableVertex>();
            for (Vertex v : vs) {
            	SerializableVertex sv = new SerializableVertex(
            			v.x, v.y, v.name, v.adj.size());
            	for (Edge e : v.adj) {
            		sv.addAdjEdge(e.src.name, e.dest.name, e.cost);
            	}
            	ll.add(sv);
            }
            oos.writeObject(ll);
            oos.flush();
            oos.close();
        } catch (Exception ex) {
            Logger.getLogger(Drawing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void load(String name) {
        clearAll();
        try {
        	
        	coordToName.clear();
            graph.clear();
        	
        	FileInputStream fis = new FileInputStream(name);
            ObjectInput ois = new ObjectInputStream(fis);
            List<SerializableVertex> l = (List<SerializableVertex>)ois.readObject(); 
            ois.close();
            
            for (SerializableVertex v : l) {
            	graph.addVertex(v.x, v.y, v.name);
            }
            
            for (SerializableVertex v : l) {
            	for (Edge e : v.adj) {
            		graph.addEdge(e.src.name, e.dest.name, e.cost);
            	}
            }
            
            for (Vertex v : graph.getVertices()) {
            	coordToName.put(new Point(v.x, v.y), v.name);
            }
            
            repaint();

        } catch (Exception ex) {
            Logger.getLogger(Drawing.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static class Path implements Comparable<Path> {

        public Vertex dest;
        public int cost;

        public Path(Vertex dest, int cost) {
            this.dest = dest;
            this.cost = cost;
        }

        @Override
        public int compareTo(Path o) {
            if (cost == o.cost) {
                return 0;
            }
            if (cost > o.cost) {
                return 1;
            }
            return -1;
        }
    }

    private void test() {

        /**
         * 1 2 3 4 5
         * 1 2 3 5 4
         * 1 2 5 4 3
         * 1 2 5 3 4
         * 1 2 4 3 5
         * 1 2 4 5 3
         *
         * 1 3 2 4 5
         * 1 3 2 5 4
         * 1 3 5 4 2
         * 1 3 5 2 4
         * 1 3 4 2 5
         * 1 3 4 5 2
         *
         * 1 4 3 2 5
         * 1 4 3 5 2
         * 1 4 5 2 3
         * 1 4 5 3 2
         * 1 4 2 3 5
         * 1 4 2 5 3
         *
         * 1 5 3 4 2
         * 1 5 3 2 4
         * 1 5 2 4 3
         * 1 5 2 3 4
         * 1 5 4 3 2
         * 1 5 4 2 3
         */
        /*
         int[] a = new int[]{1,2,3,4,5};
        
         int[] ap = rootArray(a, 0, 0); printArray(ap);
        
         swap(ap, 4, 5); printArray(ap);
         ap = rootArray(a, 0, 0);
         swap(ap, 3, 5); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
         ap = rootArray(a, 0, 0);
         swap(ap, 3, 4); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
        
         ap = rootArray(a, 2, 3); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
         ap = rootArray(a, 2, 3);
         swap(ap, 3, 5); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
         ap = rootArray(a, 2, 3);
         swap(ap, 3, 4); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
       
         ap = rootArray(a, 2, 4); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
         ap = rootArray(a, 2, 4);
         swap(ap, 3, 5); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
         ap = rootArray(a, 2, 4);
         swap(ap, 3, 4); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
 
         ap = rootArray(a, 2, 5); printArray(ap);        
         swap(ap, 4, 5); printArray(ap);
         ap = rootArray(a, 2, 5);
         swap(ap, 3, 5); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
         ap = rootArray(a, 2, 5);
         swap(ap, 3, 4); printArray(ap);
         swap(ap, 4, 5); printArray(ap);
         */
        /**
         * 1 2 3 4 5 6
         * 1 2 3 4 6 5
         * 1 2 3 6 5 4
         * 1 2 3 6 4 5
         * 1 2 6 4 5 3
         * 1 2 6 3 5 4
         * 1 2 6 3 4 5
         * 1 2 5 4 3 6
         * 1 2 5 6 3 4
         * 1 2 5 6 4 3
         * 1 2 4 3 5 6
         * 1 2 4 6 5 3
         * 1 2 4 6 3 5
         */
    }

    private void swap(int[] a, int pos1, int pos2) {
        if (pos1 == pos2) {
            return;
        }
        int temp = a[pos1 - 1];
        a[pos1 - 1] = a[pos2 - 1];
        a[pos2 - 1] = temp;
    }

    private int[] rootArray(int[] a, int pos1, int pos2) {
        int[] root = new int[a.length];
        System.arraycopy(a, 0, root, 0, a.length);
        swap(root, pos1, pos2);
        return root;
    }

    private void printArray(int[] a) {
        for (int i : a) {
            System.out.print(i + " ");
        }
        System.out.println();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        setName("Form"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>                        
    // Variables declaration - do not modify                     
    // End of variables declaration                   
}
