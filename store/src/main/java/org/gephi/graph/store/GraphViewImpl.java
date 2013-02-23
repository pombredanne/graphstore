package org.gephi.graph.store;

import cern.colt.bitvector.BitVector;
import cern.colt.bitvector.QuickBitVector;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedSubgraph;
import org.gephi.graph.store.EdgeStore.EdgeInOutIterator;

/**
 *
 * @author mbastian
 */
public final class GraphViewImpl implements GraphView {

    //Const
    public static final int DEFAULT_TYPE_COUNT = 1;
    public static final double GROWING_FACTOR = 1.1;
    //Data
    protected final GraphStore graphStore;
    protected BitVector nodeBitVector;
    protected BitVector edgeBitVector;
    protected int storeId;
    //Decorators
    private final GraphViewDecorator directedDecorator;
    private final GraphViewDecorator undirectedDecorator;
    //Stats
    protected int nodeCount;
    protected int edgeCount;
    protected int[] typeCounts;
    protected int[] mutualEdgeTypeCounts;
    protected int mutualEdgesCount;

    public GraphViewImpl(final GraphStore store) {
        this.graphStore = store;
        this.nodeCount = 0;
        this.edgeCount = 0;
        this.nodeBitVector = new BitVector(store.nodeStore.maxStoreId());
        this.edgeBitVector = new BitVector(store.edgeStore.maxStoreId());
        this.typeCounts = new int[DEFAULT_TYPE_COUNT];
        this.mutualEdgeTypeCounts = new int[DEFAULT_TYPE_COUNT];
        this.directedDecorator = new GraphViewDecorator(graphStore, this, false);
        this.undirectedDecorator = new GraphViewDecorator(graphStore, this, true);
    }

    public GraphViewImpl(final GraphViewImpl view) {
        this.graphStore = view.graphStore;
        this.nodeCount = view.nodeCount;
        this.edgeCount = view.edgeCount;
        this.nodeBitVector = view.nodeBitVector.copy();
        this.edgeBitVector = view.edgeBitVector.copy();
        this.typeCounts = new int[view.typeCounts.length];
        System.arraycopy(view.typeCounts, 0, typeCounts, 0, view.typeCounts.length);
        this.mutualEdgeTypeCounts = new int[view.mutualEdgeTypeCounts.length];
        System.arraycopy(view.mutualEdgeTypeCounts, 0, mutualEdgeTypeCounts, 0, view.mutualEdgeTypeCounts.length);
        this.directedDecorator = new GraphViewDecorator(graphStore, this, false);
        this.undirectedDecorator = new GraphViewDecorator(graphStore, this, true);
    }

    protected DirectedSubgraph getDirectedGraph() {
        return directedDecorator;
    }

    protected UndirectedSubgraph getUndirectedGraph() {
        return undirectedDecorator;
    }

    public boolean addNode(final Node node) {
        NodeImpl nodeImpl = (NodeImpl) node;
        graphStore.nodeStore.checkNodeExists(nodeImpl);

        int id = nodeImpl.storeId;
        boolean isSet = nodeBitVector.get(id);
        if (!isSet) {
            nodeBitVector.set(id);
            nodeCount++;
            return true;
        }
        return false;
    }

