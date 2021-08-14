package me.hydos.rosella.graph.one_time_submit;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import me.hydos.rosella.graph.GraphEngine;
import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.resources.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OTSRenderGraph implements RenderGraph {

    public final Lock lock = new ReentrantLock();

    private State state = State.BUILDING;

    private int pendingNodes = 0;
    private final List<OTSNodeMetadata> nodes = new ObjectArrayList<>();

    private final AtomicInteger nextRenderpassID = new AtomicInteger(0);

    /**
     * List for temporary operations to avoid reallocation. Anyone who uses this list must own the graph lock. If the
     * lock is released the list may be used by others and change content.
     */
    private final List<OTSNodeMetadata> tmpList = new ObjectArrayList<>();

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

    private int nextRenderPass() {
        return this.nextRenderpassID.getAndIncrement();
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

        private boolean inRenderPass = false;
        private OTSNodeMetadata renderPassParent = null;

        private Set<OTSNodeMetadata> parents = null;

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
            if(this.parents == null) {
                this.parents = new ObjectArraySet<>();
            }

            StaticBufferResource buffer = new StaticBufferResource(OTSRenderGraph.this, sSource);
            this.parents.add(((OTSBufferImageMeta) sSource.data).node);
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
            if(this.parents == null) {
                this.parents = new ObjectArraySet<>();
            }

            StaticImageResource image = new StaticImageResource(OTSRenderGraph.this, sSource);
            this.parents.add(((OTSBufferImageMeta) sSource.data).node);
            this.imageResources.add(image);
            return image;
        }

        @Override
        public FramebufferResource createFramebufferResource(FramebufferSpec spec, boolean inRenderPass) {
            if(this.framebufferResource != null) {
                throw new IllegalStateException("A node cannot own more than 1 framebuffer resource");
            }

            this.inRenderPass = inRenderPass;

            this.framebufferResource = new StaticFramebufferResource(OTSRenderGraph.this, spec);
            return this.framebufferResource;
        }

        @Override
        public FramebufferResource createFramebufferResource(FramebufferResource source, boolean inRenderPass) {
            if(!(source instanceof StaticFramebufferResource sSource)) {
                throw new IllegalArgumentException("Source must've been created from a NodeConfigurator but isn't a StaticFramebufferResource");
            }

            if(this.framebufferResource != null) {
                throw new IllegalStateException("A node cannot own more than 1 framebuffer resource");
            }

            if(this.parents == null) {
                this.parents = new ObjectArraySet<>();
            }

            this.inRenderPass = inRenderPass;
            if(inRenderPass) {
                this.renderPassParent = ((OTSFramebufferMeta) sSource.data).node.getOTSMetadata();
                this.parents.add(this.renderPassParent);
            }

            this.framebufferResource = new StaticFramebufferResource(OTSRenderGraph.this, sSource);
            return this.framebufferResource;
        }

        @Override
        public void complete(boolean anchor, int supportedQueueFamilies) {
            try {
                OTSRenderGraph.this.lock.lock();
                if(this.node == null) {
                    throw new IllegalStateException("complete or abort has already been called");
                }

                // Calculate render pass assignments
                int renderPassID = -1;
                int renderPassQueueFamilies = supportedQueueFamilies;
                if(this.renderPassParent == null) {
                    if(this.inRenderPass) {
                        renderPassID = OTSRenderGraph.this.nextRenderPass();
                    }
                } else {
                    renderPassID = this.renderPassParent.renderPassAssignment;
                    renderPassQueueFamilies &= this.renderPassParent.renderPassQueueFamilies;
                }

                long renderPassDependencies = 0;
                for(OTSNodeMetadata other : this.parents) {
                    renderPassDependencies |= other.renderPassDependencies;
                }

                if(this.renderPassParent != null) {
                    if(renderPassQueueFamilies == 0) {
                        // Cannot continue on the same queue family so we need to split
                        renderPassID = OTSRenderGraph.this.nextRenderPass();
                        renderPassQueueFamilies = supportedQueueFamilies;

                    } else {
                        final long rpMask = (1L >> renderPassID);
                        if((rpMask & renderPassDependencies) != 0) {
                            // Dependency from outside the render pass, need to validate that we can merge
                            renderPassQueueFamilies = tryMergeRenderPass(renderPassID, renderPassQueueFamilies);
                            if(renderPassQueueFamilies != 0) {
                                renderPassDependencies &= ~rpMask;
                            } else {
                                renderPassID = OTSRenderGraph.this.nextRenderPass();
                                renderPassQueueFamilies = supportedQueueFamilies;
                            }
                        }
                    }
                }

                OTSNodeMetadata metadata = new OTSNodeMetadata(OTSRenderGraph.this, this.node, this.parents, supportedQueueFamilies);
                metadata.renderPassAssignment = renderPassID;
                metadata.renderPassDependencies = renderPassDependencies;
                metadata.renderPassQueueFamilies = renderPassQueueFamilies;

                this.node.setOTSMetadata(metadata);

                if(this.bufferResources != null) {
                    for(StaticBufferResource buffer : this.bufferResources) {
                        buffer.inject();
                        buffer.data = new OTSBufferImageMeta(metadata);
                    }
                }
                if(this.imageResources != null) {
                    for(StaticImageResource image : this.imageResources) {
                        image.inject();
                        image.data = new OTSBufferImageMeta(metadata);
                    }
                }
                if(this.framebufferResource != null) {
                    this.framebufferResource.inject();
                    // TODO
                }

                if(anchor) {
                    metadata.setRun();
                }

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
         * Attempts to merge all nodes that depend on the current render pass into the render pass.
         *
         * A breath first search is performed finding all nodes that need to be merged. If a node is part of another
         * render pass, does not support execution inside a render pass or no single queue can support all nodes then
         * the merger is aborted.
         *
         * The graph lock must be held before calling this function.
         *
         * @return The new queue limits or 0 if the merge failed.
         */
        private int tryMergeRenderPass(int renderPassID, int queueLimits) {
            final long rpMask = 1L >> renderPassID;
            List<OTSNodeMetadata> nodes = OTSRenderGraph.this.tmpList;
            nodes.clear();

            for(OTSNodeMetadata parent : this.parents) {
                if((parent.renderPassDependencies & rpMask) != 0) {
                    queueLimits &= parent.supportedQueueFamilies;
                    if(queueLimits == 0 || cannotMergeWith(parent)) {
                        return 0;
                    }
                    nodes.add(parent);
                }
            }

            int current = -1;
            while(++current != nodes.size()) {
                for(OTSNodeMetadata parent : nodes.get(current).parents) {
                    if((parent.renderPassDependencies & rpMask) != 0) {
                        queueLimits &= parent.supportedQueueFamilies;
                        if(queueLimits == 0 || cannotMergeWith(parent)) {
                            return 0;
                        }
                        nodes.add(parent);
                    }
                }
            }

            for(OTSNodeMetadata node : nodes) {
                node.renderPassQueueFamilies = queueLimits;
                node.renderPassAssignment = renderPassID;
                node.renderPassDependencies &= ~rpMask;
            }

            return queueLimits;
        }

        private static boolean cannotMergeWith(OTSNodeMetadata node) {
            return (node.renderPassAssignment != -1) || !node.canRunInsideRenderPass;
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
