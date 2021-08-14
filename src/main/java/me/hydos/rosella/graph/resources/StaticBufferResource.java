package me.hydos.rosella.graph.resources;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.rosella.graph.RenderGraph;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StaticBufferResource implements BufferResource {

    public Object data = null;
    private final RenderGraph graph;

    public final long bufferSize;

    private StaticBufferResource parent = null;
    private final List<StaticBufferResource> dependants = new ObjectArrayList<>();

    public StaticBufferResource(@NotNull RenderGraph graph, long bufferSize) {
        this.graph = graph;
        this.bufferSize = bufferSize;
    }

    public StaticBufferResource(@NotNull RenderGraph graph, @NotNull StaticBufferResource parent) {
        this.graph = graph;
        this.bufferSize = parent.bufferSize;

        this.parent = parent;
    }

    /**
     * Creates connections to the parent resource if a parent is set.
     */
    public void inject() {
        if(this.parent != null) {
            this.parent.dependants.add(this);
        }
    }

    public StaticBufferResource getParent() {
        return this.parent;
    }

    public List<StaticBufferResource> getDependants() {
        return this.dependants;
    }

    /**
     * Removes the link to the dependant resource. This is useful to eliminate connections to nodes that
     * are removed from the graph but care must be taken to ensure valid graph state.
     */
    public void erase() {
        if(this.parent != null) {
            this.parent.dependants.remove(this);
            this.parent = null;
        }
    }

    @Override
    public long getBufferSize() {
        return this.bufferSize;
    }

    @Override
    public RenderGraph getGraph() {
        return this.graph;
    }

    @Override
    public boolean isInSameGraph(Resource other) {
        RenderGraph a = this.getGraph();
        if(a == null) {
            return false;
        }

        RenderGraph b = other.getGraph();
        if(b == null) {
            return false;
        }

        return a == b;
    }
}
