package me.hydos.rosella.graph.one_time_submit;

public class OTSNodeMetadata {

    private OTSNodeMetadata unionParent = null;
    private int unionRank = 0;

    public final OTSNode node;

    public OTSNodeMetadata(OTSNode node) {
        this.node = node;
    }

    public OTSNodeMetadata findUnionRoot() {
        if(this.unionParent != null) {
            OTSNodeMetadata root = this.unionParent.findUnionRoot();
            this.unionParent = root;
            return root;
        } else {
            return this;
        }
    }

    public void unionMerge(OTSNodeMetadata other) {
        OTSNodeMetadata root1 = this.findUnionRoot();
        OTSNodeMetadata root2 = other.findUnionRoot();

        if(root1 != root2) {
            if(root1.unionRank < root2.unionRank) {
                OTSNodeMetadata tmp = root1;
                root1 = root2;
                root2 = tmp;
            }

            root2.unionParent = root1;
            root1.unionRank++;
        }
    }
}
