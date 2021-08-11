package me.hydos.rosella.scene.object;

import me.hydos.rosella.LegacyRosella;
import me.hydos.rosella.memory.MemoryCloseable;
import me.hydos.rosella.render.info.InstanceInfo;
import me.hydos.rosella.render.info.RenderInfo;

import java.util.concurrent.Future;

/**
 * Contains data for what you want to render
 */
public interface Renderable extends MemoryCloseable {

    /**
     * Called when the Application asked {@link LegacyRosella} to add this to the scene.
     *
     * @param rosella the common fields used by {@link LegacyRosella}
     */
    void onAddedToScene(LegacyRosella rosella);

    /**
     * Called when the swapchain needs to be resized
     *
     * @param rosella the instance of the {@link LegacyRosella} engine used.
     */
    void rebuild(LegacyRosella rosella);

    InstanceInfo getInstanceInfo();

    Future<RenderInfo> getRenderInfo();
}
