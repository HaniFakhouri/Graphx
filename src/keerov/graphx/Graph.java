package keerov.graphx;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import keerov.graphx.ds.PairingHeap.Position;

/**
 *
 * @author Keerov
 * thekeerov@gmail.com
 */
public class Graph implements Serializable {

    private static final long serialVersionUID = 1L;
    private HashMap<String, Vertex> vertexMap;
    private boolean isDirected;

    public Graph() {
        vertexMap = new HashMap<String, Vertex>();
        isDirected = true;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public void clearGraph() {
        clear();
        vertexMap.clear();
    }

    public void clear() {
        for (Vertex v : vertexMap.values()) {
            v.clear();
        }
    }

    public void unvisitVertices() {
        for (Vertex v : vertexMap.values()) {
            v.visited = false;
        }
    }

    public void resetGuiColors() {
        for (Vertex v : vertexMap.values()) {
            v.vColor = Color.BLUE;
        }
    }

    public int size() {
        return vertexMap.size();
    }

    public Vertex getVertex(String vertexName) {
        return vertexMap.get(vertexName);
    }

    public Vertex getFirstVertex() {
        return vertexMap.get(vertexMap.values().iterator().next().name);
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean containsVertex(String vertexName) {
        return vertexMap.get(vertexName) != null;
    }

    public void addVertex(int x, int y, String vertexName) {
        if (vertexMap.get(vertexName) != null) {
            return;
        }
        Point p = new Point(x, y);
        Vertex v = new Vertex(vertexName);
        v.x = p.x;
        v.y = p.y;
        vertexMap.put(vertexName, v);
    }

    public Collection<Vertex> getVertices() {
        return vertexMap.values();
    }

    public HashMap<String, Vertex> getVertexMap() {
        HashMap<String, Vertex> copy = new HashMap<String, Vertex>();
        @SuppressWarnings("rawtypes")
        Iterator it = vertexMap.entrySet().iterator();
        while (it.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry pairs = (Map.Entry) it.next();
            copy.put((String) pairs.getKey(), (Vertex) pairs.getValue());
        }
        return copy;
    }

    public void useVertexMap(Map<String, Vertex> map) {
        clear();
        clearGraph();
        vertexMap = new HashMap<String, Vertex>();
        @SuppressWarnings("rawtypes")
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry pairs = (Map.Entry) it.next();
            vertexMap.put((String) pairs.getKey(), (Vertex) pairs.getValue());
        }
    }

    public void resetGraphColors() {
        for (Vertex v : vertexMap.values()) {
            v.vColor = Color.BLUE;
            for (Edge e : v.adj) {
                e.eColor = Color.BLUE;
            }
            for (Edge e : v.adjEntering) {
                e.eColor = Color.BLUE;
            }
        }
    }

    public void updateEdgeCost(String src, String dest, int newCost) {
        Vertex v = vertexMap.get(src);
        for (Edge e : v.adj) {
            if (e.dest.name.equals(dest)) {
                e.cost = newCost;
            }
        }
    }

    /**
     * This method is used in conjunction with the Ford-Fulkerson Maximum-Flow
     * algorithm
     *
     * @param src
     * @param dest
     * @param newFlow
     */
    public void updateEdgeFlow(String src, String dest, int newFlow) {
        boolean found = false;
        Vertex v = vertexMap.get(src);
        for (Edge e : v.adj) {
            if (e.dest.name.equals(dest)) {
                e.flow += newFlow;
                found = true;
                break;
            }
        }
        if (!found) {
            for (Edge e : v.adjEntering) {
                if (e.src.name.equals(dest)) {
                    e.flow -= newFlow;
                    break;
                }
            }
        }
    }

    public void addEdge(String source, String dest, int cost) {
        Vertex source_v = vertexMap.get(source);
        Vertex dest_v = vertexMap.get(dest);

        boolean found = false;

        for (Edge e : source_v.adj) {
            if (e.dest.name.equals(dest)) {
                e.cost = cost;
                found = true;
                for (Edge ee : dest_v.adjEntering) {
                    if (ee.src.name.equals(source)) {
                        ee.cost = cost;
                        break;
                    }
                }
                if (!isDirected) {
                    for (Edge ee : source_v.adjEntering) {
                        if (ee.src.name.equals(dest)) {
                            ee.cost = cost;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        }

        if (!found) {
            Edge e = new Edge(source_v, dest_v, cost);
            source_v.adj.add(e);
            dest_v.adjEntering.add(e);
            if (!isDirected) {
                e = new Edge(dest_v, source_v, cost);
                dest_v.adj.add(e);
                source_v.adjEntering.add(e);
            }
        }
    }

    private void addEdge(Vertex source, Vertex dest, int cost) {
        addEdge(source.name, dest.name, cost);
    }

    public void removeVertex(String vertexName) {
        removeEdges(vertexName);
        vertexMap.remove(vertexName);
    }

    public void removeEdges(Vertex vertex) {
        removeEdges(vertex.name);
    }

    public void removeEdge(String src, String dest) {
        Vertex v = vertexMap.get(src);
        int index = 0;
        for (Edge e : v.adj) {
            Vertex w = e.dest;
            int index2 = 0;
            if (w.name.equals(dest)) {
                for (Edge ew : w.adjEntering) {
                    if (ew.src.name.equals(src)) {
                        break;
                    }
                    index2++;
                }
                if (!w.adjEntering.isEmpty()) {
                    w.adjEntering.remove(index2);
                }
                break;
            }
            index++;
        }
        if (!v.adj.isEmpty()) {
            v.adj.remove(index);
        }
    }

    private void removeAllEdges() {
        for (Vertex v : vertexMap.values()) {
            removeEdges(v);
        }
    }

    public void makeCompelete() {
        boolean temp = isDirected;
        isDirected = true;

        removeAllEdges();
        for (Vertex v : vertexMap.values()) {
            for (Vertex u : vertexMap.values()) {
                if (!v.name.equals(u.name)) {
                    addEdge(v, u, 1);
                }
            }
        }
        isDirected = temp;
    }

    private void removeEdges(String vertexName) {

        Vertex v = vertexMap.get(vertexName);

        // v --> u, remove v from u's adjEntering list
        for (int i = 0; i < v.adj.size(); i++) {
            Vertex u = v.adj.get(i).dest;
            int index = 0;
            for (Edge e : u.adjEntering) {
                if (e.src.name.equals(v.name)) {
                    break;
                }
                index++;
            }
            u.adjEntering.remove(index);
        }

        // u --> v, remove v from u's adj list
        for (int i = 0; i < v.adjEntering.size(); i++) {
            Vertex u = v.adjEntering.get(i).src;
            int index = 0;
            for (Edge e : u.adj) {
                if (e.dest.name.equals(v.name)) {
                    break;
                }
                index++;
            }
            u.adj.remove(index);
        }

        v.adj.clear();
        v.adjEntering.clear();
    }

    @Override
    public Graph clone() {
        Graph clone = new Graph();
        clone.setDirected(true);
        for (Vertex v : vertexMap.values()) {
            for (Edge e : v.adj) {
                clone.addVertex(e.src.x, e.src.y, e.src.name);
                clone.addVertex(e.dest.x, e.dest.y, e.dest.name);
                clone.addEdge(e.src.name, e.dest.name, e.cost);
            }
            for (Edge e : v.adjEntering) {
                clone.addVertex(e.src.x, e.src.y, e.src.name);
                clone.addVertex(e.dest.x, e.dest.y, e.dest.name);
                clone.addEdge(e.src.name, e.dest.name, e.cost);
            }
        }
        return clone;
    }

    public void printGraph() {
        for (Vertex v : vertexMap.values()) {
            System.out.print(v.name + ": ");
            for (Edge e : v.adj) {
                System.out.println(e.toString());
            }
            for (Edge e : v.adjEntering) {
                System.out.println(e.toString());
            }
            System.out.println();
        }
    }

    public boolean isBipartite() {

        boolean isBipartite = true;

        for (Vertex v : vertexMap.values()) {
            if (!v.visited) {
                Queue<Vertex> q = new LinkedList<Vertex>();

                v.color = 1;
                v.visited = true;
                q.add(v);

                while (!q.isEmpty()) {

                    v = q.poll();
                    for (Edge e : v.adj) {
                        Vertex w = e.dest;
                        if (v.color == w.color) {
                            return false;
                        }
                        if (!w.visited) {
                            w.visited = true;
                            if (v.color == 1) {
                                w.color = 2;
                            } else {
                                w.color = 1;
                            }
                            q.add(w);
                        }
                    }

                }
            }
        }

        return isBipartite;

    }

    public static class Vertex implements Serializable, Comparable<Vertex> {

        private static final long serialVersionUID = 1L;
        public String name;
        public int x, y;
        public int color;
        public int dist;
        public boolean visited;
        public Vertex prev;
        public ArrayList<Edge> adj;
        public ArrayList<Edge> adjEntering;
        public Color vColor = Color.BLUE;
        public int disjointSetIndex;
        public Position pos;

        // A* specific variables
        public int f;
        public int g;

        public Vertex(String name) {
            this.name = name;
            adj = new ArrayList<Edge>();
            adjEntering = new ArrayList<Edge>();
            clear();
        }

        public void clear() {
            visited = false;
            prev = null;
            color = 0;
            vColor = Color.BLUE;
            dist = 999;
            disjointSetIndex = 0;
            pos = null;

            f = -1;
            g = Integer.MAX_VALUE;

            for (Edge e : adj) {
                e.clear();
            }
            for (Edge e : adjEntering) {
                e.clear();
            }
        }

        // Used in A* search
        public int heuristicDistanceTo(Vertex v) {
            // a very simple diagonal heuristic
            double xx = Math.abs(this.x - v.x) * Math.abs(this.x - v.x);
            double yy = Math.abs(this.y - v.y) * Math.abs(this.y - v.y);
            return (int) (Math.sqrt(xx + yy));
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(Vertex v) {
            return 0;
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (char c : name.toCharArray()) {
                h += (int) c;
            }
            return h;
        }

        @Override
        public boolean equals(Object v) {
            return ((Vertex) v).name.equalsIgnoreCase(name);
        }

        public static class AStarComparator implements Comparator<Vertex> {

            @Override
            public int compare(Vertex v1, Vertex v2) {
                if (v1.f > v2.f) {
                    return 1;
                }
                if (v1.f < v2.f) {
                    return -1;
                }
                return 0;
            }
        }

    }

    public static class SerializableVertex implements Serializable {

        private static final long serialVersionUID = 1L;
        public String name;
        public int x, y;
        public Edge[] adj;
        private int adj_c = 0;

        public SerializableVertex(int x, int y, String name, int adjSize) {
            this.x = x;
            this.y = y;
            this.name = name;
            adj = new Edge[adjSize];
        }

        public void addAdjEdge(String src, String dest, int cost) {
            Edge e = new Edge(new Vertex(src), new Vertex(dest), cost);
            adj[adj_c] = e;
            adj_c++;
        }

    }

    public static class Edge implements Serializable, Comparable<Edge> {

        private static final long serialVersionUID = 1L;
        public int cost, flow;
        public Vertex src, dest;
        public Color eColor = Color.BLUE;

        public Edge(Vertex src, Vertex dest, int cost) {
            this.src = src;
            this.dest = dest;
            this.cost = cost;
            clear();
        }

        public void clear() {
            flow = 0;
            eColor = Color.BLUE;
        }

        @Override
        public String toString() {
            return src.name + " -- " + flow + "/" + cost + "--> " + dest.name;
        }

        @Override
        public boolean equals(Object o) {
            Edge e = (Edge) o;

            String s1 = src.name;
            String d1 = dest.name;

            String s2 = e.src.name;
            String d2 = e.dest.name;

            if (s1.equals(s2) && d1.equals(d2)) {
                return true;
            }
            if (s1.equals(d2) && s2.equals(d1)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            char[] src_c = src.name.toCharArray();
            char[] dst_c = dest.name.toCharArray();
            int h = 0;
            for (char c : src_c) {
                h += (int) c;
            }
            for (char c : dst_c) {
                h += (int) c;
            }
            return h;
        }

        public int compareTo(Edge o) {
            return 1;
        }

        public static class EdgeComparator implements Comparator<Edge> {

            public int compare(Edge o1, Edge o2) {
                if (o1.cost > o2.cost) {
                    return 1;
                }
                if (o1.cost < o2.cost) {
                    return -1;
                }
                return 0;
            }
        }

    }

}
