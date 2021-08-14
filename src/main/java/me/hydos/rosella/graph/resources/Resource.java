package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.RenderGraph;

/**
 * Represents data passed between nodes in a graph.
 */
public interface Resource {

    /**
     * @return The graph that this resource is a part of
     */
    RenderGraph getGraph();

    /**
     * @param other Another resource
     * @return True if the other resource is part of the same graph as this resource
     */
    boolean isInSameGraph(Resource other);
}
