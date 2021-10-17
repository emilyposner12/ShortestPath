
// Compute shortest paths in a weighted, directed graph
package spath;

import java.util.LinkedList;
import java.util.HashMap;

// heap-related structures
import heaps.Decreaser;
import heaps.MinHeap;

// directed graph structure
import spath.graphs.DirectedGraph;
import spath.graphs.Edge;
import spath.graphs.Vertex;

// vertex/dist pair for priority queue
import spath.VertexAndDist;

import timing.Ticker;


public class ShortestPaths {

    // "infinity" value for path lengths
    private final static Integer inf = Integer.MAX_VALUE;
    
    // a directed graph, and a weighting function on its edges
    private final DirectedGraph g;
    private HashMap<Edge, Integer> weights;	
    
    // starting vertex for shortest path computation
    private Vertex startVertex;
    
    // map from vertices to their handles into the priority queue
    private HashMap<Vertex, Decreaser<VertexAndDist>> handles;
    
    // map from vertices to their parent edges in the shortest-path tree
    private HashMap<Vertex, Edge> parentEdges;
    
    
 
    public ShortestPaths(DirectedGraph g, HashMap<Edge,Integer> weights, 
			 Vertex startVertex) {
    	this.g           = g;
    	this.weights     = weights;

    	this.startVertex = startVertex;	
	
    	this.handles     = new HashMap<Vertex, Decreaser<VertexAndDist>>();
    	this.parentEdges = new HashMap<Vertex, Edge>();
    }

    

    // Given a weighted digraph stored in g/weights, computes a
    // shortest-path tree of parent edges back to a given starting vertex.
    public void run() {
    	Ticker ticker = new Ticker(); // heap requires a ticker
	
    	MinHeap<VertexAndDist> pq = 
    			new MinHeap<VertexAndDist>(g.getNumVertices(), ticker);

    	// Put all vertices into the heap, infinitely far from start.
    	// Record handle to each inserted vertex, and initialize
    	// parent edge of each to null
    	for (Vertex v : g.vertices()) {
    		Decreaser<VertexAndDist> d = pq.insert(new VertexAndDist(v, inf));
    		handles.put(v, d);
    		parentEdges.put(v, null);
    	}

    	// Set the starting vertex's distance to 0, get the handle to the vertex from the heap, extract the vertex + distance object from the handle, 
      // create a new vertex + distance object with a reduced distance, and update the heap through the vertex's handle
    	Decreaser<VertexAndDist> startHandle = handles.get(startVertex);
    	VertexAndDist vd = startHandle.getValue();
    	startHandle.decrease(new VertexAndDist(vd.vertex, 0));
	
   
    while(pq.isEmpty() == false) {
    	VertexAndDist minimum = pq.extractMin();
    	for(Edge v: minimum.vertex.edgesFrom()) {
    		Vertex go = v.to;
    		Decreaser<VertexAndDist> dist = handles.get(go);
    		VertexAndDist p = dist.getValue();
    		int w = p.distance; //weight
    		
    		if(minimum.distance + weights.get(v) < w) {
    			w = minimum.distance + weights.get(v);
    			VertexAndDist newUnique = new VertexAndDist(go, w);
    			dist.decrease(newUnique);
    			parentEdges.put(v.to,v);
    		}
    	}
    }
    }

    // Computes a linked list containing every edge on a shortest path from the starting vertex (stored) to the ending vertex.
    public LinkedList<Edge> returnPath(Vertex endVertex) {
    	LinkedList<Edge> path = new LinkedList<Edge>();
    	while(endVertex.equals(startVertex) == false) {
  
    		path.add(0, parentEdges.get(endVertex));
    		endVertex = parentEdges.get(endVertex).from;
    		
    	}
    	return path;
    }


    // Computes the total weight of a putative shortest path from the start vertex to the specified end vertex.
    public int returnLength(Vertex endVertex) {
    	LinkedList<Edge> path = returnPath(endVertex);
	
    	int pathLength = 0;
    	for(Edge e : path) {
    		pathLength += weights.get(e);
    	}
	
    	return pathLength;
    }

    // Exposes the current-best distance estimate stored at a vertex.
    // Note: this is useful for comparing to ground-truth shortest-path distance in the absence of parent pointers
    public int returnLengthDirect(Vertex endVertex) {
    	Decreaser<VertexAndDist> endhandle = handles.get(endVertex);
    	return endhandle.getValue().distance;
    }
}
