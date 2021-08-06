package me.hydos.rosella.graph.resources;

import kotlin.NotImplementedError;

import java.util.Set;

public record FramebufferSpec(
        int width,
        int height,
        int layers,
        Set<FramebufferAttachment> attachments
) {

    /**
     * Tests if the spec is valid and supported by the provided device.
     *
     * @return True if the spec is valid and the device supports it.
     */
    public boolean isValid() {
        throw new NotImplementedError();
    }
}
