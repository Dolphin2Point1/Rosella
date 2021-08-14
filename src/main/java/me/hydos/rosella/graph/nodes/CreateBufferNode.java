package me.hydos.rosella.graph.nodes;

import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.one_time_submit.OTSNode;
import me.hydos.rosella.graph.one_time_submit.OTSNodeMetadata;
import me.hydos.rosella.graph.one_time_submit.OTSRenderGraph;
import me.hydos.rosella.graph.resources.BufferResource;
import me.hydos.rosella.graph.resources.ResourceAccess;

public class CreateBufferNode extends AbstractGraphNode implements OTSNode {

    private OTSNodeMetadata otsMetadata = null;

    public final BufferResource buffer;

    public CreateBufferNode(OTSRenderGraph graph, long bufferSize) {
        super(graph);

        OTSNode.NodeConfigurator config = graph.addNode(this);
        this.buffer = config.createBufferResource(bufferSize, ResourceAccess.NONE, 0, 0, 0);
        config.complete(0);
    }

    public BufferResource getBuffer() {
        return this.buffer;
    }

    @Override
    public void setOTSMetadata(OTSNodeMetadata metadata) {
        this.otsMetadata = metadata;
    }

    @Override
    public OTSNodeMetadata getOTSMetadata() {
        return this.otsMetadata;
    }
}
