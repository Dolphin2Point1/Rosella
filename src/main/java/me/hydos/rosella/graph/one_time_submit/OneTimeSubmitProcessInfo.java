package me.hydos.rosella.graph.one_time_submit;

import me.hydos.rosella.graph.GraphEngine;
import me.hydos.rosella.graph.resources.*;

public interface OneTimeSubmitProcessInfo {

    GraphEngine getGraphEngine();

    /**
     * Determines the specification of a buffer resource. The resource must have been resolved before this function
     * is called.
     *
     * @param resource The buffer
     * @return The size of the buffer.
     */
    long getBufferSpec(BufferResource resource);

    /**
     * Determines the specification of a image resource. The resource must have been resolved before this function
     * is called.
     *
     * @param resource The image
     * @return The spec of the image.
     */
    ImageSpec getImageSpec(ImageResource resource);

    /**
     * Determines the specification of a framebuffer resource. The resource must have been resolved before this function
     * is called.
     *
     * @param resource The framebuffer
     * @return The spec of the framebuffer.
     */
    FramebufferSpec getFramebufferSpec(FramebufferResource resource);
}
