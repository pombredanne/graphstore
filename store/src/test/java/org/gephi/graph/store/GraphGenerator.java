package org.gephi.graph.store;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author mbastian
 */
public class GraphGenerator {
    
    public static EdgeImpl[] generateSmallEdgeList() {
        return generateEdgeList(100, 0, true, true);
    }
    
    public static EdgeImpl[] generateSmallUndirectedEdgeList() {
        return generateEdgeList(100, 0, false, true);
    }
    
    public static EdgeImpl[] generateSmallMixedEdgeList() {
        return generateMixedEdgeList(100, 0);
    }
    
    public static EdgeImpl[] generateSmallMultiTypeEdgeList() {
        return generateMultiTypeEdgeList(100, 3, true, true);
    }
    
    public static EdgeImpl[] generateSmallUndirectedMultiTypeEdgeList() {
        return generateMultiTypeEdgeList(100, 3, true, true);
    }
    
    public static EdgeImpl[] generateLargeEdgeList() {
        return generateEdgeList(EdgeStore.BLOCK_SIZE * 3 + (int) (EdgeStore.BLOCK_SIZE / 3.0), 0, true, true);
    }
    
    public static EdgeImpl[] generateLargeMultiTypeEdgeList() {
        return generateMultiTypeEdgeList(EdgeStore.BLOCK_SIZE * 3 + (int) (EdgeStore.BLOCK_SIZE / 3.0), 5, true, true);
    }
    
    public static EdgeImpl generateSingleEdge() {
        return generateEdgeList(1, 0, true, false)[0];
    }
    
    public static EdgeImpl generateSingleEdge(int type) {
        return generateEdgeList(1, type, true, false)[0];
    }
    
    public static EdgeImpl generateSelfLoop(int type, boolean directed) {
        NodeStore nodeStore = generateNodeStore(2);
        EdgeImpl edge = new EdgeImpl('0', nodeStore.get(0), nodeStore.get(0), type, directed);
        return edge;
    }
    
    public static EdgeImpl generateSingleUndirectedEdge() {
        return generateEdgeList(1, 0, false, false)[0];
    }
    
    public static EdgeImpl[] generateEdgeList(int edgeCount) {
        return generateEdgeList(edgeCount, 0, true, true);
    }
    
    public static EdgeImpl[] generateMixedEdgeList(int edgeCount) {
        return generateMixedEdgeList(edgeCount, 0);
    }
    
    public static EdgeImpl[] generateEdgeList(int edgeCount, int type, boolean directed, boolean allowSelfLoops) {
        int nodeCount = Math.max((int) Math.ceil(Math.sqrt(edgeCount * 2)), (int) (edgeCount / 10.0));
        return generateEdgeList(generateNodeStore(nodeCount), edgeCount, type, directed, allowSelfLoops);
    }
    
    public static EdgeImpl[] generateMixedEdgeList(int edgeCount, int type) {
        int nodeCount = Math.max((int) Math.ceil(Math.sqrt(edgeCount * 2)), (int) (edgeCount / 10.0));
        return generateMixedEdgeList(generateNodeStore(nodeCount), edgeCount, type);
    }
    
    public static EdgeImpl[] generateEdgeList(NodeStore nodeStore, int edgeCount, int type, boolean directed, boolean allowSelfLoops) {
        int nodeCount = nodeStore.size();
        final List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>();
        LongSet idSet = new LongOpenHashSet();
        Random r = new Random(124);
        
        IntSet leafs = new IntOpenHashSet();
        if (nodeCount > 10) {
            for (int i = 0; i < Math.min(10, (int) (nodeCount * .05)); i++) {
                int id = r.nextInt(nodeCount);
                if (leafs.contains(id)) {
                    i--;
                } else {
                    leafs.add(id);
                }
            }
        }
        
        int c = 0;
        while (idSet.size() < edgeCount) {
            int sourceId = r.nextInt(nodeCount);
            int targetId = r.nextInt(nodeCount);
            NodeImpl source = nodeStore.get(sourceId);
            NodeImpl target = nodeStore.get(targetId);
            EdgeImpl edge = new EdgeImpl(String.valueOf(c), source, target, type, directed);
            if (!leafs.contains(sourceId) && !leafs.contains(targetId) && (allowSelfLoops || (!allowSelfLoops && source != target)) && !idSet.contains(edge.getLongId())) {
                edgeList.add(edge);
                c++;
                idSet.add(edge.getLongId());
            }
        }
        return edgeList.toArray(new EdgeImpl[0]);
    }
    
