package me.hydos.rosella.graph.resources;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import me.hydos.rosella.graph.GraphNode;

import java.util.Set;

/**
 *
 */
public class FramebufferResource extends Resource {

    public final FramebufferSpec spec;

    protected final FramebufferResourceDependency source;
    protected final Set<FramebufferResourceDependency> dependants = new ObjectArraySet<>();

    public FramebufferResource(FramebufferSpec spec, GraphNode node) {
        super(node);
        this.spec = spec;
        this.source = null;
    }

    public FramebufferResource(FramebufferSpec spec, GraphNode node, FramebufferResourceDependency source) {
        super(node);
        this.spec = spec;
        this.source = source;
    }

    @Override
    public boolean isDerived() {
        return this.source != null;
    }

    @Override
    public ResourceDependency getSource() {
        return this.source;
    }

    /**
     * Adds a dependency to the list of dependant operations.
     *
     * Dependencies are managed by the {@link me.hydos.rosella.graph.resources.ResourceDependency} class and all
     * operations changing dependencies must be initiated by calling a ResourceDependency function. As such this
     * function must only be called inside the ResourceDependency class. Failure to comply might result in invalid
     * graph state.
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
     * Dependencies are managed by the {@link me.hydos.rosella.graph.resources.ResourceDependency} class and all
     * operations changing dependencies must be initiated by calling a ResourceDependency function. As such this
     * function must only be called inside the ResourceDependency class. Failure to comply might result in invalid
     * graph state.
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
}
