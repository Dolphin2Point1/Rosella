package me.hydos.rosella.graph.one_time_submit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class OTSNodeMetadata {

    private OTSNodeMetadata unionParent = null;
    private int unionRank = 0;

    private boolean shouldRun = false;

    public final Set<OTSNodeMetadata> parents;

    public final OTSRenderGraph graph;
    public final OTSNode node;

    public final boolean canRunInsideRenderPass = true;

    public final int supportedQueueFamilies;

    public long renderPassAssignment = -1;

    /**
     * A bitmask of all render passes that need to finish execution before this node can execute. If this node is part
     * of a render pass that render pass is not included.
     */
    public long renderPassDependencies;

    /**
     * Creates a new node metadata and updates the union find datasturcture of the graph.
     *
     * @param node
     * @param parents
     */
    public OTSNodeMetadata(
            @NotNull OTSRenderGraph graph,
            @NotNull OTSNode node,
            @Nullable Set<OTSNodeMetadata> parents,
            int queueFamilies) {

        this.graph = graph;
        this.node = node;
        this.parents = parents;

        this.supportedQueueFamilies = queueFamilies;

        if(this.parents != null) {
            try {
                graph.lock.lock();

                long renderPasses = 0;
                for (OTSNodeMetadata other : parents) {
                    renderPasses |= other.renderPassDependencies;
                    this.unionMerge(other);
                }
                this.renderPassDependencies = renderPasses;

            } finally {
                graph.lock.unlock();
            }
        } else {
            this.renderPassDependencies = 0;
        }
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

    public boolean shouldRun() {
        return this.shouldRun;
    }

    /**
     * Recursively sets this and all nodes that this node depends on as should run.
     */
    public void setRun() {
        try {
            graph.lock.lock();
            this.setRunRec();
        } finally {
            graph.lock.unlock();
        }
    }

    private void setRunRec() {
        if(!this.shouldRun) {
            this.shouldRun = true;

            if(this.parents != null) {
                for(OTSNodeMetadata node : this.parents) {
                    node.setRun();
                }
            }
        }
    }

    private void unionMerge(OTSNodeMetadata other) {
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
