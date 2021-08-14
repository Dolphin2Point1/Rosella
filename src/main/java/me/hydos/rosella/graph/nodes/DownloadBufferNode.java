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

    public final BufferResource result;

    public DownloadBufferNode(OTSRenderGraph graph, BufferResource srcBuffer, BufferCopyRegion copyRegion) {
        super(graph);

        this.copyRegions = List.of(copyRegion);

        NodeConfigurator config = graph.addNode(this);
        this.result = config.createBufferResource(srcBuffer,
                ResourceAccess.READ_ONLY,
                VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK10.VK_ACCESS_TRANSFER_READ_BIT,
                VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
        config.complete(0); // TODO select queues
    }

    public DownloadBufferNode(OTSRenderGraph graph, BufferResource srcBuffer, List<BufferCopyRegion> copyRegions) {
        super(graph);

        if(copyRegions.isEmpty()) {
            throw new IllegalArgumentException("Copy regions list is empty");
        }
        this.copyRegions = new ObjectArrayList<>(copyRegions);

        NodeConfigurator config = graph.addNode(this);
        this.result = config.createBufferResource(srcBuffer,
                ResourceAccess.READ_ONLY,
                VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK10.VK_ACCESS_TRANSFER_READ_BIT,
                VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
        config.complete(0); // TODO select queues
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
