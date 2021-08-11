package me.hydos.rosella.graph.present;

import me.hydos.rosella.LegacyRosella;
import me.hydos.rosella.graph.RenderGraph;

public class BasicDisplaySurface implements DisplaySurface {

    private final LegacyRosella engine;
    private final SurfaceProvider surfaceProvider;

    private long swapchain;

    public BasicDisplaySurface(LegacyRosella engine, SurfaceProvider surfaceProvider) {
        this.engine = engine;
        this.surfaceProvider = surfaceProvider;
        surfaceProvider.onAttach(engine, this);
    }

    private void rebuildSwapchain() {

    }

    @Override
    public PresentNode createPresentNode(RenderGraph graph) {
        return null;
        // TODO
    }

    @Override
    public void destroy() {
        // TODO
    }
}
