package me.hydos.rosella.graph;

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
    protected final GraphInstance graph;

    /**
     *
     *
     * @param graph The graph that this node should attach to.
     */
    protected GraphNode(@NotNull RenderGraph graph) {
        this.graph = graph.addNode(this);
    }

    /**
     * Returns all resources that this node provides.
     *
     * This list must not change during the lifetime of the graph.
     *
     * @return All resources provided by this node.
     */
    public abstract Set<Resource> getProvidedResources();

    public abstract Set<ResourceDependency> getDependentResources();

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
