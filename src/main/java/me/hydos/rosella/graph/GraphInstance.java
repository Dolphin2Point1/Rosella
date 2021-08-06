package me.hydos.rosella.graph;

/**
 * Provides a common interface for graph nodes to perform graph global operations and validation.
 */
public interface GraphInstance {

    /**
     * Returns true if the graph is currently in the build stage.
     *
     * If this function returns false then the graph must not be modified in any way.
     * Nodes should use this function to ensure the application does not modify the graph
     * after the build phase has completed.
     *
     * @return True if the graph is currently in the build stage and can be modified.
     */
    boolean isBuilding();
}
