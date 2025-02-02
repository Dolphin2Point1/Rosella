package me.hydos.rosella.memory.buffer;

import it.unimi.dsi.fastutil.ints.*;
import me.hydos.rosella.Rosella;
import me.hydos.rosella.memory.BufferInfo;
import me.hydos.rosella.memory.ManagedBuffer;
import me.hydos.rosella.memory.Memory;
import me.hydos.rosella.render.renderer.Renderer;
import me.hydos.rosella.util.HashUtil;
import me.hydos.rosella.vkobjects.VkCommon;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Iterator;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_GPU_ONLY;
import static org.lwjgl.vulkan.VK10.*;

public class GlobalBufferManager {

    private static final IntHash.Strategy PREHASHED_STRATEGY = new IntHash.Strategy() { // for some reason fastutil still does HashCommon.mix on the strategy
        @Override
        public int hashCode(int e) {
            return e;
        }

        @Override
        public boolean equals(int a, int b) {
            return a == b;
        }
    };

    private final Memory memory;
    private final VkCommon common;
    private final Renderer renderer;

    // TODO: synchronize these structures when we start async (actually may only need to sync in postDraw)

    // these maps are used for actually storing the buffers
    private final Int2ObjectMap<BufferInfo> vertexHashToBufferMap = new Int2ObjectOpenCustomHashMap<>(PREHASHED_STRATEGY);
    private final Int2ObjectMap<BufferInfo> indexHashToBufferMap = new Int2ObjectOpenCustomHashMap<>(PREHASHED_STRATEGY);

    // these sets are used to see if the buffers have been used in the past frame
    private final IntSet usedVertexHashes = new IntOpenCustomHashSet(PREHASHED_STRATEGY);
    private final IntSet usedIndexHashes = new IntOpenCustomHashSet(PREHASHED_STRATEGY);

    public GlobalBufferManager(Rosella rosella) {
        this.memory = rosella.common.memory;
        this.common = rosella.common;
        this.renderer = rosella.renderer;
    }

    public void postDraw() {
        freeUnusedBuffers(vertexHashToBufferMap, usedVertexHashes);
        freeUnusedBuffers(indexHashToBufferMap, usedIndexHashes);
    }

    private void freeUnusedBuffers(Int2ObjectMap<BufferInfo> hashToBufferMap, IntSet usedHashes) {
        Iterator<Int2ObjectMap.Entry<BufferInfo>> entryIterator = hashToBufferMap.int2ObjectEntrySet().iterator();
        while (entryIterator.hasNext()) {
            Int2ObjectMap.Entry<BufferInfo> entry = entryIterator.next();
            if (!usedHashes.contains(entry.getIntKey())) {
                entry.getValue().free(common.device, memory);
                entryIterator.remove();
            }
        }
        usedHashes.clear();
    }

    /**
     * Gets or creates an index buffer
     *
     * @param indexBytes The bytes representing the indices.
     *                   This buffer must have the position set to the start of where you
     *                   want to read and the limit set to the end of where you want to read
     * @return An index buffer
     */
    public BufferInfo getOrCreateIndexBuffer(ManagedBuffer<ByteBuffer> indexBytes) {
        ByteBuffer bytes = indexBytes.buffer();
        int previousPosition = bytes.position();
        int hash = (int) HashUtil.hash64(bytes);
        bytes.position(previousPosition);
        usedIndexHashes.add(hash);
        BufferInfo buffer = indexHashToBufferMap.get(hash);
        if (buffer == null) {
            buffer = createIndexBuffer(indexBytes);
            indexHashToBufferMap.put(hash, buffer);
        } else {
            indexBytes.free(common.device, memory);
        }
        return buffer;
    }

    /**
     * Gets or creates a vertex buffer
     *
     * @param vertexBytes The bytes representing the vertices.
     *                    This buffer must have the position set to the start of where you
     *                    want to read and the limit set to the end of where you want to read
     * @return A vertex buffer
     */
    public BufferInfo getOrCreateVertexBuffer(ManagedBuffer<ByteBuffer> vertexBytes) {
        ByteBuffer bytes = vertexBytes.buffer();
        int previousPosition = bytes.position();
        int hash = (int) HashUtil.hash64(bytes);
        bytes.position(previousPosition);
        usedVertexHashes.add(hash);
        BufferInfo buffer = vertexHashToBufferMap.get(hash);
        if (buffer == null) {
            buffer = createVertexBuffer(vertexBytes);
            vertexHashToBufferMap.put(hash, buffer);
        } else {
            vertexBytes.free(common.device, memory);
        }
        return buffer;
    }

    /**
     * Creates a index buffer
     *
     * @param indexBytes The bytes representing the indices.
     *                   This buffer must have the position set to the start of where you
     *                   want to read and the limit set to the end of where you want to read
     * @return An index buffer
     */
    public BufferInfo createIndexBuffer(ManagedBuffer<ByteBuffer> indexBytes) {
        ByteBuffer src = indexBytes.buffer();
        int size = src.limit() - src.position();

        try (MemoryStack stack = stackPush()) {
            LongBuffer pBuffer = stack.mallocLong(1);

            BufferInfo stagingBuffer = memory.createStagingBuf(size, pBuffer, data -> {
                ByteBuffer dst = data.getByteBuffer(0, size);
                // TODO OPT: do optional batching again
                dst.put(0, src, src.position(), size);
            });
            indexBytes.free(common.device, memory);

            BufferInfo indexBuffer = memory.createBuffer(
                    size,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                    VMA_MEMORY_USAGE_GPU_ONLY,
                    pBuffer
            );

            long pIndexBuffer = pBuffer.get(0);
            memory.copyBuffer(stagingBuffer.buffer(),
                    pIndexBuffer,
                    size,
                    renderer,
                    common.device);
            stagingBuffer.free(common.device, memory);

            return indexBuffer;
        }
    }

    /**
     * Creates a vertex buffer.
     *
     * @param vertexBytes The bytes representing the vertices.
     *                    This buffer must have the position set to the start of where you
     *                    want to read and the limit set to the end of where you want to read
     * @return A vertex buffer
     */
    public BufferInfo createVertexBuffer(ManagedBuffer<ByteBuffer> vertexBytes) {
        ByteBuffer src = vertexBytes.buffer();
        int size = src.limit() - src.position();

        try (MemoryStack stack = stackPush()) {
            LongBuffer pBuffer = stack.mallocLong(1);

            BufferInfo stagingBuffer = memory.createStagingBuf(size, pBuffer, data -> {
                ByteBuffer dst = data.getByteBuffer(0, size);
                // TODO OPT: do optional batching again
                dst.put(0, src, src.position(), size);
            });
            vertexBytes.free(common.device, memory);

            BufferInfo vertexBuffer = memory.createBuffer(
                    size,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    VMA_MEMORY_USAGE_GPU_ONLY,
                    pBuffer
            );

            long pVertexBuffer = pBuffer.get(0);
            memory.copyBuffer(stagingBuffer.buffer(),
                    pVertexBuffer,
                    size,
                    renderer,
                    common.device);
            stagingBuffer.free(common.device, memory);

            return vertexBuffer;
        }
    }

    public void free() {
        for (BufferInfo buffer : vertexHashToBufferMap.values()) {
            buffer.free(common.device, common.memory);
        }

        for (BufferInfo buffer : indexHashToBufferMap.values()) {
            buffer.free(common.device, common.memory);
        }
    }
}
