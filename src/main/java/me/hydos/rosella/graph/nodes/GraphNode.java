package me.hydos.rosella.graph.nodes;

import me.hydos.rosella.graph.RenderGraph;

/**
 * A node is the basic building block of render graphs.
 * They represent operations that should be performed on resources.
 */
public interface GraphNode {

    RenderGraph getGraph();

    /**
     * Frees any resources still owned by the node.
     *
     * This function should never be called directly by the application. It is the responsibility of the owning graph
     * to manage the lifetime of its nodes.
     *
     * Nodes must not raise errors if this function is called multiple times.
     */
    void destroy();

    /**
     * A globally unique id to identify this node. The id should be acquired using the id generator of the
     * {@link me.hydos.rosella.graph.nodes.AbstractGraphNode} class or must otherwise ensure global uniqueness.
     *
     * @return A globally unique id for this node.
     */
    long getID();
}
