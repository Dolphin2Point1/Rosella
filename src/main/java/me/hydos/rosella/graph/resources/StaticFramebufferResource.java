package me.hydos.rosella.graph.resources;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.rosella.graph.IllegalGraphStateException;
import me.hydos.rosella.graph.RenderGraph;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StaticFramebufferResource implements FramebufferResource {

    public Object data = null;
    private final RenderGraph graph;

    public final FramebufferSpec framebufferSpec;

    private StaticFramebufferResource parent = null;
    private StaticFramebufferResource dependant = null;

    public StaticFramebufferResource(@NotNull RenderGraph graph, FramebufferSpec spec) {
        this.graph = graph;
        this.framebufferSpec = spec;
    }

    public StaticFramebufferResource(@NotNull RenderGraph graph, @NotNull StaticFramebufferResource parent) {
        this.graph = graph;
        this.framebufferSpec = parent.framebufferSpec;

        this.parent = parent;
    }

    /**
     * Creates connections to the parent resource if a parent is set.
     */
    public void inject() {
        if(this.parent != null) {
            if(this.parent.dependant != null) {
                throw new IllegalGraphStateException("Tried to add multiple framebuffer dependencies");
            }

            this.parent.dependant = this;
        }
    }

    public StaticFramebufferResource getParent() {
        return this.parent;
    }

    public StaticFramebufferResource getDependant() {
        return this.dependant;
    }

    /**
     * Removes the link to the dependant resource. This is useful to eliminate connections to nodes that
     * are removed from the graph but care must be taken to ensure valid graph state.
     */
    public void erase() {
        if(this.parent != null) {
            this.parent.dependant = null;
            this.parent = null;
        }
    }

    @Override
    public FramebufferSpec getFramebufferSpec() {
        return this.framebufferSpec;
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
