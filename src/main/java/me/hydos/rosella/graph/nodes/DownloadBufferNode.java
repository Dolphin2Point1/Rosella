package me.hydos.rosella.graph.nodes;

import me.hydos.rosella.graph.RenderGraph;
import me.hydos.rosella.graph.resources.BufferResource;
import me.hydos.rosella.graph.resources.DependantBufferResource;
import me.hydos.rosella.graph.resources.ResourceAccess;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class DownloadBufferNode extends GraphNode {

    protected final DependantBufferResource source;
    protected final CompletableFuture<ByteBuffer> result;

    protected ByteBuffer dst;
    protected long sourceOffset;

    public DownloadBufferNode(RenderGraph graph) {
        super(graph);
        this.source = new DependantBufferResource(this, ResourceAccess.READ_ONLY);
        this.result = new CompletableFuture<>();
        graph.addNode(this);
    }

    public void setSource(BufferResource source) {
        synchronized(this) {
            this.source.setSource(source);
        }
    }

    /**
     * Defines the buffer where the downloaded data will be stored.
     * The amount of data copied will be <code>min(sourceSize - offset, dstRemaining)</code>
     *
     * The dst buffer must not be modified until either a different buffer is provided to this function
     * or the result future is completed.
     *
     * @param dst The destination buffer
     */
    public void setDstBuffer(ByteBuffer dst) {
        synchronized(this) {
            this.dst = dst;
        }
    }

    /**
     * Defines the offset into the source buffer from where to start the download range.
     *
     * @param offset The offset into the source buffer
     */
    public void setSourceOffset(long offset) {
        synchronized (this) {
            this.sourceOffset = offset;
        }
    }

    /**
     * Returns a future that can be used to get the result of the download operation.
     *
     * If the download operation is aborted (for example if the graph gets aborted) the future will be canceled.
     *
     * @return Future providing the result of the download operation
     */
    public Future<ByteBuffer> getResult() {
        return this.result;
    }

    @Override
    public boolean isAnchor() {
        return true;
    }

    @Override
    public void destroy() {
        this.result.cancel(true);
    }
}
