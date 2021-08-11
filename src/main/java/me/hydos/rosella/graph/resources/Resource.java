package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.nodes.GraphNode;
import org.jetbrains.annotations.NotNull;

/**
 * Represents data passed between nodes in a graph.
 */
public abstract class Resource {

    /**
     * Used to store metadata for the graph compile process. Provided here to avoid unnecessary lookup in a table.
     * This is for the graph compile process and must not be touched by the application. It is expected that all
     * resources submitted to a compile process have this value set to null.
     */
    public Object compileMeta = null;

    /**
     * The node that provides this resource.
     */
    public final GraphNode node;

    /**
     * @return The node owning this resource.
     */
    public GraphNode getNode() {
        return this.node;
    }

    /**
     * @param node The node that provides the resource.
     */
    protected Resource(GraphNode node) {
        this.node = node;
    }


    /**
     * Tests if both resources are part of the same render graph.
     *
     * @param other The resource to compare to.
     * @return True if both resources are part of the same render graph.
     */
    public boolean isInSameGraph(@NotNull Resource other) {
        return this.node.graph == other.node.graph;
    }
}