    public static BasicGraphStore.BasicEdge[] generateBasicEdgeList(BasicGraphStore.BasicNodeStore nodeStore, int edgeCount, int type, boolean directed, boolean allowSelfLoops) {
        int nodeCount = nodeStore.size();
        final List<BasicGraphStore.BasicEdge> edgeList = new ArrayList<BasicGraphStore.BasicEdge>();
        ObjectSet<String> idSet = new ObjectOpenHashSet();
        Random r = new Random(124);
        
        IntSet leafs = new IntOpenHashSet();
        if (nodeCount > 10) {
            for (int i = 0; i < Math.min(10, (int) (nodeCount * .05)); i++) {
                int id = r.nextInt(nodeCount);
                if (leafs.contains(id)) {
                    i--;
                } else {
                    leafs.add(id);
                }
            }
        }
        
        int c = 0;
        while (idSet.size() < edgeCount) {
            int sourceId = r.nextInt(nodeCount);
            int targetId = r.nextInt(nodeCount);
            BasicGraphStore.BasicNode source = nodeStore.get(String.valueOf(sourceId));
            BasicGraphStore.BasicNode target = nodeStore.get(String.valueOf(targetId));
            BasicGraphStore.BasicEdge edge = new BasicGraphStore.BasicEdge(String.valueOf(c), source, target, type, directed);
            if (!leafs.contains(sourceId) && !leafs.contains(targetId) && (allowSelfLoops || (!allowSelfLoops && source != target)) && !idSet.contains(edge.getStringId())) {
                edgeList.add(edge);
                c++;
                idSet.add(edge.getStringId());
            }
        }
        return edgeList.toArray(new BasicGraphStore.BasicEdge[0]);
    }
    
    public static EdgeImpl[] generateMixedEdgeList(NodeStore nodeStore, int edgeCount, int type) {
        int nodeCount = nodeStore.size();
        final List<EdgeImpl> edgeList = new ArrayList<EdgeImpl>();
        LongSet idSet = new LongOpenHashSet();
        Random r = new Random(124);
        int c = 0;
        while (idSet.size() < edgeCount / 2) {
            int sourceId = r.nextInt(nodeCount);
            int targetId = r.nextInt(nodeCount);
            NodeImpl source = nodeStore.get(sourceId);
            NodeImpl target = nodeStore.get(targetId);
            EdgeImpl edge = new EdgeImpl(String.valueOf(c), source, target, type, true);
            if (source != target && !idSet.contains(edge.getLongId())) {
                edgeList.add(edge);
                c++;
                idSet.add(edge.getLongId());
            }
        }
        while (idSet.size() < edgeCount) {
            int sourceId = r.nextInt(nodeCount);
            int targetId = r.nextInt(nodeCount);
            NodeImpl source = nodeStore.get(sourceId);
            NodeImpl target = nodeStore.get(targetId);
            EdgeImpl edge = new EdgeImpl(String.valueOf(c), source, target, type, false);
            if (source != target && !idSet.contains(EdgeStore.getLongId(edge.source, edge.target, true)) && !idSet.contains(EdgeStore.getLongId(edge.target, edge.source, true))) {
                edgeList.add(edge);
                c++;
                idSet.add(edge.getLongId());
            }
        }
        return edgeList.toArray(new EdgeImpl[0]);
    }
    
    public static EdgeImpl[] generateMultiTypeEdgeList(int edgeCount, int typeCount, boolean directed, boolean allowSelfLoops) {
        List<EdgeImpl> edges = new ArrayList<EdgeImpl>();
        int nodeCount = Math.max((int) Math.ceil(Math.sqrt(edgeCount * 2)), (int) (edgeCount / 10.0));
        NodeStore nodeStore = generateNodeStore(nodeCount);
        int[] typeAssignemnts = distributeTypeCounts(typeCount, edgeCount);
        for (int i = 0; i < typeCount; i++) {
            edges.addAll(Arrays.asList(generateEdgeList(nodeStore, typeAssignemnts[i], i, directed, allowSelfLoops)));
        }
        Collections.shuffle(edges, new Random(87));
        return edges.toArray(new EdgeImpl[0]);
    }
    
