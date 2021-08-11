package me.hydos.rosella.graph.present;

import me.hydos.rosella.LegacyRosella;
import me.hydos.rosella.device.VulkanQueue;

/**
 * Interface that provides a surface for present operations.
 *
 * Applications can implement this interface to support custom display configurations.
 */
public interface SurfaceProvider {

    /**
     * @return The vulkan surface handle that the {@link me.hydos.rosella.graph.present.DisplaySurface} instance should use
     */
    long getSurfaceHandle();

    /**
     * @return A queue capable of presenting to the surface
     */
    VulkanQueue getPresentQueue();

    /**
     * Called when a new {@link me.hydos.rosella.graph.present.DisplaySurface} is created using this provider.
     *
     * @param engine The Rosella instance that owns the {@link me.hydos.rosella.graph.present.DisplaySurface} instance
     * @param surface The {@link me.hydos.rosella.graph.present.DisplaySurface} instance that uses this provider
     */
    void onAttach(LegacyRosella engine, DisplaySurface surface);

    /**
     * Called when a {@link me.hydos.rosella.graph.present.DisplaySurface} stops using this provider.
     *
     * It is guaranteed that all vulkan objects using the surface have been destroyed before this function is called.
     */
    void onDetach();

    /**
     * Called when the swapchain has to be configured.
     *
     * This function must at least configure the image spec and color space of the created swapchain. All other
     * parameters are optional and will be automatically configured by the engine if no values are provided.
     *
     * @param configuration A instance used to configure swapchain parameters
     */
    void onSwapchainReconfigure(SwapchainConfiguration configuration);
}
