package me.hydos.rosella.graph.resources;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.rosella.graph.RenderGraph;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StaticImageResource implements ImageResource {

    public Object data;
    private final RenderGraph graph;

    public final ImageSpec imageSpec;

    private StaticImageResource parent = null;
    private final List<StaticImageResource> dependants = new ObjectArrayList<>();

    public StaticImageResource(@NotNull RenderGraph graph, ImageSpec imageSpec) {
        this.graph = graph;
        this.imageSpec = imageSpec;
    }

    public StaticImageResource(@NotNull RenderGraph graph, @NotNull StaticImageResource parent) {
        this.graph = graph;
        this.imageSpec = parent.imageSpec;

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

    public StaticImageResource getParent() {
        return this.parent;
    }

    public List<StaticImageResource> getDependants() {
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
    public ImageSpec getImageSpec() {
        return null;
    }

    @Override
    public RenderGraph getGraph() {
        return null;
    }

    @Override
    public boolean isInSameGraph(Resource other) {
        return false;
    }
}