    public static BasicGraphStore.BasicEdge[] generateBasicMultiTypeEdgeList(int edgeCount, int typeCount, boolean directed, boolean allowSelfLoops) {
        List<BasicGraphStore.BasicEdge> edges = new ArrayList<BasicGraphStore.BasicEdge>();
        int nodeCount = Math.max((int) Math.ceil(Math.sqrt(edgeCount * 2)), (int) (edgeCount / 10.0));
        BasicGraphStore.BasicNodeStore nodeStore = generateBasicNodeStore(nodeCount);
        int[] typeAssignemnts = distributeTypeCounts(typeCount, edgeCount);
        for (int i = 0; i < typeCount; i++) {
            edges.addAll(Arrays.asList(generateBasicEdgeList(nodeStore, typeAssignemnts[i], i, directed, allowSelfLoops)));
        }
        Collections.shuffle(edges, new Random(87));
        return edges.toArray(new BasicGraphStore.BasicEdge[0]);
    }
    
    public static int[] distributeTypeCounts(int typeCount, int edgeCount) {
        double sum = 0;
        double[] ratio = new double[typeCount];
        Random r = new Random(453);
        for (int i = 0; i < typeCount;) {
            double n = r.nextDouble();
            if (n != 0) {
                ratio[i] = n;
                sum += n;
                i++;
            }
        }
        int[] res = new int[typeCount];
        int total = 0;
        for (int i = 0; i < typeCount; i++) {
            if (i == typeCount - 1) {
                res[i] = edgeCount - total;
                assert res[i] > 0;
            } else {
                res[i] = (int) (ratio[i] / sum);
                total += res[i];
            }
        }
        return res;
    }
    
    public static NodeStore generateNodeStore(int nodeCount) {
        final NodeStore nodeStore = new NodeStore();
        
        for (int i = 0; i < nodeCount; i++) {
            NodeImpl n = new NodeImpl(String.valueOf(i));
            nodeStore.add(n);
        }
        return nodeStore;
    }
    
    public static NodeStore generateLargeNodeStore() {
        return generateNodeStore(1000);
    }
    
    public static BasicGraphStore.BasicNodeStore generateBasicNodeStore(int nodeCount) {
        final BasicGraphStore.BasicNodeStore nodeStore = new BasicGraphStore.BasicNodeStore();
        
        for (int i = 0; i < nodeCount; i++) {
            BasicGraphStore.BasicNode n = new BasicGraphStore.BasicNode(String.valueOf(i));
            nodeStore.add(n);
        }
        return nodeStore;
    }
    
    public static BasicGraphStore.BasicNodeStore generateLargeBasicNodeStore() {
        return generateBasicNodeStore(1000);
    }
    
    public static NodeImpl[] generateSmallNodeList() {
        return generateNodeList(100);
    }
    
    public static NodeImpl[] generateLargeNodeList() {
        return generateNodeList(NodeStore.BLOCK_SIZE * 3 + (int) (NodeStore.BLOCK_SIZE / 3.0));
    }
    
    public static BasicGraphStore.BasicNode[] generateLargeBasicNodeList() {
        return generateBasicNodeList(NodeStore.BLOCK_SIZE * 3 + (int) (NodeStore.BLOCK_SIZE / 3.0));
    }
    
    public static NodeImpl[] generateNodeList(int nodeCount) {
        NodeImpl[] nodes = new NodeImpl[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            NodeImpl node = new NodeImpl(String.valueOf(i));
            nodes[i] = node;
        }
        return nodes;
    }
    
    public static BasicGraphStore.BasicNode[] generateBasicNodeList(int nodeCount) {
        BasicGraphStore.BasicNode[] nodes = new BasicGraphStore.BasicNode[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            BasicGraphStore.BasicNode node = new BasicGraphStore.BasicNode(String.valueOf(i));
            nodes[i] = node;
        }
        return nodes;
    }
}
