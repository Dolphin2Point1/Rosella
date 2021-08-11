package me.hydos.rosella.graph.one_time_submit;

import me.hydos.rosella.graph.resources.*;

/**
 *
 */
public interface OTSScanPhase extends OneTimeSubmitProcessInfo {

    /**
     * Resolves the spec of a buffer resource.
     *
     * @param resource The resource to resolve.
     */
    void resolveBufferResource(BufferResource resource, OTSBufferExecutionRequirements requirements);

    /**
     * Resolves the spec of a image resource.
     *
     * @param resource The resource to resolve.
     */
    void resolveImageResource(ImageResource resource, OTSImageExecutionRequirements requirements);

    /**
     * Resolves the spec of a framebuffer resource.
     *
     * @param resource The resource to resolve.
     */
    void resolveFramebufferResource(FramebufferResource resource, OTSFramebufferExecutionRequirements requirements);

    /**
     * Resolves a custom resource. No operations whatsoever will be performed on the resource besides marking it as
     * resolved, allowing dependant nodes to execute.
     *
     * This should never be called on any officially supported resource as it may lead to invalid state for any
     * subsequent node depending on them.
     *
     * @param resource The resource to resolve.
     */
    void resolveCustomResource(Resource resource);
}
