package me.hydos.rosella.display;

import me.hydos.rosella.LegacyRosella;
import me.hydos.rosella.device.VulkanQueue;
import me.hydos.rosella.graph.present.DisplaySurface;
import me.hydos.rosella.graph.present.SurfaceProvider;
import me.hydos.rosella.graph.present.SwapchainConfiguration;
import me.hydos.rosella.init.InitializationRegistry;
import me.hydos.rosella.init.VulkanInstance;
import me.hydos.rosella.util.VkUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;

public class GlfwWindow implements SurfaceProvider {

    private long window = 0;

    private Function<List<SurfaceFormat>, SurfaceFormat> formatSelector = this::defaultFormatSelector;
    private Function<List<Integer>, Integer> presentModeSelector = this::defaultPresentModeSelector;

    private int currentWidth;
    private int currentHeight;

    private VulkanInstance instance = null;
    private DisplaySurface displaySurface = null;
    private VulkanQueue presentQueue = null;
    private long surface = VK10.VK_NULL_HANDLE;

    private List<SurfaceFormat> supportedFormats;
    private List<Integer> supportedPresentModes;

    public GlfwWindow(int initialWidth, int initialHeight, String title, boolean canResize) {
        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new RuntimeException("Your machine doesn't support Vulkan :(");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, canResize ? GLFW_TRUE : GLFW_FALSE);
        this.window = glfwCreateWindow(initialWidth, initialHeight, title, 0, 0);

        this.currentWidth = initialWidth;
        this.currentHeight = initialHeight;
    }

    public void destroy() {
        assert(displaySurface == null);

        this.presentQueue = null;
        if(surface != VK10.VK_NULL_HANDLE) {
            KHRSurface.vkDestroySurfaceKHR(this.instance.getInstance(), this.surface, null);
            this.surface = VK10.VK_NULL_HANDLE;
        }
        if(this.window != 0) {
            glfwDestroyWindow(this.window);
            this.window = 0;
        }
    }

    public void setFormatSelector(Function<List<SurfaceFormat>, SurfaceFormat> selector) {
        if(selector != null) {
            this.formatSelector = selector;
        } else {
            this.formatSelector = this::defaultFormatSelector;
        }
    }

    public void setPresentModeSelector(Function<List<Integer>, Integer> selector) {
        if(selector != null) {
            this.presentModeSelector = selector;
        } else {
            this.presentModeSelector = this::defaultPresentModeSelector;
        }
    }

    public DisplaySurface getDisplaySurface() {
        return this.displaySurface;
    }

    public void onPreInstanceCreate(InitializationRegistry registry) {
        PointerBuffer requiredExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        if (requiredExtensions != null) {
            for (int i = 0; i < requiredExtensions.limit(); i++) {
                registry.addRequiredInstanceExtensions(requiredExtensions.getStringUTF8(i));
            }
        }
    }

    public void onInstanceReady(VulkanInstance instance, InitializationRegistry registry) {
        this.instance = instance;
        try(MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSurface = stack.mallocLong(1);
            VkUtils.ok(GLFWVulkan.glfwCreateWindowSurface(this.instance.getInstance(), this.window, null, pSurface));
            this.surface = pSurface.get();

            registry.registerApplicationFeature(new GlfwWindowSurfaceProviderFeature(this.surface, Collections.emptySet()));
            registry.addRequiredApplicationFeature(GlfwWindowSurfaceProviderFeature.NAME);
        }
    }

    @Override
    public long getSurfaceHandle() {
        return this.surface;
    }

    @Override
    public VulkanQueue getPresentQueue() {
        return this.presentQueue;
    }

    @Override
    public void onAttach(LegacyRosella engine, DisplaySurface surface) {
        assert(engine.vulkanInstance.getInstance() == instance.getInstance() && displaySurface == null);

        this.displaySurface = surface;
        try {
            this.presentQueue = Objects.requireNonNull(GlfwWindowSurfaceProviderFeature.getMetadata(engine.vulkanDevice)).presentQueue().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        VkPhysicalDevice physicalDevice = engine.vulkanDevice.getDevice().getPhysicalDevice();
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.mallocStack(stack);
            VkUtils.ok(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, this.surface, capabilities));

            IntBuffer count = stack.mallocInt(1);
            VkUtils.ok(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, this.surface, count, null));
            VkSurfaceFormatKHR.Buffer formats = VkSurfaceFormatKHR.mallocStack(count.get(0), stack);
            VkUtils.ok(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, this.surface, count, formats));

            List<SurfaceFormat> surfaceFormats = new ArrayList<>();
            for(int i = 0; i < count.get(0); i++) {
                VkSurfaceFormatKHR format = formats.get(i);
                surfaceFormats.add(new SurfaceFormat(format.format(), format.colorSpace()));
            }
            this.supportedFormats = Collections.unmodifiableList(surfaceFormats);

            VkUtils.ok(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, this.surface, count, null));
            IntBuffer modes = stack.mallocInt(count.get(0));
            VkUtils.ok(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, this.surface, count, modes));

            List<Integer> presentModes = new ArrayList<>();
            for(int i = 0; i < count.get(0); i++) {
                int mode = modes.get(i);
                presentModes.add(mode);
            }
            this.supportedPresentModes = Collections.unmodifiableList(presentModes);
        }
    }

    @Override
    public void onDetach() {
        this.displaySurface = null;
    }

    @Override
    public void onSwapchainReconfigure(SwapchainConfiguration configuration) {
        updateSize();

        SurfaceFormat format = this.formatSelector.apply(this.supportedFormats);
        int presentMode = this.presentModeSelector.apply(this.supportedPresentModes);

        // TODO
    }

    private void updateSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetFramebufferSize(window, pWidth, pHeight);
            while(pWidth.get(0) == 0 || pHeight.get(0) == 0) {
                glfwWaitEvents();
                glfwGetFramebufferSize(window, pWidth, pHeight);
            }

            this.currentWidth = pWidth.get(0);
            this.currentHeight = pWidth.get(0);
        }
    }

    private SurfaceFormat defaultFormatSelector(List<SurfaceFormat> formats) {
        return formats.get(0);
    }

    private int defaultPresentModeSelector(List<Integer> modes) {
        return KHRSurface.VK_PRESENT_MODE_FIFO_KHR; // Guaranteed to be always available by the spec
    }

    public record SurfaceFormat(int format, int colorSpace) {
    }
}
