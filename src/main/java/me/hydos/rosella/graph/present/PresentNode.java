package me.hydos.rosella.graph.present;

import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.nodes.AbstractGraphNode;
import me.hydos.rosella.graph.resources.DependantResource;

import java.util.List;

public class PresentNode extends AbstractGraphNode {

    private final BasicDisplaySurface surface;

    protected PresentNode(BasicDisplaySurface surface, RenderGraph graph) {
        super(graph);
        this.surface = surface;
    }

    @Override
    public boolean isAnchor() {
        return true;
    }

    @Override
    public List<DependantResource> getAllDependencies() {
        return AbstractGraphNode.EMPTY_DEPENDANT_RESOURCE_LIST;
    }
}
