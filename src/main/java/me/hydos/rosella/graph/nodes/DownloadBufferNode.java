package me.hydos.rosella.graph.nodes;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.rosella.graph.one_time_submit.OTSNode;
import me.hydos.rosella.graph.one_time_submit.OTSNodeMetadata;
import me.hydos.rosella.graph.one_time_submit.OTSRenderGraph;
import me.hydos.rosella.graph.resources.BufferCopyRegion;
import me.hydos.rosella.graph.resources.BufferResource;
import me.hydos.rosella.graph.resources.ResourceAccess;
import org.lwjgl.vulkan.VK10;

import java.util.List;

public class DownloadBufferNode extends AbstractGraphNode implements OTSNode {

    private OTSNodeMetadata otsMetadata = null;

    private final List<BufferCopyRegion> copyRegions;
    private final long minDstBufferSize;

    public final BufferResource result;

    public DownloadBufferNode(OTSRenderGraph graph, BufferResource srcBuffer, BufferCopyRegion copyRegion) {
        super(graph);

        if(srcBuffer.getBufferSize() < copyRegion.srcOffset() + copyRegion.size()) {
            throw new IllegalArgumentException("Copy region extend beyond source buffer size");
        }
        this.minDstBufferSize = copyRegion.dstOffset() + copyRegion.size();
        this.copyRegions = List.of(copyRegion);

        NodeConfigurator config = graph.addNode(this);
        this.result = config.createBufferResource(srcBuffer,
                ResourceAccess.READ_ONLY,
                VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK10.VK_ACCESS_TRANSFER_READ_BIT,
                VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
        config.complete(true, 0); // TODO select queues
    }

    public DownloadBufferNode(OTSRenderGraph graph, BufferResource srcBuffer, List<BufferCopyRegion> copyRegions) {
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
        if(srcBuffer.getBufferSize() < minSrc) {
            throw new IllegalArgumentException("Copy regions extend beyond source buffer size");
        }
        this.minDstBufferSize = minDst;
        this.copyRegions = new ObjectArrayList<>(copyRegions);

        NodeConfigurator config = graph.addNode(this);
        this.result = config.createBufferResource(srcBuffer,
                ResourceAccess.READ_ONLY,
                VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK10.VK_ACCESS_TRANSFER_READ_BIT,
                VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
        config.complete(true, 0); // TODO select queues
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
