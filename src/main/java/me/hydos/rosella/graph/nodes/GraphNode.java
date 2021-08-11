package me.hydos.rosella.graph.nodes;

import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.one_time_submit.OneTimeSubmitNode;
import me.hydos.rosella.graph.resources.Resource;
import me.hydos.rosella.graph.resources.DependantResource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A node is the basic building block of render graphs.
 * They represent operations that should be performed on resources.
 */
public abstract class GraphNode implements OneTimeSubmitNode {

    private static final AtomicLong nextID = new AtomicLong(1);

    /**
     * Value used to uniquely identify and compare nodes for datastructures.
     */
    public final long id = nextID.getAndIncrement();

    /**
     * The graph that this node is attached to.
     */
    public final RenderGraph graph;

    /**
     * Used to store metadata for the graph compile process. Provided here to avoid unnecessary lookup in a table.
     * This is for the graph compile process and must not be touched by the application. It is expected that all
     * nodes submitted to a compile process have this value set to null.
     */
    public Object meta = null;

    /**
     * This constructor will not call {@link me.hydos.rosella.graph.RenderGraph#addNode(GraphNode)}.
     * The subclass must make sure to call {@link me.hydos.rosella.graph.RenderGraph#addNode(GraphNode)} unless it throws an
     * exception. Not failing with an exception and not attaching to the graph is considered invalid state.
     *
     * @see me.hydos.rosella.graph.RenderGraph#addNode(GraphNode)
     *
     * @param graph The graph that this node should attach to.
     */
    protected GraphNode(@NotNull RenderGraph graph) {
        this.graph = graph;
    }

    /**
     * Returns true if this node is a anchor node.
     *
     * Anchor nodes are nodes that will always be executed and serve as reference points to determine which nodes
     * outputs are unused. If no nodes in the output dependency tree of a non anchor node are anchor nodes that
     * node may be optimized away.
     *
     * In general this means anchor nodes are nodes whose operations have effects outside of the graph. For example
     * global resource writes, resource downloads or presentation.
     *
     * This function must always return the same value and must be ready to call when {@link me.hydos.rosella.graph.RenderGraph#addNode(GraphNode)}
     * is called.
     *
     * @return True if this node is a anchor
     */
    public boolean isAnchor() {
        return false;
    }

    /**
     * Frees any resources still owned by the node.
     *
     * This function should never be called directly by the application. It is the responsibility of the owning graph
     * to manage the lifetime of its nodes.
     *
     * Nodes must not raise errors if this function is called multiple times.
     */
    public void destroy() {
    }

    public abstract List<Resource> getAllResources();

    public abstract List<DependantResource> getAllDependencies();

    @Override
    public int hashCode() {
        return Objects.hash(this.id); // TODO faster?
    }

    public void otsInit() {
        throw new RuntimeException(this.getClass().getSimpleName() + " does not support the one time submit process");
    }
}
