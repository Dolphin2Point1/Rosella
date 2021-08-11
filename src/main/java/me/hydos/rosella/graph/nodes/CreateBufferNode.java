package me.hydos.rosella.graph.nodes;

import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.resources.BufferResource;

public class CreateBufferNode extends GraphNode {

    protected final BufferResource buffer;
    protected long size;

    public CreateBufferNode(RenderGraph graph) {
        super(graph);
        this.buffer = new BufferResource(this);
        graph.addNode(this);
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
