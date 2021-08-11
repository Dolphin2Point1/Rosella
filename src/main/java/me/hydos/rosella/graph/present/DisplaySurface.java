package me.hydos.rosella.graph.present;

import me.hydos.rosella.graph.RenderGraph;

public interface DisplaySurface {

    /**
     * Creates a new present node for a graph.
     *
     * This function will validate the swapchain and acquire a image for presentation. If the swapchain is not optimal
     * it will be recreated. This function will block until a image can be acquired.
     *
     * Since a Image is acquired during this function a PresentNode must either be submitted or manually destroyed.
     * Failure to comply will lead to resource leaks and can cause the application to halt execution when the swapchain
     * needs to be recreated.
     *
     * @param graph That graph that will own the node
     */
    PresentNode createPresentNode(RenderGraph graph);

    /**
     * Destroys the instance and frees any resources associated with it.
     *
     * This function can be called at any time and previously created and submitted nodes can still be used.
     * The resources will be freed once all previous operations have completed.
     *
     * However no new node must be created after calling this function.
     */
    void destroy();
}
