package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.IllegalGraphStateException;
import me.hydos.rosella.graph.nodes.GraphNode;

public class DependantImageResource extends ImageResource implements DependantResource {

    private ImageResource source;

    public DependantImageResource(GraphNode graphNode, ResourceAccess accessType) {
        super(graphNode);
    }

    /**
     * Sets the source of this dependency.
     *
     * This function should not be called directly by any user of the graph system as it bypasses any potential checks
     * performed by the GraphNode.
     *
     * @param newSource The new source
     */
    protected void setDependency(ImageResource newSource) {
        if(newSource != null && !isInSameGraph(newSource)) {
            throw new IllegalGraphStateException("Tried to depend on a resource in a different graph");
        }

        synchronized (this) {
            if(this.source != null) {
                this.source.removeDependency(this);
                this.source = null;
            }

            if(newSource != null) {
                newSource.addDependency(this);
                this.source = newSource;
            }
        }
    }

    @Override
    public void clearSource() {
        synchronized (this) {
            if(source != null) {
                source.removeDependency(this);
                source = null;
            }
        }
    }

    @Override
    public Resource getSource() {
        return source;
    }

    @Override
    public boolean isSatisfied() {
        synchronized (this) {
            return this.source != null;
        }
    }
}
