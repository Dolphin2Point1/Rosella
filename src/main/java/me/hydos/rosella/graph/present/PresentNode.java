package me.hydos.rosella.graph.present;

import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.nodes.GraphNode;

public class PresentNode extends GraphNode {

    private final BasicDisplaySurface surface;

    protected PresentNode(BasicDisplaySurface surface, RenderGraph graph) {
        super(graph);
        this.surface = surface;
    }

    @Override
    public boolean isAnchor() {
        return true;
    }
}
