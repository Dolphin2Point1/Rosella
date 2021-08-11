package me.hydos.rosella.init.features;

import me.hydos.rosella.device.VulkanDevice;
import me.hydos.rosella.init.DeviceBuildConfigurator;
import me.hydos.rosella.init.DeviceBuildInformation;
import me.hydos.rosella.util.NamedID;
import org.lwjgl.vulkan.KHRSwapchain;

public class GraphPresent extends ApplicationFeature {

    public static final NamedID NAME = new NamedID("rosella:graph_present");

    public GraphPresent() {
        super(NAME);
    }

    @Override
    public Instance createInstance() {
        return new GraphPresentInstance();
    }

    private class GraphPresentInstance extends Instance {

        @Override
        public void testFeatureSupport(DeviceBuildInformation meta) {
            this.canEnable = false;
            if(!this.allDependenciesMet(meta)) {
                return;
            }

            if(!meta.isExtensionAvailable(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)) {
                return;
            }

            this.canEnable = true;
        }

        @Override
        public Object enableFeature(DeviceBuildConfigurator meta) {
            meta.enableExtension(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
            return null;
        }
    }
}
