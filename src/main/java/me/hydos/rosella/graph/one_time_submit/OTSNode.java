package me.hydos.rosella.graph.one_time_submit;

import me.hydos.rosella.graph.nodes.GraphNode;
import me.hydos.rosella.graph.resources.*;

/**
 * Nodes that support being added to a one time submit graph.
 */
public interface OTSNode extends GraphNode {

    void setOTSMetadata(OTSNodeMetadata metadata);

    /**
     * @return The metadata object given to the node when otsInit was called
     */
    OTSNodeMetadata getOTSMetadata();

    /**
     * A class used to configure a node.
     */
    interface NodeConfigurator {

        BufferResource createBufferResource(long size, ResourceAccess access, int usageFlags, int accessMask, int stageMask);

        BufferResource createBufferResource(BufferResource source, ResourceAccess access, int usageFlags, int accessMask, int stageMask);

        ImageResource createImageResource(ImageSpec spec, ResourceAccess access, int usageFlags, int accessMask, int stageMask);

        ImageResource createImageResource(ImageResource source, ResourceAccess access, int usageFlags, int accessMask, int stageMask);

        FramebufferResource createFramebufferResource(FramebufferSpec spec, boolean inRenderPass);

        FramebufferResource createFramebufferResource(FramebufferResource source, boolean inRenderPass);

        void complete(boolean anchor, int queueFlags);

        void abort();
    }
}
