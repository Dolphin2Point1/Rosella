package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.nodes.GraphNode;

/**
 *
 */
public abstract class ResourceDependency {

    public final GraphNode node;

    public final ResourceAccess accessType;
    public final int stageMask;
    public final int accessMask;

    protected ResourceDependency(GraphNode node, ResourceAccess accessType, int stageMask, int accessMask) {
        this.node = node;
        this.accessType = accessType;
        this.stageMask = stageMask;
        this.accessMask = accessMask;
    }

    /**
     * @return True if this dependency is satisfied.
     */
    public abstract boolean isSatisfied();

    /**
     * @param other The resource to compare to
     * @return True if the resource is part of the same graph as this dependency
     */
    public boolean isInSameGraph(Resource other) {
        return other.sourceNode.graph == this.node.graph;
    }
}
