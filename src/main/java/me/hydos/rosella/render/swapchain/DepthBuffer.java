package me.hydos.rosella.render.swapchain;

import me.hydos.rosella.device.VulkanDevice;
import me.hydos.rosella.device.VulkanDevice;
import me.hydos.rosella.memory.Memory;
import me.hydos.rosella.memory.MemoryCloseable;
import me.hydos.rosella.render.renderer.Renderer;
import me.hydos.rosella.render.texture.TextureImage;
import me.hydos.rosella.util.VkUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VkFormatProperties;

import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.vulkan.VK10.*;

public class DepthBuffer implements MemoryCloseable {

    private TextureImage depthImage;

    public TextureImage getDepthImage() {
        return Objects.requireNonNull(depthImage);
    }

    public void createDepthResources(VulkanDevice device, Memory memory, Swapchain swapchain, Renderer renderer) {
        int depthFormat = findDepthFormat(device);
        depthImage = VkUtils.createImage(
                memory,
                swapchain.getSwapChainExtent().width(),
                swapchain.getSwapChainExtent().height(),
                depthFormat,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                Vma.VMA_MEMORY_USAGE_UNKNOWN // FIXME
        );
        depthImage.setView(VkUtils.createImageView(device, depthImage.pointer(), depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT));

        // Explicitly transitioning the depth image
        VkUtils.transitionImageLayout(
                renderer,
                device,
                renderer.depthBuffer,
                depthImage.pointer(),
                depthFormat,
                VK_IMAGE_LAYOUT_UNDEFINED,
                VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL
        );
    }

    public int findDepthFormat(VulkanDevice device) {
        return findSupportedFormat(
                MemoryStack.stackGet()
                        .ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT),
                VK_IMAGE_TILING_OPTIMAL,
                VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT,
                device
        );
    }

    public boolean hasStencilComponent(int format) {
        return format == VK_FORMAT_D32_SFLOAT_S8_UINT || format == VK_FORMAT_D24_UNORM_S8_UINT;
    }

    private int findSupportedFormat(IntBuffer formatCandidates, int tiling, int features, VulkanDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFormatProperties props = VkFormatProperties.callocStack(stack);

            for (int i = 0; i < formatCandidates.capacity(); i++) {
                int format = formatCandidates.get(i);
                vkGetPhysicalDeviceFormatProperties(device.getRawDevice().getPhysicalDevice(), format, props);

                if (tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features) {
                    return format;
                } else if (tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features) {
                    return format;
                }
            }
        }

        throw new RuntimeException("Failed to find supported format");
    }

    @Override
    public void free(VulkanDevice device, Memory memory) {
        depthImage.free(device, memory);
    }
}
