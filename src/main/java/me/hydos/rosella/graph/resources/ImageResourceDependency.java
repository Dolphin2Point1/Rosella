package me.hydos.rosella.graph.resources;

import me.hydos.rosella.graph.GraphNode;

public class ImageResourceDependency extends ResourceDependency {

    private ImageResource dependency;

    public ImageResourceDependency(GraphNode graphNode, ResourceAccess accessType, int stageMask, int accessMask) {
        super(graphNode, accessType, stageMask, accessMask);
    }

    @Override
    public boolean isSatisfied() {
        return false;
    }

    /**
     * Sets the source of this dependency.
     *
     * This function should not be called directly by any user of the graph system as it bypasses any potential checks
     * performed by the GraphNode.
     *
     * @param source The new source
     */
    protected void setDependency(ImageResource source) {
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
