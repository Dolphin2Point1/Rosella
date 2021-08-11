package me.hydos.rosella.graph.one_time_submit;

public interface OneTimeSubmitNode {

    /**
     * Called during the initialization of the one time submit process.
     * Will always be called before any other ots function.
     *
     * A node that does not support the one time submit process must throw a error.
     *
     * Nodes that will not execute may not have this function called on them. Nodes may not assume any ordering of
     * having this function called on them and this function may be called concurrently for different nodes.
     */
    void otsInit(OneTimeSubmitProcess process);
}
