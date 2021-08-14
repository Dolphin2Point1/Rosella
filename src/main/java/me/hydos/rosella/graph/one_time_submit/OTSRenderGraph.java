package me.hydos.rosella.graph.one_time_submit;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.rosella.graph.GraphEngine;
import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.resources.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OTSRenderGraph implements RenderGraph {

    private State state = State.BUILDING;

    private int pendingNodes = 0;
    private final List<OTSNodeMetadata> nodes = new ObjectArrayList<>();

    public final Lock lock = new ReentrantLock();

    public OTSRenderGraph(GraphEngine engine) {
    }

    /**
     * Adds a node to the graph. The node must use the returned configurator objects to configure any requirements and
     * resources. The node is only officially added to the graph once the
     * {@link me.hydos.rosella.graph.one_time_submit.OTSNode.NodeConfigurator::complete(int)} method is called on the
     * returned object.
     *
     * @return The configurator object.
     */
    public OTSNode.NodeConfigurator addNode(@NotNull OTSNode node) {
        try {
            this.lock.lock();
            this.pendingNodes++;
        } finally {
            this.lock.unlock();
        }

        return new NodeConfigurator(node);
    }

    @Override
    public void destroy() {
        try {
            lock.lock();
            if(this.state != State.BUILDING) {
                return;
            }

            for(OTSNodeMetadata metadata : nodes) {
                metadata.node.destroy();
            }

            this.state = State.DESTROYED;
        } finally {
            lock.unlock();
        }
    }

    public void submit() {
    }

    private enum State {
        BUILDING,
        COMPILING,
        DESTROYED
    }

    private class NodeConfigurator implements OTSNode.NodeConfigurator {

        private OTSNode node;

        private List<StaticBufferResource> bufferResources = null;
        private List<StaticImageResource> imageResources = null;
        private StaticFramebufferResource framebufferResource = null;

        public NodeConfigurator(OTSNode node) {
            this.node = node;
        }

        @Override
        public BufferResource createBufferResource(long size, ResourceAccess access, int usageFlags, int accessMask, int stageMask) {
            if(this.bufferResources == null) {
                this.bufferResources = new ObjectArrayList<>();
            }

            StaticBufferResource buffer = new StaticBufferResource(OTSRenderGraph.this, size);
            this.bufferResources.add(buffer);
            return buffer;
        }

        @Override
        public BufferResource createBufferResource(BufferResource source, ResourceAccess access, int usageFlags, int accessMask, int stageMask) {
            if(!(source instanceof StaticBufferResource sSource)) {
                throw new IllegalArgumentException("Source must've been created from a NodeConfigurator but isn't a StaticBufferResource");
            }

            if(this.bufferResources == null) {
                this.bufferResources = new ObjectArrayList<>();
            }

            StaticBufferResource buffer = new StaticBufferResource(OTSRenderGraph.this, sSource);
            this.bufferResources.add(buffer);
            return buffer;
        }

        @Override
        public ImageResource createImageResource(ImageSpec spec, ResourceAccess access, int usageFlags, int accessMask, int stageMask) {
            if(this.imageResources == null) {
                this.imageResources = new ObjectArrayList<>();
            }

            StaticImageResource image = new StaticImageResource(OTSRenderGraph.this, spec);
            this.imageResources.add(image);
            return image;
        }

        @Override
        public ImageResource createImageResource(ImageResource source, ResourceAccess access, int usageFlags, int accessMask, int stageMask) {
            if(!(source instanceof StaticImageResource sSource)) {
                throw new IllegalArgumentException("Source must've been created from a NodeConfigurator but isn't a StaticImageResource");
            }

            if(this.imageResources == null) {
                this.imageResources = new ObjectArrayList<>();
            }

            StaticImageResource image = new StaticImageResource(OTSRenderGraph.this, sSource);
            this.imageResources.add(image);
            return image;
        }

        @Override
        public FramebufferResource createFramebufferResource(FramebufferSpec spec) {
            if(this.framebufferResource != null) {
                throw new IllegalStateException("A node cannot own more than 1 framebuffer resource");
            }

            this.framebufferResource = new StaticFramebufferResource(OTSRenderGraph.this, spec);
            return this.framebufferResource;
        }

        @Override
        public FramebufferResource createFramebufferResource(FramebufferResource source) {
            if(!(source instanceof StaticFramebufferResource sSource)) {
                throw new IllegalArgumentException("Source must've been created from a NodeConfigurator but isn't a StaticFramebufferResource");
            }

            if(this.framebufferResource != null) {
                throw new IllegalStateException("A node cannot own more than 1 framebuffer resource");
            }

            this.framebufferResource = new StaticFramebufferResource(OTSRenderGraph.this, sSource);
            return this.framebufferResource;
        }

        @Override
        public void complete(int queueFlags) {
            try {
                OTSRenderGraph.this.lock.lock();
                if(this.node == null) {
                    throw new IllegalStateException("complete or abort has already been called");
                }

                OTSNodeMetadata metadata = new OTSNodeMetadata(this.node);

                if(this.bufferResources != null) {
                    for(StaticBufferResource buffer : this.bufferResources) {
                        buffer.inject();
                    }
                }
                if(this.imageResources != null) {
                    for(StaticImageResource image : this.imageResources) {
                        image.inject();
                    }
                }
                if(this.framebufferResource != null) {
                    this.framebufferResource.inject();
                }

                this.node.setOTSMetadata(metadata);
                OTSRenderGraph.this.nodes.add(metadata);

                this.notifyGraph();
                this.node = null;
            } finally {
                OTSRenderGraph.this.lock.unlock();
            }
        }

        @Override
        public void abort() {
            try {
                OTSRenderGraph.this.lock.lock();
                if(this.node == null) {
                    throw new IllegalStateException("complete or abort has already been called");
                }

                this.notifyGraph();
                this.node = null;
            } finally {
                OTSRenderGraph.this.lock.unlock();
            }
        }

        /**
         * Decrements the pending nodes counter and notifies the graph if it reaches 0. The graph lock must be acquired
         * before calling this function and it must be ensured that it is never called more than once for each instance.
         */
        private void notifyGraph() {
            if(--OTSRenderGraph.this.pendingNodes == 0) {
                synchronized (OTSRenderGraph.this) {
                    OTSRenderGraph.this.notify();
                }
            }
        }
    }
}
