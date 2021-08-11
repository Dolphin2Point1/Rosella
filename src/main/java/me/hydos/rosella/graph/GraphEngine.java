package me.hydos.rosella.graph;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import me.hydos.rosella.Rosella;
import me.hydos.rosella.graph.nodes.GraphNode;
import me.hydos.rosella.graph.resources.ResourceDependency;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class GraphEngine {

    public final Rosella rosella;
    public final Logger logger;

    private final ExecutorService mainWorkers = Executors.newSingleThreadExecutor();

    public GraphEngine(Rosella rosella) {
        this.rosella = rosella;
        this.logger = rosella.logger;

        this.logger.debug("Initializing graph engine");

        this.logger.debug("Graph engine initialization complete");
    }

    public void destroy() {
        this.logger.debug("Destroying graph engine");

        this.mainWorkers.shutdown();
        try {
            this.logger.trace("Waiting for shutdown of graph engine workers");
            while(!mainWorkers.awaitTermination(1, TimeUnit.MINUTES)) {
                this.logger.warn("Reached 1min timeout while waiting for graph engine workers shutdown");
            }
        } catch (InterruptedException e) {
            this.logger.error("Interrupted while waiting for graph engine workers shutdown", e);
        }
    }

    public RenderGraph createGraph() {
        return new RenderGraph(this);
    }

    /**
     * Executes the provided graph.
     *
     * The provided graph will have its ownership transferred to the GraphEngine and the application must not call any
     * function or otherwise modify the graph or its nodes in any way. Destruction of the graph and nodes will be
     * managed by the GraphEngine.
     *
     * Global resource have sequentially consistent semantics. It is guaranteed that any execute call made after
     * the current call returns will see all changes in global state made by the current execution and all previous
     * executions.
     *
     * @param graph The graph to execute
     */
    public Future<Void> execute(RenderGraph graph) {
        if(graph.engine != this) {
            throw new RuntimeException("Tried to execute graph on foreign engine");
        }

        try {
            graph.lock();

            // TODO lock global resources

        } finally {
            graph.unlock();
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        mainWorkers.execute(new CompileAndExecute(graph, future));
        return future;
    }

    private class CompileAndExecute implements Runnable {
        private final RenderGraph graph;

        /**
         * A list of the NodeMeta instances representing the root of each disjoint union find tree
         */
        private List<NodeMeta> subgraphs = new ArrayList<>();

        private final CompletableFuture<Void> completedFuture;

        public CompileAndExecute(RenderGraph graph, CompletableFuture<Void> completedFuture) {
            this.graph = graph;
            this.completedFuture = completedFuture;
        }

        @Override
        public void run() {
            try {
                this.graph.lock();

                preprocessGraph();

                completedFuture.complete(null);

            } catch(Exception ex) {
                logger.error("Failed to compile and execute graph", ex);
                completedFuture.cancel(true);
                this.graph.destroy();

            } finally {
                this.graph.unlock();
            }
        }

        /**
         * Prepares the graph for compilation.
         *
         * After this function returns all disjoint subraphs will have their union find root node in the
         * subgraphs list, all non dead nodes will have a initialized NodeMeta instance added and all
         * dead nodes will have been destroyed and their links removed from the graph.
         */
        private void preprocessGraph() {
            int iterationID = 0;
            for(GraphNode anchor : this.graph.getAnchors()) {
                NodeMeta meta = new NodeMeta(anchor, iterationID++);

                initializeFromAnchor(anchor, meta);
            }

            for(GraphNode node : this.graph.getNodes()) {
                NodeMeta meta = (NodeMeta) node.meta;
                if(meta == null) {
                    eliminateDeadNode(node);
                } else {
                    if(node.getAllDependencies().isEmpty()) {
                        meta.getOrCreateHeads().add(node);
                    }
                }
            }
        }

        private void initializeFromAnchor(GraphNode current, NodeMeta parent) {
            NodeMeta meta = (NodeMeta) current.meta;
            if(meta != null) {
                if(meta.iterationID == parent.iterationID) {
                    throw new IllegalGraphStateException("Graph contains loops");
                }

                meta.unionMerge(parent);
                return;
            }

            meta = new NodeMeta(current, parent, parent.iterationID);
            for(ResourceDependency dependency : current.getAllDependencies()) {
                if(!dependency.isSatisfied()) {
                    throw new IllegalGraphStateException("Unsatisfied dependency in non dead graph node");
                }

                initializeFromAnchor(dependency.node, meta);
            }
        }

        private void eliminateDeadNode(GraphNode node) {
            // Only need to remove links to non dead nodes, and there cannot be any downwards non dead node since then this node wouldn't be dead
            for(ResourceDependency dependency : node.getAllDependencies()) {
                if(dependency.isSatisfied()) {
                    if(dependency.getDependency().sourceNode.meta != null) {
                        dependency.reset();
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
                if(this.unionParent == null) {
                    return this;
                }

                NodeMeta root = this.unionParent.findRoot();
                this.unionParent = root;
                return root;
            }

            public void unionMerge(NodeMeta other) {
                NodeMeta root = findRoot();
                NodeMeta otherRoot = other.findRoot();

                if(root.unionRank < otherRoot.unionRank) {
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
             *
             * Must be called after all union operations have completed.
             *
             * @return The head list for this subgraph
             */
            public Set<GraphNode> getOrCreateHeads() {
                NodeMeta root = findRoot();
                if(root.heads == null) {
                    root.heads = new ObjectArraySet<>();
                    subgraphs.add(root);
                }

                return root.heads;
            }
        }
    }
}
