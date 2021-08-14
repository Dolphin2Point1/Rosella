package me.hydos.rosella.graph;

import me.hydos.rosella.Rosella;
import me.hydos.rosella.graph.one_time_submit.OTSRenderGraph;

import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class GraphEngine {

    public final Rosella rosella;
    public final Logger logger;

    private final ExecutorService workerPool = Executors.newFixedThreadPool(1);

    public GraphEngine(Rosella rosella) {
        this.rosella = rosella;
        this.logger = rosella.logger;

        this.logger.debug("Initializing graph engine");

        this.logger.debug("Graph engine initialization complete");
    }

    public void destroy() {
        this.logger.debug("Destroying graph engine");

        this.workerPool.shutdown();
        try {
            this.logger.trace("Waiting for shutdown of graph engine workers");
            while(!workerPool.awaitTermination(1, TimeUnit.MINUTES)) {
                this.logger.warn("Reached 1min timeout while waiting for graph engine workers shutdown");
            }
        } catch (InterruptedException e) {
            this.logger.error("Interrupted while waiting for graph engine workers shutdown", e);
        }
    }

    public RenderGraph createGraph() {
        return new OTSRenderGraph(this);
    }

}
