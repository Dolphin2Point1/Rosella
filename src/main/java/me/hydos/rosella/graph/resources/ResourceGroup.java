package me.hydos.rosella.graph.resources;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.hydos.rosella.util.NamedID;

import java.util.Map;

/**
 *
 */
public class ResourceGroup {
    public final Map<NamedID, BufferResource> buffers = new Object2ObjectOpenHashMap<>();
    public final Map<NamedID, ImageResource> images = new Object2ObjectOpenHashMap<>();
}