    public boolean addAllNodes(final Collection<? extends Node> nodes) {
        if (!nodes.isEmpty()) {
            Iterator<? extends Node> nodeItr = nodes.iterator();
            boolean changed = false;
            while (nodeItr.hasNext()) {
                Node node = nodeItr.next();
                checkValidNodeObject(node);
                if (addNode(node)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean addEdge(final Edge edge) {
        EdgeImpl edgeImpl = (EdgeImpl) edge;
        graphStore.edgeStore.checkEdgeExists(edgeImpl);

        int id = edgeImpl.storeId;
        boolean isSet = edgeBitVector.get(id);
        if (!isSet) {
            checkIncidentNodesExists(edgeImpl);

            edgeBitVector.set(id);
            edgeCount++;

            int type = edgeImpl.type;
            ensureTypeCountArrayCapacity(type);

            typeCounts[type]++;

            if (edgeImpl.isMutual() && edgeImpl.source.storeId < edgeImpl.target.storeId) {
                mutualEdgeTypeCounts[type]++;
                mutualEdgesCount++;
            }
            return true;
        }
        return false;
    }

    public boolean addAllEdges(final Collection<? extends Edge> edges) {
        if (!edges.isEmpty()) {
            Iterator<? extends Edge> edgeItr = edges.iterator();
            boolean changed = false;
            while (edgeItr.hasNext()) {
                Edge edge = edgeItr.next();
                checkValidEdgeObject(edge);
                if (addEdge(edge)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean removeNode(final Node node) {
        NodeImpl nodeImpl = (NodeImpl) node;
        graphStore.nodeStore.checkNodeExists(nodeImpl);

        int id = nodeImpl.storeId;
        boolean isSet = nodeBitVector.get(id);
        if (isSet) {
            nodeBitVector.clear(id);
            nodeCount--;

            //Remove edges
            EdgeInOutIterator itr = graphStore.edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                EdgeImpl edgeImpl = itr.next();

                int edgeId = edgeImpl.storeId;
                boolean edgeSet = edgeBitVector.get(edgeId);
                if (edgeSet) {
                    edgeBitVector.clear(edgeId);
                    edgeCount--;
                    typeCounts[edgeImpl.type]--;

                    if (edgeImpl.isMutual() && edgeImpl.source.storeId < edgeImpl.target.storeId) {
                        mutualEdgeTypeCounts[edgeImpl.type]--;
                        mutualEdgesCount--;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean removeNodeAll(final Collection<? extends Node> nodes) {
        if (!nodes.isEmpty()) {
            Iterator<? extends Node> nodeItr = nodes.iterator();
            boolean changed = false;
            while (nodeItr.hasNext()) {
                Node node = nodeItr.next();
                checkValidNodeObject(node);
                if (removeNode(node)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean removeEdge(final Edge edge) {
        EdgeImpl edgeImpl = (EdgeImpl) edge;
        graphStore.edgeStore.checkEdgeExists(edgeImpl);

        int id = edgeImpl.storeId;
        boolean isSet = edgeBitVector.get(id);
        if (isSet) {
            edgeBitVector.clear(id);
            edgeCount--;
            typeCounts[edgeImpl.type]--;

            if (edgeImpl.isMutual() && edgeImpl.source.storeId < edgeImpl.target.storeId) {
                mutualEdgeTypeCounts[edgeImpl.type]--;
                mutualEdgesCount--;
            }
            return true;
        }
        return false;
    }

    public boolean removeEdgeAll(final Collection<? extends Edge> edges) {
        if (!edges.isEmpty()) {
            Iterator<? extends Edge> edgeItr = edges.iterator();
            boolean changed = false;
            while (edgeItr.hasNext()) {
                Edge edge = edgeItr.next();
                checkValidEdgeObject(edge);
                if (removeEdge(edge)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public void clear() {
        nodeBitVector.clear();
        edgeBitVector.clear();
        nodeCount = 0;
        edgeCount = 0;
        typeCounts = new int[DEFAULT_TYPE_COUNT];
        mutualEdgeTypeCounts = new int[DEFAULT_TYPE_COUNT];
        mutualEdgesCount = 0;
    }

    public void clearEdges() {
        edgeBitVector.clear();
        edgeCount = 0;
        typeCounts = new int[DEFAULT_TYPE_COUNT];
        mutualEdgeTypeCounts = new int[DEFAULT_TYPE_COUNT];
        mutualEdgesCount = 0;
    }

    public boolean containsNode(final NodeImpl node) {
        return nodeBitVector.get(node.storeId);
    }

    public boolean containsEdge(final EdgeImpl edge) {
        return edgeBitVector.get(edge.storeId);
    }

    public void intersection(final GraphViewImpl otherView) {
        BitVector nodeOtherBitVector = otherView.nodeBitVector;
        BitVector edgeOtherBitVector = otherView.edgeBitVector;

        int nodeSize = nodeBitVector.size();
        for (int i = 0; i < nodeSize; i++) {
            boolean t = nodeBitVector.get(i);
            boolean o = nodeOtherBitVector.get(i);
            if (t && !o) {
                removeNode(getNode(i));
            }
        }

        int edgeSize = nodeBitVector.size();
        for (int i = 0; i < edgeSize; i++) {
            boolean t = edgeBitVector.get(i);
            boolean o = edgeOtherBitVector.get(i);
            if (t && !o) {
                removeEdge(getEdge(i));
            }
        }
    }

    public void union(final GraphViewImpl otherView) {
        BitVector nodeOtherBitVector = otherView.nodeBitVector;
        BitVector edgeOtherBitVector = otherView.edgeBitVector;

        int nodeSize = nodeBitVector.size();
        for (int i = 0; i < nodeSize; i++) {
            boolean t = nodeBitVector.get(i);
            boolean o = nodeOtherBitVector.get(i);
            if (!t && o) {
                addNode(getNode(i));
            }
        }

        int edgeSize = nodeBitVector.size();
        for (int i = 0; i < edgeSize; i++) {
            boolean t = edgeBitVector.get(i);
            boolean o = edgeOtherBitVector.get(i);
            if (!t && o) {
                addEdge(getEdge(i));
            }
        }
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getUndirectedEdgeCount() {
        return edgeCount - mutualEdgesCount;
    }

    public int getEdgeCount(int type) {
        if (type < 0 || type >= typeCounts.length) {
            throw new IllegalArgumentException("Incorrect type=" + type);
        }
        return typeCounts[type];
    }

    public int getUndirectedEdgeCount(int type) {
        if (type < 0 || type >= typeCounts.length) {
            throw new IllegalArgumentException("Incorrect type=" + type);
        }
        return typeCounts[type] - mutualEdgeTypeCounts[type];
    }

    public void ensureNodeVectorSize(NodeImpl node) {
        int sid = node.storeId;
        if (sid >= nodeBitVector.size()) {
            int newSize = Math.min(Math.max(sid + 1, (int) (sid * GROWING_FACTOR)), Integer.MAX_VALUE);
            nodeBitVector = growBitVector(nodeBitVector, newSize);
        }
    }

    public void ensureEdgeVectorSize(EdgeImpl edge) {
        int sid = edge.storeId;
        if (sid >= edgeBitVector.size()) {
            int newSize = Math.min(Math.max(sid + 1, (int) (sid * GROWING_FACTOR)), Integer.MAX_VALUE);
            edgeBitVector = growBitVector(edgeBitVector, newSize);
        }
    }

    @Override
    public GraphModelImpl getGraphModel() {
        return graphStore.graphModel;
    }

    @Override
    public boolean isMainView() {
        return false;
    }

    private BitVector growBitVector(BitVector bitVector, int size) {
        long[] elements = bitVector.elements();
        long[] newElements = QuickBitVector.makeBitVector(size, 1);
        System.arraycopy(elements, 0, newElements, 0, elements.length);
        return new BitVector(newElements, size);
    }

    private NodeImpl getNode(int id) {
        return graphStore.nodeStore.get(id);
    }

    private EdgeImpl getEdge(int id) {
        return graphStore.edgeStore.get(id);
    }

    private void ensureTypeCountArrayCapacity(int type) {
        if (type >= typeCounts.length) {
            int[] newArray = new int[type + 1];
            System.arraycopy(typeCounts, 0, newArray, 0, typeCounts.length);
            typeCounts = newArray;

            int[] newMutualArray = new int[type + 1];
            System.arraycopy(mutualEdgeTypeCounts, 0, newMutualArray, 0, mutualEdgeTypeCounts.length);
            mutualEdgeTypeCounts = newMutualArray;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.nodeBitVector != null ? this.nodeBitVector.hashCode() : 0);
        hash = 11 * hash + (this.edgeBitVector != null ? this.edgeBitVector.hashCode() : 0);
        hash = 11 * hash + this.nodeCount;
        hash = 11 * hash + this.edgeCount;
        hash = 11 * hash + Arrays.hashCode(this.typeCounts);
        hash = 11 * hash + Arrays.hashCode(this.mutualEdgeTypeCounts);
        hash = 11 * hash + this.mutualEdgesCount;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GraphViewImpl other = (GraphViewImpl) obj;
        if (this.nodeBitVector != other.nodeBitVector && (this.nodeBitVector == null || !this.nodeBitVector.equals(other.nodeBitVector))) {
            return false;
        }
        if (this.edgeBitVector != other.edgeBitVector && (this.edgeBitVector == null || !this.edgeBitVector.equals(other.edgeBitVector))) {
            return false;
        }
        if (this.nodeCount != other.nodeCount) {
            return false;
        }
        if (this.edgeCount != other.edgeCount) {
            return false;
        }
        if (!Arrays.equals(this.typeCounts, other.typeCounts)) {
            return false;
        }
        if (!Arrays.equals(this.mutualEdgeTypeCounts, other.mutualEdgeTypeCounts)) {
            return false;
        }
        if (this.mutualEdgesCount != other.mutualEdgesCount) {
            return false;
        }
        return true;
    }

    private void checkIncidentNodesExists(final EdgeImpl e) {
        if (!nodeBitVector.get(e.source.storeId) || !nodeBitVector.get(e.target.storeId)) {
            throw new RuntimeException("Both source and target nodes need to be in the view");
        }
    }

    private void checkValidEdgeObject(final Edge n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (!(n instanceof EdgeImpl)) {
            throw new ClassCastException("Object must be a EdgeImpl object");
        }
        if (((EdgeImpl) n).storeId == EdgeStore.NULL_ID) {
            throw new IllegalArgumentException("Edge should belong to a store");
        }
    }

    private void checkValidNodeObject(final Node n) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (!(n instanceof NodeImpl)) {
            throw new ClassCastException("Object must be a NodeImpl object");
        }
        if (((NodeImpl) n).storeId == NodeStore.NULL_ID) {
            throw new IllegalArgumentException("Node should belong to a store");
        }
    }
}
