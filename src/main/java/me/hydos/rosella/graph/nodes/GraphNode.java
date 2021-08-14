package me.hydos.rosella.graph.nodes;

import me.hydos.rosella.graph.RenderGraph;

/**
 * A node is the basic building block of render graphs.
 * They represent operations that should be performed on resources.
 */
public interface GraphNode {

    RenderGraph getGraph();

    /**
     * Returns true if this node is a anchor node.
     *
     * Anchor nodes are nodes that will always be executed and serve as reference points to determine which nodes
     * outputs are unused. If no nodes in the output dependency tree of a non anchor node are anchor nodes that
     * node may be optimized away.
     *
     * In general this means anchor nodes are nodes whose operations have effects outside of the graph. For example
     * global resource writes, resource downloads or presentation.
     *
     * @return True if this node is a anchor
     */
    boolean isAnchor();

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
