package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.GraphNode;

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
}
