package me.hydos.rosella.graph;

import me.hydos.rosella.graph.nodes.GraphNode;

/**
 *
 */
public abstract class RenderGraph {

    /**
     * Adds a node to the graph.
     * The node must not already be registered in any graph including this one.
     *
     * The node may not be fully initialized when this function is called. It is guaranteed that the graph only
     * queries the resource type.
     *
     * @param node The node to add
     * @return The graph instance that the node should use for graph operations
     */
    public abstract GraphInstance addNode(GraphNode node);
}
