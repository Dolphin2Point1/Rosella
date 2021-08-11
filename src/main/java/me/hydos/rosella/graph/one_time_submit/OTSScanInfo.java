package me.hydos.rosella.graph.one_time_submit;

import me.hydos.rosella.graph.resources.*;

/**
 *
 */
public interface OTSScanInfo {

    /**
     * Resolves the spec of a buffer resource.
     *
     * @param resource The resource to resolve.
     * @return The size of the buffer resource.
     */
    long resolveBufferResource(BufferResource resource);

    /**
     * Resolves the spec of a image resource.
     *
     * @param resource The resource to resolve.
     * @return The spec of the image resource.
     */
    ImageSpec resolveImageResource(ImageResource resource);

    /**
     * Resolves the spec of a framebuffer resource.
     *
     * @param resource The resource to resolve.
     * @return The spec of the framebuffer resource.
     */
    FramebufferSpec resolveFramebufferResource(FramebufferResource resource);
}
