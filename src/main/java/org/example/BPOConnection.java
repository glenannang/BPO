package org.example;
import com.svi.bpo.api.entities.BpoNode;
import com.svi.bpo.api.operations.impl.rest.requests.BpoConnector;
import com.svi.bpo.api.operations.impl.rest.requests.NodeOperationsRestRequest;
import com.svi.bpo.api.operations.impl.rest.response.node.NodeListingRestResponse;
import java.util.List;
public class BPOConnection {
    private static final String BPO_DOMAIN = "http://localhost";
    private static final String BPO_PORT = "8080";
    private static final String BPO_CONTEXT_ROOT = "BPO-5295-PROJECT";
    private final NodeOperationsRestRequest nodeOperations;
    public BPOConnection() {
        BpoConnector bpoConnector = new BpoConnector(BPO_DOMAIN, BPO_PORT, BPO_CONTEXT_ROOT);
        nodeOperations = new NodeOperationsRestRequest(bpoConnector);
    }
    public void listAllNodes() {
        NodeListingRestResponse response = nodeOperations.getNodeListing();
        if (!response.isSuccessful()) {
            System.err.println("Unable to connect to BPO. Error code: " + response.getErrorCode());
            return;
        }
        System.out.println("Successfully connected to BPO.");
        List<BpoNode> nodes = response.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            System.out.println("No nodes found.");
            return;
        }
        System.out.println("Nodes:");
        for (BpoNode node : nodes) {
            System.out.println(node);
        }
    }
    public static void main(String[] args) {
        new BPOConnection().listAllNodes();
    }
}
