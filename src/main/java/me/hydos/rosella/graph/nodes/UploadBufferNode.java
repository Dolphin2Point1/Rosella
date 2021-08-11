package me.hydos.rosella.graph.nodes;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.resources.BufferCopyRegion;
import me.hydos.rosella.graph.resources.BufferResource;
import me.hydos.rosella.graph.resources.BufferResourceDependency;
import me.hydos.rosella.graph.resources.ResourceAccess;

import java.nio.ByteBuffer;
import java.util.List;

public class UploadBufferNode extends GraphNode {

    protected final BufferResourceDependency dstBuffer;
    protected final BufferResource result;
    protected final List<BufferCopyRegion> copyRegions = new ObjectArrayList<>();

    public UploadBufferNode(RenderGraph graph) {
        super(graph);
        this.dstBuffer = new BufferResourceDependency(this, ResourceAccess.WRITE_ONLY);
        this.result = new BufferResource(this, this.dstBuffer);
        graph.addNode(this);
    }

    public void setDstBuffer(BufferResource dstBuffer) {
        this.dstBuffer.setDependency(dstBuffer);
    }

    /**
     * Sets the data that will be uploaded to the buffer.
     *
     * @param data The data that should be uploaded to the buffer
     */
    public void setData(ByteBuffer data) {
        // TODO
    }

    public BufferResource getResult() {
        return this.result;
    }

    public void addCopyRegion(BufferCopyRegion region) {
        this.copyRegions.add(region);
    }
}
