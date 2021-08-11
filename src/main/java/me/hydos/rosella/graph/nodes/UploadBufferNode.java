package me.hydos.rosella.graph.nodes;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.resources.*;

import java.nio.ByteBuffer;
import java.util.List;

public class UploadBufferNode extends GraphNode {

    protected final DependantBufferResource dstBuffer;
    protected final List<BufferCopyRegion> copyRegions = new ObjectArrayList<>();

    public UploadBufferNode(RenderGraph graph) {
        super(graph);
        this.dstBuffer = new DependantBufferResource(this, ResourceAccess.WRITE_ONLY);
        graph.addNode(this);
    }

    @Override
    public List<DependantResource> getAllDependencies() {
        return List.of(dstBuffer);
    }

    public void setDstBuffer(BufferResource dstBuffer) {
        this.dstBuffer.setSource(dstBuffer);
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
        return this.dstBuffer;
    }

    public void addCopyRegion(BufferCopyRegion region) {
        this.copyRegions.add(region);
    }
}
