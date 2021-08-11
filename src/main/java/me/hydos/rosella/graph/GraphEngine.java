package me.hydos.rosella.graph;

import me.hydos.rosella.Rosella;
import me.hydos.rosella.graph.one_time_submit.OneTimeSubmitProcess;

import org.apache.logging.log4j.Logger;

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
        mainWorkers.execute(new OneTimeSubmitProcess(this, graph, future));
        return future;
    }

}
