package me.hydos.rosella.graph.nodes;

import me.hydos.rosella.graph.GraphInstance;
import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.resources.Resource;
import me.hydos.rosella.graph.resources.ResourceDependency;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A node is the basic building block of render graphs.
 * They represent operations that should be performed on resources.
 */
public abstract class GraphNode {

    /**
     * The graph that this node is attached to.
     */
    public final GraphInstance graph;

    /**
     *
     *
     * @param graph The graph that this node should attach to.
     */
    protected GraphNode(@NotNull RenderGraph graph) {
        this.graph = graph.addNode(this);
    }

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
    public abstract boolean isAnchor();
}
