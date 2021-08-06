package me.hydos.rosella.graph.resources;

import me.hydos.rosella.util.NamedID;

/**
 *
 */
public record FramebufferAttachment(
    NamedID name,
    ImageSpec image
) {
}
