package me.hydos.rosella.graph.resources;

import kotlin.NotImplementedError;

/**
 *
 */
public record ImageSpec(
        int type,
        int width,
        int height,
        int depth,
        int mipLevels,
        int arrayLayers,
        int sampleCount,
        int format
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
