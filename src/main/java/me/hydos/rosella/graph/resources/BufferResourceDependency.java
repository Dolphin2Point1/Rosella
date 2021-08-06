package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.IllegalGraphStateException;
import me.hydos.rosella.graph.nodes.GraphNode;

public class BufferResourceDependency extends ResourceDependency {

    private BufferResource dependency = null;

    public BufferResourceDependency(GraphNode graphNode, ResourceAccess accessType, int stageMask, int accessMask) {
        super(graphNode, accessType, stageMask, accessMask);
    }

    @Override
    public boolean isSatisfied() {
        synchronized (this) {
            return dependency != null;
        }
    }

    /**
     * Sets the source of this dependency.
     *
     * This function should not be called directly by any user of the graph system as it bypasses any potential checks
     * performed by the GraphNode.
     *
     * @param source The new source
     */
    protected void setDependency(BufferResource source) {
        if(source != null && !isInSameGraph(source)) {
            throw new IllegalGraphStateException("Tried to depend on a resource in a different graph");
        }

        synchronized (this) {
            if(dependency != null) {
                dependency.removeDependency(this);
                dependency = null;
            }

            if(source != null) {
                source.addDependency(this);
                this.dependency = source;
            }
        }
    }
}
