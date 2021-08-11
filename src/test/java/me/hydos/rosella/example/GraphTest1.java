package me.hydos.rosella.example;

import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.nodes.CreateBufferNode;
import me.hydos.rosella.init.InitializationRegistry;
import me.hydos.rosella.Rosella;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GraphTest1 {

    private static Rosella createRosella() {
        InitializationRegistry registry = new InitializationRegistry();
        registry.enableValidation(true);

        return new Rosella(registry, new Rosella.ApplicationInfo("GraphTest1", 0, 1, 0));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Rosella engine = createRosella();

        RenderGraph graph = engine.graphEngine.createGraph();
        CreateBufferNode cbuff = new CreateBufferNode(graph);
        cbuff.setSize(16);

        Future<Void> result = engine.graphEngine.execute(graph);
        result.get();

        engine.destroy();
    }
}
