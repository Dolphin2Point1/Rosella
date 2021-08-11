package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.nodes.GraphNode;

/**
 *
 */
public abstract class ResourceDependency {

    public final GraphNode node;

    public final ResourceAccess accessType;

    protected ResourceDependency(GraphNode node, ResourceAccess accessType) {
        this.node = node;
        this.accessType = accessType;
    }

    public abstract void reset();

    public abstract Resource getDependency();

    /**
     * @return True if this dependency is satisfied.
     */
    public abstract boolean isSatisfied();

    /**
     * @param other The resource to compare to
     * @return True if the resource is not part of the same graph as this dependency
     */
    public boolean isInDifferentGraph(Resource other) {
        return other.sourceNode.graph != this.node.graph;
    }
}
