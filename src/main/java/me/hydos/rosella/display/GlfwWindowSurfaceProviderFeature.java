package me.hydos.rosella.display;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.hydos.rosella.device.VulkanDevice;
import me.hydos.rosella.device.VulkanQueue;
import me.hydos.rosella.init.DeviceBuildConfigurator;
import me.hydos.rosella.init.DeviceBuildInformation;
import me.hydos.rosella.init.features.ApplicationFeature;
import me.hydos.rosella.init.features.GraphPresent;
import me.hydos.rosella.util.BitUtils;
import me.hydos.rosella.util.NamedID;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Tests for display capabilities and allocates a display queue.
 */
public class GlfwWindowSurfaceProviderFeature extends ApplicationFeature {

    public static final NamedID NAME = new NamedID("rosella:default_surface");

    private final long surface;
    private final Set<String> requiredDeviceExtensions;

    public GlfwWindowSurfaceProviderFeature(long surface, Set<String> requiredDeviceExtensions) {
        super(NAME, Set.of(GraphPresent.NAME));
        this.surface = surface;
        this.requiredDeviceExtensions = requiredDeviceExtensions;
    }

    @Override
    public GlfwWindowSurfaceProviderFeatureInstance createInstance() {
        return new GlfwWindowSurfaceProviderFeatureInstance();
    }

    private class GlfwWindowSurfaceProviderFeatureInstance extends ApplicationFeature.Instance {

        private int presentFamily = -1;

        public GlfwWindowSurfaceProviderFeatureInstance() {
        }

        @Override
        public void testFeatureSupport(DeviceBuildInformation meta) {
            this.canEnable = false;
            if(!this.allDependenciesMet(meta)) {
                return;
            }

            if(!meta.getInstance().getCapabilities().VK_KHR_surface) {
                return;
            }

            if(!GlfwWindowSurfaceProviderFeature.this.requiredDeviceExtensions.stream().allMatch(meta::isExtensionAvailable)) {
                return;
            }

            List<VkQueueFamilyProperties> properties = meta.getQueueFamilyProperties();
            List<Integer> supportedFamilies = new IntArrayList();
            try (MemoryStack stack = MemoryStack.stackPush()) {

                IntBuffer bool = stack.mallocInt(1);
                for(int family = 0; family < properties.size(); family++) {
                    KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(meta.getPhysicalDevice(), family, GlfwWindowSurfaceProviderFeature.this.surface, bool);

                    if(bool.get(0) == VK10.VK_TRUE) {
                        if((properties.get(family).queueFlags() & VK10.VK_QUEUE_TRANSFER_BIT) == VK10.VK_QUEUE_TRANSFER_BIT) {
                            supportedFamilies.add(family);
                        }
                    }
                }

                if(supportedFamilies.isEmpty()) {
                    return;
                }

                // Use the queue that supports the least amount of other operations
                supportedFamilies.sort(Comparator.comparingInt(BitUtils::countBits));
                presentFamily = supportedFamilies.get(0);
            }

            this.canEnable = true;
        }

        @Override
        public Object enableFeature(DeviceBuildConfigurator meta) {
            GlfwWindowSurfaceProviderFeature.this.requiredDeviceExtensions.forEach(meta::enableExtension);

            return new GlfwWindowSurfaceProviderFeatures(meta.addQueueRequest(this.presentFamily));
        }
    }

    public static GlfwWindowSurfaceProviderFeatures getMetadata(VulkanDevice device) {
        Object o = device.getFeatureMeta(NAME);

        if(o == null) {
            return null;
        }

        if(!(o instanceof GlfwWindowSurfaceProviderFeatures)) {
            throw new RuntimeException("Meta object could not be cast to DefaultSurfaceProviderFeatures");
        }
        return (GlfwWindowSurfaceProviderFeatures) o;
    }

    public record GlfwWindowSurfaceProviderFeatures(Future<VulkanQueue> presentQueue) {
    }
}
