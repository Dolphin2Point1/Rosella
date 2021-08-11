package me.hydos.rosella.graph.resources;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import me.hydos.rosella.graph.nodes.GraphNode;

import java.util.Set;

/**
 *
 */
public class BufferResource extends Resource {

    protected final BufferResourceDependency source;
    protected final Set<BufferResourceDependency> dependants = new ObjectArraySet<>();
    protected long bufferSize;

    public BufferResource(GraphNode node) {
        super(node);
        this.source = null;
    }

    public BufferResource(GraphNode node, BufferResourceDependency source) {
        super(node);
        this.source = source;
    }

    @Override
    public boolean isDerived() {
        return this.source != null;
    }

    @Override
    public BufferResourceDependency getSource() {
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
    protected void addDependency(BufferResourceDependency dependant) {
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
    protected void removeDependency(BufferResourceDependency dependant) {
        synchronized (this.dependants) {
            this.dependants.remove(dependant);
        }
    }
}
