package me.hydos.rosella.graph.nodes;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.rosella.graph.one_time_submit.OTSNode;
import me.hydos.rosella.graph.one_time_submit.OTSNodeMetadata;
import me.hydos.rosella.graph.one_time_submit.OTSRenderGraph;
import me.hydos.rosella.graph.resources.*;
import org.lwjgl.vulkan.VK10;

import java.util.List;

public class UploadBufferNode extends AbstractGraphNode implements OTSNode {

    private OTSNodeMetadata otsMetadata = null;

    private final List<BufferCopyRegion> copyRegions;
    private final long minSrcBufferSize;

    public final BufferResource result;

    public UploadBufferNode(OTSRenderGraph graph, BufferResource dstBuffer, BufferCopyRegion copyRegion) {
        super(graph);

        if(dstBuffer.getBufferSize() < copyRegion.dstOffset() + copyRegion.size()) {
            throw new IllegalArgumentException("Copy region extend beyond destination buffer size");
        }
        this.minSrcBufferSize = copyRegion.srcOffset() + copyRegion.size();
        this.copyRegions = List.of(copyRegion);

        OTSNode.NodeConfigurator config = graph.addNode(this);
        this.result = config.createBufferResource(dstBuffer,
                ResourceAccess.WRITE_ONLY,
                VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK10.VK_ACCESS_TRANSFER_WRITE_BIT,
                VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
        config.complete(false, 0); // TODO: select all queues
    }

    public UploadBufferNode(OTSRenderGraph graph, BufferResource dstBuffer, List<BufferCopyRegion> copyRegions) {
        super(graph);

        if(copyRegions.isEmpty()) {
            throw new IllegalArgumentException("Copy regions list is empty");
        }
        long minSrc = 0;
        long minDst = 0;
        for(BufferCopyRegion region : copyRegions) {
            long rMinSrc = region.srcOffset() + region.size();
            long rMinDst = region.dstOffset() + region.size();

            if(minSrc < rMinSrc) {
                minSrc = rMinSrc;
            }
            if(minDst < rMinDst) {
                minDst = rMinDst;
            }
        }
        if(dstBuffer.getBufferSize() < minDst) {
            throw new IllegalArgumentException("Copy regions extend beyond destination buffer size");
        }
        this.minSrcBufferSize = minSrc;
        this.copyRegions = new ObjectArrayList<>(copyRegions);

        OTSNode.NodeConfigurator config = graph.addNode(this);
        this.result = config.createBufferResource(dstBuffer,
                ResourceAccess.WRITE_ONLY,
                VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK10.VK_ACCESS_TRANSFER_WRITE_BIT,
                VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
        config.complete(false, 0); // TODO: select all queues
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
