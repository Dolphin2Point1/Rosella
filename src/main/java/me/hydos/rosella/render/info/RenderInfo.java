package me.hydos.rosella.render.info;

import me.hydos.rosella.device.VulkanDevice;
import me.hydos.rosella.device.VulkanDevice;
import me.hydos.rosella.memory.BufferInfo;
import me.hydos.rosella.memory.Memory;
import me.hydos.rosella.memory.MemoryCloseable;

public record RenderInfo(BufferInfo vertexBuffer, BufferInfo indexBuffer, int indexCount) implements MemoryCloseable {
    @Override
    public void free(VulkanDevice device, Memory memory) {
        memory.freeBuffer(vertexBuffer);
        memory.freeBuffer(indexBuffer);
    }
}
