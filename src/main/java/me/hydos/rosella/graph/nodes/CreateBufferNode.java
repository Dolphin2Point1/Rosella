package me.hydos.rosella.graph.nodes;

import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.resources.BufferResource;
import me.hydos.rosella.graph.resources.DependantResource;
import me.hydos.rosella.graph.resources.Resource;

import java.util.List;

public class CreateBufferNode extends GraphNode {

    protected final BufferResource buffer;
    protected long size;

    public CreateBufferNode(RenderGraph graph) {
        super(graph);
        this.buffer = new BufferResource(this);
        graph.addNode(this);
    }

    @Override
    public List<DependantResource> getAllDependencies() {
        return GraphNode.EMPTY_DEPENDANT_RESOURCE_LIST;
    }

    public void setSize(long size) {
        synchronized (this) {
            this.size = size;
        }
    }

    public BufferResource getBuffer() {
        return this.buffer;
    }
}
