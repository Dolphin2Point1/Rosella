package me.hydos.rosella;

import me.hydos.rosella.debug.MessageSeverity;
import me.hydos.rosella.debug.MessageType;
import me.hydos.rosella.debug.VulkanDebugCallback;
import me.hydos.rosella.device.VulkanDevice;
import me.hydos.rosella.graph.GraphEngine;
import me.hydos.rosella.init.DeviceBuilder;
import me.hydos.rosella.init.InitializationRegistry;
import me.hydos.rosella.init.InstanceBuilder;
import me.hydos.rosella.init.VulkanInstance;

import me.hydos.rosella.init.features.GraphTransfer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;

public class Rosella {

    public final Logger logger = LogManager.getLogger("Rosella", new StringFormatterMessageFactory());

    public final VulkanInstance vulkanInstance;
    public final VulkanDevice vulkanDevice;

    public final GraphEngine graphEngine;

    public Rosella(InitializationRegistry registry, ApplicationInfo applicationInfo) {
        this.logger.info("Initializing new Rosella instance");
        this.logger.info("Application: \"{}\" {}.{}.{}", applicationInfo.name, applicationInfo.versionMajor, applicationInfo.versionMinor, applicationInfo.versionPatch);

        addRosellaFeatures(registry);
        registry.addDebugCallback(new RosellaDebugLogger(LogManager.getLogger("Vulkan")));
        this.vulkanInstance = new InstanceBuilder(registry).build(applicationInfo.name, applicationInfo.toVulkanVersion());

        try {
            this.vulkanDevice = new DeviceBuilder(this.vulkanInstance, registry).build();
        } catch (Exception ex) {
            this.vulkanInstance.destroy();
            throw ex;
        }

        this.graphEngine = new GraphEngine(this);

        this.logger.info("Rosella initialization complete");
    }

    public void destroy() {
        this.logger.info("Shutting down Rosella");
        this.graphEngine.destroy();
        this.vulkanDevice.destroy();
        this.vulkanInstance.destroy();
        this.logger.info("Rosella shutdown complete");
    }

    private static void addRosellaFeatures(InitializationRegistry registry) {
        registry.registerApplicationFeature(new GraphTransfer());
    }

    public record ApplicationInfo(String name, int versionMajor, int versionMinor, int versionPatch) {

        /**
         * @return The vulkan representation of the application version
         */
        public int toVulkanVersion() {
            return VK10.VK_MAKE_VERSION(this.versionMajor, this.versionMinor, this.versionPatch);
        }
    }

    private static class RosellaDebugLogger extends VulkanDebugCallback.Callback {

        private final Logger vkLogger;

        private RosellaDebugLogger(Logger logger) {
            super(MessageSeverity.allBits(), MessageType.allBits());
            this.vkLogger = logger;
        }

        @Override
        protected void callInternal(MessageSeverity severity, MessageType type, VkDebugUtilsMessengerCallbackDataEXT data) {
            switch (severity) {
                case VERBOSE -> {
                    this.vkLogger.debug(data.pMessageString());
                }
                case INFO -> {
                    this.vkLogger.info(data.pMessageString());
                }
                case WARNING -> {
                    this.vkLogger.warn(data.pMessageString());
                }
                case ERROR -> {
                    this.vkLogger.error(data.pMessageString());
                }
            }
        }
    }
}
