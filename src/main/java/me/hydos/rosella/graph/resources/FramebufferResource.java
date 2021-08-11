package me.hydos.rosella.graph.resources;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import me.hydos.rosella.graph.nodes.GraphNode;

import java.util.Set;

/**
 *
 */
public class FramebufferResource extends Resource {

    protected final Set<FramebufferResourceDependency> dependants = new ObjectArraySet<>();

    public FramebufferResource(GraphNode node) {
        super(node);
    }

    /**
     * Adds a dependency to the list of dependant operations.
     *
     * Dependencies are managed by the dependant resource and all operations changing dependencies must be initiated
     * by the dependant resource. As such this function must only be called by other resources. Failure to comply
     * might result in invalid graph state.
     *
     * This function is fully thread safe.
     *
     * @param dependant The dependant to add
     */
    protected void addDependency(FramebufferResourceDependency dependant) {
        synchronized (this.dependants) {
            this.dependants.add(dependant);
        }
    }

    /**
     * Removes a dependency from the list of dependant operations.
     *
     * Dependencies are managed by the dependant resource and all operations changing dependencies must be initiated
     * by the dependant resource. As such this function must only be called by other resources. Failure to comply
     * might result in invalid graph state.
     *
     * This function is fully thread safe.
     *
     * @param dependant The dependant to remove
     */
    protected void removeDependency(FramebufferResourceDependency dependant) {
        synchronized (this.dependants) {
            this.dependants.remove(dependant);
        }
    }

    public Set<FramebufferResourceDependency> getDependencies() {
        return this.dependants;
    }
}
