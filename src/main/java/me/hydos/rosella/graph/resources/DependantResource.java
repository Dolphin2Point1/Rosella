package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.nodes.GraphNode;

/**
 *
 */
public interface DependantResource {

    /**
     * @return The node owning this resource.
     */
    GraphNode getNode();

    /**
     * @return True if a source has been set
     */
    boolean isSatisfied();

    /**
     * Removes any currently configured source.
     */
    void clearSource();

    /**
     * @return The current source.
     */
    Resource getSource();
}
