package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.nodes.GraphNode;

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
     * The node that provides this resource
     */
    public final GraphNode sourceNode;

    /**
     * @param node The node that provides the resource
     */
    protected Resource(GraphNode node) {
        this.sourceNode = node;
    }

    /**
     * Returns true if this resources is the result of some operation on a different resource.
     *
     * @return True if this resource is derived.
     */
    public abstract boolean isDerived();

    /**
     * If this resource is derived returns the dependency that defines the source resource.
     * If this resource is not derived this function will return null.
     *
     * @return The source resource dependency or null if the resource is not derived.
     */
    public abstract ResourceDependency getSource();
}
