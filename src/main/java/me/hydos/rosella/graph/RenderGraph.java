package me.hydos.rosella.graph;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.hydos.rosella.graph.nodes.GraphNode;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class RenderGraph {

    public final GraphEngine engine;
    public final Lock lock = new ReentrantLock();

    private final Set<GraphNode> nodes = new ObjectOpenHashSet<>();
    private final Set<GraphNode> anchors = new ObjectOpenHashSet<>();

    public RenderGraph(GraphEngine engine) {
        this.engine = engine;
    }

    /**
     * Adds a node to the graph.
     * The node must not already be registered in any graph including this one.
     *
     * After this function is called the node may be accessed asynchronously and at random so it must make sure
     * to have finished initialization before calling this function.
     *
     * @param node The node to add
     */
    public void addNode(GraphNode node) {
        try {
            lock();
            if(this.nodes.contains(node)) {
                throw new IllegalGraphStateException("Node is already in graph");
            }
            this.nodes.add(node);
            if(node.isAnchor()) {
                this.anchors.add(node);
            }
        } finally {
            unlock();
        }
    }

    /**
     * Removes a node from the graph.
     *
     * This will free all resources associated with the node and remove all dependency from and to the node.
     *
     * @param node The node to be removed
     */
    public void removeNode(GraphNode node) {
        try {
            lock();


        } finally {
            unlock();
        }
    }

    /**
     * Returns all anchors of this graph.
     *
     * Access to the returned object must be synchronized using the graph lock.
     *
     * @return All anchors in this graph
     */
    public Set<GraphNode> getAnchors() {
        return this.anchors;
    }

    /**
     * Returns all nodes of this graph.
     *
     * Access to the returned object must be synchronized using the graph lock.
     *
     * @return All nodes in this graph
     */
    public Set<GraphNode> getNodes() {
        return this.nodes;
    }

    public void destroy() {
        try {
            lock();

            for(GraphNode node : this.nodes) {
                node.destroy();
            }

            this.nodes.clear();
            this.anchors.clear();
        } finally  {
            unlock();
        }
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }
}
