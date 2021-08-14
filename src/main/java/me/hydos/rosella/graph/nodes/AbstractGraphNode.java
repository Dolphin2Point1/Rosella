package me.hydos.rosella.graph.nodes;

import me.hydos.rosella.graph.RenderGraph;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractGraphNode implements GraphNode {

    private static final AtomicLong nextID = new AtomicLong(1);

    /**
     * Creates a new globally unique node id.
     *
     * @return A globally unique node id
     */
    public static long createID() {
        return AbstractGraphNode.nextID.getAndIncrement();
    }

    /**
     * Value used to uniquely identify and compare nodes for datastructures.
     */
    public final long id = createID();

    /**
     * The graph that this node is attached to.
     */
    public final RenderGraph graph;

    /**
     * This function will not attach the node to the graph.
     *
     * @param graph The graph this node should be attached to.
     */
    protected AbstractGraphNode(@NotNull RenderGraph graph) {
        this.graph = graph;
    }

    @Override
    public final RenderGraph getGraph() {
        return this.graph;
    }

    @Override
    public void destroy() {
    }

    @Override
    public long getID() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id); // TODO faster?
    }
}
