package me.hydos.rosella.device;

import me.hydos.rosella.LegacyRosella;

/**
 * The presentation and graphics queue used in {@link LegacyRosella}
 */
public class VulkanQueues {

    public final VulkanQueue graphicsQueue;
    public final VulkanQueue presentQueue;

    public VulkanQueues(VulkanQueue graphicsQueue, VulkanQueue presentQueue) {
        this.graphicsQueue = graphicsQueue;
        this.presentQueue = presentQueue;
    }
}
