package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.IllegalGraphStateException;
import me.hydos.rosella.graph.nodes.GraphNode;

public class BufferResourceDependency extends ResourceDependency {

    private BufferResource dependency = null;

    public BufferResourceDependency(GraphNode graphNode, ResourceAccess accessType) {
        super(graphNode, accessType);
    }

    @Override
    public void reset() {
        synchronized (this) {
            if(this.dependency != null) {
                this.dependency.removeDependency(this);
                this.dependency = null;
            }
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
    public void setDependency(BufferResource source) {
        if(source != null && isInDifferentGraph(source)) {
            throw new IllegalGraphStateException("Tried to depend on a resource in a different graph");
        }

        synchronized (this) {
            if(this.dependency != null) {
                this.dependency.removeDependency(this);
                this.dependency = null;
            }

            if(source != null) {
                source.addDependency(this);
                this.dependency = source;
            }
        }
    }

    @Override
    public BufferResource getDependency() {
        return this.dependency;
    }

    @Override
    public boolean isSatisfied() {
        synchronized (this) {
            return dependency != null;
        }
    }
}
