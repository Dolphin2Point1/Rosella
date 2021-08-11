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

    /**
     * Called during the scan phase of a one time submit process.
     *
     * A node must resolve all of its resources using the provided info. Not resolving any resource will cause
     * compilation to fail.
     *
     * @param phase
     * @return The execution requirements of the node.
     */
    OTSNodeExecutionRequirements otsScan(OTSScanPhase phase);

    /**
     * Called during the record phase of a one time submit process.
     *
     * After this function returns a node must be in a state such that it can be abandoned without calling its destroy
     * function and does not leak any resources. State that needs to persist after this function can be added to the
     * provided configuration object.
     */
    void otsRecord();


}
