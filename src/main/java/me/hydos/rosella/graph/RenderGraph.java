package me.hydos.rosella.graph;

/**
 *
 */
public interface RenderGraph {

    /**
     * <p> Destroys the graph and frees all resources owned by it. Some resources that are still used by other systems
     * or the application be not be freed immediately but it is guaranteed that they will be freed if the other system
     * or application properly releases them.</p>
     *
     * <p>Some graph types may not require this function to be called if they are submitted or otherwise processed. In
     * any case however a graph must never throw an error or end up in undefined state if this function is called
     * unnecessarily, multiple times or concurrently.</p>
     *
     * <p>After this function is called no call to any other function of this graph is allowed unless explicitly
     * stated.</p>
     */
    void destroy();
}
