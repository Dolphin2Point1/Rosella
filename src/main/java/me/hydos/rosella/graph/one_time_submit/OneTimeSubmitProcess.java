package me.hydos.rosella.graph.one_time_submit;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import me.hydos.rosella.graph.GraphEngine;
import me.hydos.rosella.graph.IllegalGraphStateException;
import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.nodes.GraphNode;
import me.hydos.rosella.graph.resources.DependantResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class OneTimeSubmitProcess implements Runnable {
    private final GraphEngine engine;
    private final RenderGraph graph;

    /**
     * A list of the NodeMeta instances representing the root of each disjoint union find tree
     */
    private List<NodeMeta> subgraphs = new ArrayList<>();

    private final CompletableFuture<Void> completedFuture;

    public OneTimeSubmitProcess(GraphEngine engine, RenderGraph graph, CompletableFuture<Void> completedFuture) {
        this.engine = engine;
        this.graph = graph;
        this.completedFuture = completedFuture;
    }

    @Override
    public void run() {
        try {
            this.graph.lock();

            preprocessGraph();

            completedFuture.complete(null);

        } catch (Exception ex) {
            engine.logger.error("Failed to compile and execute graph", ex);
            completedFuture.cancel(true);
            this.graph.destroy();

        } finally {
            this.graph.unlock();
        }
    }

    /**
     * Prepares the graph for compilation.
     * <p>
     * After this function returns all disjoint subraphs will have their union find root node in the
     * subgraphs list, all non dead nodes will have a initialized NodeMeta instance added and all
     * dead nodes will have been destroyed and their links removed from the graph.
     */
    private void preprocessGraph() {
        int iterationID = 0;
        for (GraphNode anchor : this.graph.getAnchors()) {
            anchor.otsInit(this);
            NodeMeta meta = new NodeMeta(anchor, iterationID++);

            initializeFromAnchor(anchor, meta);
        }

        for (GraphNode node : this.graph.getNodes()) {
            NodeMeta meta = (NodeMeta) node.meta;
            if (meta == null) {
                eliminateDeadNode(node);
            } else {
                if (node.getAllDependencies().isEmpty()) {
                    meta.getOrCreateHeads().add(node);
                }
            }
        }
    }

    private void initializeFromAnchor(GraphNode current, NodeMeta parent) {
        NodeMeta meta = (NodeMeta) current.meta;
        if (meta != null) {
            if (meta.iterationID == parent.iterationID) {
                throw new IllegalGraphStateException("Graph contains loops");
            }

            meta.unionMerge(parent);

        } else {
            current.otsInit(this);
            meta = new NodeMeta(current, parent, parent.iterationID);
            for (DependantResource dependency : current.getAllDependencies()) {
                if (!dependency.isSatisfied()) {
                    throw new IllegalGraphStateException("Unsatisfied dependency in non dead graph node");
                }

                initializeFromAnchor(dependency.getNode(), meta);
            }
        }
    }

    private void eliminateDeadNode(GraphNode node) {
        // Only need to remove links to non dead nodes, and there cannot be any downwards non dead node since then this node wouldn't be dead
        for (DependantResource dependency : node.getAllDependencies()) {
            if (dependency.isSatisfied()) {
                if (dependency.getSource().getNode().meta != null) {
                    dependency.clearSource();
                }
            }
        }

        node.destroy();
    }

    private class NodeMeta {
        public final GraphNode node;

        /**
         * The id of the anchor tree that discovered this node.
         * If a node with the same id is found while iterating through the dependency graph during initialization
         * then the graph contains a loop.
         */
        public final int iterationID;

        private NodeMeta unionParent = null;
        private int unionRank = 0;

        private Set<GraphNode> heads = null;

        public NodeMeta(GraphNode node, int iterationID) {
            this.node = node;
            node.meta = this;

            this.iterationID = iterationID;
        }

        public NodeMeta(GraphNode node, NodeMeta mergeWith, int iterationID) {
            this.node = node;
            node.meta = this;

            this.iterationID = iterationID;

            this.unionParent = mergeWith.findRoot();
        }

        public NodeMeta findRoot() {
            if (this.unionParent == null) {
                return this;
            }

            NodeMeta root = this.unionParent.findRoot();
            this.unionParent = root;
            return root;
        }

        public void unionMerge(NodeMeta other) {
            NodeMeta root = findRoot();
            NodeMeta otherRoot = other.findRoot();

            if (root.unionRank < otherRoot.unionRank) {
                root.unionParent = otherRoot;
                otherRoot.unionRank++;
            } else {
                otherRoot.unionParent = root;
                root.unionRank++;
            }
        }

        /**
         * Retrieves the head list for this subgraph.
         * If the the head list does not exist creates a one and adds this subgraph to the subgraphs list.
         * <p>
         * Must be called after all union operations have completed.
         *
         * @return The head list for this subgraph
         */
        public Set<GraphNode> getOrCreateHeads() {
            NodeMeta root = findRoot();
            if (root.heads == null) {
                root.heads = new ObjectArraySet<>();
                subgraphs.add(root);
            }

            return root.heads;
        }
    }
}
