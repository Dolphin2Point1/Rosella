package me.hydos.rosella.graph.one_time_submit;

public class OTSFramebufferMeta {

    public final OTSNode node;
    public final int renderPassID;

    public OTSFramebufferMeta(OTSNode node, int renderPassID) {
        this.node = node;
        this.renderPassID = renderPassID;
    }
}
