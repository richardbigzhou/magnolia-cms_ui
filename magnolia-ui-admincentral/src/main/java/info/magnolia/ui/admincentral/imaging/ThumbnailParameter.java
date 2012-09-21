package info.magnolia.ui.admincentral.imaging;

import javax.jcr.Node;

/**
 * ThumbnailParameter.
 */
public class ThumbnailParameter {

    private final String variation;

    private Node node;

    public ThumbnailParameter(String variation, Node node) {

        this.variation = variation;
        this.node = node;
    }

    public String getVariation() {
        return variation;
    }

    public Node getNode() {
        return node;
    }

}