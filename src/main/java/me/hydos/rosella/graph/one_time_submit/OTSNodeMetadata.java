package me.hydos.rosella.graph.one_time_submit;

import java.util.Set;

public class OTSNodeMetadata {

    private OTSNodeMetadata unionParent = null;
    private int unionRank = 0;

    private boolean shouldRun = false;

    private final Set<OTSNodeMetadata> dependencies;

    public final OTSNode node;

    public OTSNodeMetadata(OTSNode node, Set<OTSNodeMetadata> dependencies) {
        this.node = node;
        this.dependencies = dependencies;
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

    public void setRun() {
        if(!this.shouldRun) {
            this.shouldRun = true;

            if(this.dependencies != null) {
                for(OTSNodeMetadata node : this.dependencies) {
                    node.setRun();
                }
            }
        }
    }
}
