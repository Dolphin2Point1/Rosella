package me.hydos.rosella.init.features;

import me.hydos.rosella.device.VulkanDevice;
import me.hydos.rosella.device.VulkanQueue;
import me.hydos.rosella.init.DeviceBuildConfigurator;
import me.hydos.rosella.init.DeviceBuildInformation;
import me.hydos.rosella.util.NamedID;

import java.util.concurrent.Future;

public class GraphTransfer extends ApplicationFeature {

    public static final NamedID NAME = new NamedID("rosella:graph_transfer");

    public GraphTransfer() {
        super(NAME);
    }

    @Override
    public Instance createInstance() {
        return new GraphTransferInstance();
    }

    private class GraphTransferInstance extends ApplicationFeature.Instance {
        @Override
        public void testFeatureSupport(DeviceBuildInformation meta) {
            this.canEnable = true;
        }

        @Override
        public Object enableFeature(DeviceBuildConfigurator meta) {
            return new GraphTransferFeatures(meta.addQueueRequest(0));
        }
    }

    public static GraphTransferFeatures getMetadata(VulkanDevice device) {
        Object o = device.getFeatureMeta(NAME);

        if(o == null) {
            return null;
        }

        if(!(o instanceof GraphTransferFeatures)) {
            throw new RuntimeException("Meta object could not be cast to GraphTransferFeatures");
        }
        return (GraphTransferFeatures) o;
    }

    public static record GraphTransferFeatures(Future<VulkanQueue> transferQueue) {
    }
}
