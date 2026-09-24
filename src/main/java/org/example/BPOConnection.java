package org.example;

import com.svi.bpo.api.entities.BpoElement;
import com.svi.bpo.api.entities.BpoNode;
import com.svi.bpo.api.entities.ProductionOutputUnit;
import com.svi.bpo.api.operations.BpoOperationResponse;
import com.svi.bpo.api.operations.impl.rest.requests.BpoConnector;
import com.svi.bpo.api.operations.impl.rest.requests.NodeOperationsRestRequest;
import com.svi.bpo.api.operations.impl.rest.requests.WorkerOperationsRestRequest;
import com.svi.bpo.api.operations.impl.rest.response.node.NodeListingRestResponse;
import com.svi.bpo.api.operations.impl.rest.response.worker.WorkerSingleElementRestResponse;

import java.util.List;
import java.util.Map;

public class BPOConnection {

    private final NodeOperationsRestRequest nodeOperations;
    private final WorkerOperationsRestRequest workerOperations;

    public BPOConnection(Config config) {

        BpoConnector bpoConnector = new BpoConnector(
                config.getBpoDomain(),
                config.getBpoPort(),
                config.getBpoContextRoot()
        );

        nodeOperations = new NodeOperationsRestRequest(bpoConnector);
        workerOperations = new WorkerOperationsRestRequest(bpoConnector);
    }

    public void listAllNodes() {

        NodeListingRestResponse response = nodeOperations.getNodeListing();

        if (!response.isSuccessful()) {
            System.err.println(
                    "Unable to connect to BPO. Error code: "
                            + response.getErrorCode()
            );
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

    public synchronized BpoElement getOneElement(String workerId, String nodeId) {

        System.out.println("Getting one element...");
        System.out.println("Worker: " + workerId);
        System.out.println("Node: " + nodeId);

        WorkerSingleElementRestResponse response =
                workerOperations.getElement(workerId, nodeId);

        if (!response.isSuccessful()) {
            System.err.println(
                    "Failed to get element. Error code: "
                            + response.getErrorCode()
            );
            return null;
        }

        BpoElement element = response.getElement();

        if (element == null) {
            System.out.println("No element returned.");
            return null;
        }

        System.out.println("Successfully retrieved element: "
                + element.getElementId());

        return element;
    }

    public boolean returnAndDropElement(
            String workerId,
            String nodeId,
            String elementId) {

        Map<String, ProductionOutputUnit> productionOutputUnits =
                new java.util.HashMap<>();

        BpoOperationResponse response =
                workerOperations.dropElement(
                        workerId,
                        nodeId,
                        elementId,
                        productionOutputUnits
                );

        if (!response.isSuccessful()) {
            System.err.println(
                    "Failed to return and drop element. Error code: "
                            + response.getErrorCode()
            );
            return false;
        }

        System.out.println(
                "Successfully returned and dropped "
                        + elementId
                        + " from "
                        + workerId
        );

        return true;
    }

    public boolean completeElementToNextNode(
            String workerId,
            String nodeId,
            String elementId,
            String nextNodeId,
            Map<String, ProductionOutputUnit> productionOutputUnits,
            Map<String, Object> extraDetails) {

        BpoOperationResponse response =
                workerOperations.completeElementToNextNode(
                        workerId,
                        nodeId,
                        elementId,
                        nextNodeId,
                        productionOutputUnits,
                        extraDetails
                );

        if (!response.isSuccessful()) {
            System.err.println(
                    "Failed to complete element. Error code: "
                            + response.getErrorCode()
            );
            return false;
        }

        return true;
    }

    public boolean completeElementToEnd(
            String workerId,
            String nodeId,
            String elementId,
            Map<String, ProductionOutputUnit> productionOutputUnits,
            Map<String, Object> extraDetails) {

        BpoOperationResponse response =
                workerOperations.completeElementToEnd(
                        workerId,
                        nodeId,
                        elementId,
                        productionOutputUnits,
                        extraDetails
                );

        if (!response.isSuccessful()) {
            System.err.println("Failed to complete element to end. Error code: " + response.getErrorCode());
            return false;
        }

        return true;
    }
}