package org.example;

import com.svi.bpo.api.entities.BpoElement;
import com.svi.bpo.api.entities.ProductionOutputUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SleepProcessor {

    private final Config config;
    private final BPOConnection bpoConnection;

    public SleepProcessor(Config config) {
        this.config = config;
        this.bpoConnection = new BPOConnection(config);
    }

    public void processWorker(String workerId) {

        System.out.println(
                workerId + " started processing " + config.getNodeId()
        );

        while (!Thread.currentThread().isInterrupted()) {

            /*
             * Get the next available element from the node.
             */
            BpoElement element =
                    bpoConnection.getOneElement(
                            workerId,
                            config.getNodeId()
                    );

            /*
             * If there is currently no element available,
             * wait before checking the node again.
             *
             * This allows all node processors to remain
             * running at the same time while waiting for
             * elements from the previous node.
             */
            if (element == null) {

                System.out.println(
                        workerId
                                + " found no available element. "
                                + "Waiting before trying again..."
                );

                try {

                    Thread.sleep(2000);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;
                }

                continue;
            }

            /*
             * Process the element.
             *
             * If processing or completion fails,
             * stop this worker so it does not retrieve
             * another element while the current element
             * may still be assigned to it.
             */
            boolean successful =
                    processElement(workerId, element);

            if (!successful) {

                System.err.println(
                        workerId
                                + " stopped because the current element failed."
                );

                break;
            }
        }

        System.out.println(
                workerId + " stopped."
        );
    }

    private boolean processElement(
            String workerId,
            BpoElement element) {

        String elementId =
                element.getElementId();

        try {

            System.out.println();
            System.out.println("================================");
            System.out.println("Processing Element");
            System.out.println("================================");
            System.out.println("Element: " + elementId);
            System.out.println("Worker: " + workerId);
            System.out.println("Node: " + config.getNodeId());

            /*
             * Generate the first random number
             * from 1 to 20.
             */
            int firstNumber =
                    ThreadLocalRandom.current()
                            .nextInt(1, 21);

            System.out.println(
                    "First number: " + firstNumber
            );

            /*
             * Sleep based on the first random number.
             */
            System.out.println(
                    "Sleeping for "
                            + firstNumber
                            + " seconds..."
            );

            Thread.sleep(
                    firstNumber * 1000L
            );

            System.out.println(
                    "Finished sleeping."
            );

            /*
             * Generate the second random number
             * from 1 to 20.
             */
            int secondNumber =
                    ThreadLocalRandom.current()
                            .nextInt(1, 21);

            /*
             * Add the two generated numbers.
             */
            int total =
                    firstNumber + secondNumber;

            System.out.println(
                    "Second number: " + secondNumber
            );

            System.out.println(
                    "Total: " + total
            );

            /*
             * --------------------------------
             * Production Output Units
             * --------------------------------
             */

            Map<String, ProductionOutputUnit> productionOutputUnits =
                    new HashMap<>();

            /*
             * SLEEP POU
             *
             * The first number represents the
             * actual sleeping time in seconds.
             */
            ProductionOutputUnit sleepOutput =
                    new ProductionOutputUnit();

            sleepOutput.setOutputCount(
                    firstNumber
            );

            sleepOutput.setErrorCount(
                    0
            );

            productionOutputUnits.put(
                    "SLEEP",
                    sleepOutput
            );

            /*
             * ELEMENT POU
             *
             * One element was processed.
             */
            ProductionOutputUnit elementOutput =
                    new ProductionOutputUnit();

            elementOutput.setOutputCount(
                    1
            );

            elementOutput.setErrorCount(
                    0
            );

            productionOutputUnits.put(
                    "ELEMENT",
                    elementOutput
            );

            /*
             * --------------------------------
             * Extra Details
             * --------------------------------
             */

            Map<String, Object> extraDetails =
                    new HashMap<>();

            extraDetails.put(
                    "first_number",
                    firstNumber
            );

            extraDetails.put(
                    "second_number",
                    secondNumber
            );

            extraDetails.put(
                    "total",
                    total
            );

            /*
             * --------------------------------
             * Determine Destination
             * --------------------------------
             */

            boolean successful;

            /*
             * Over-sleepers from any processing node
             * are sent to the Over-Sleeper Archive Node.
             */
            if (total >=
                    config.getOverSleeperMinimumInSeconds()) {

                String destinationNode =
                        config.getOverSleeperNode();

                System.out.println(
                        "OVER-SLEEPER detected."
                );

                System.out.println(
                        "Destination Node: "
                                + destinationNode
                );

                successful =
                        bpoConnection.completeElementToNextNode(
                                workerId,
                                config.getNodeId(),
                                elementId,
                                destinationNode,
                                productionOutputUnits,
                                extraDetails
                        );

                /*
                 * NODE4 is the final normal processing node.
                 * Normal elements are therefore completed
                 * to the end of the workflow.
                 */
            } else if ("NODE4".equalsIgnoreCase(
                    config.getNodeId())) {

                System.out.println(
                        "Normal processing."
                );

                System.out.println(
                        "Destination: END"
                );

                successful =
                        bpoConnection.completeElementToEnd(
                                workerId,
                                config.getNodeId(),
                                elementId,
                                productionOutputUnits,
                                extraDetails
                        );

                /*
                 * For NODE1, NODE2, and NODE3,
                 * normal elements proceed to the
                 * configured next node.
                 */
            } else {

                String destinationNode =
                        config.getNextNodeId();

                System.out.println(
                        "Normal processing."
                );

                System.out.println(
                        "Destination Node: "
                                + destinationNode
                );

                successful =
                        bpoConnection.completeElementToNextNode(
                                workerId,
                                config.getNodeId(),
                                elementId,
                                destinationNode,
                                productionOutputUnits,
                                extraDetails
                        );
            }

            /*
             * If completion succeeds, the worker
             * may safely retrieve another element.
             */
            if (successful) {

                if ("NODE4".equalsIgnoreCase(config.getNodeId())
                        && total <
                        config.getOverSleeperMinimumInSeconds()) {

                    System.out.println(
                            workerId
                                    + " completed "
                                    + elementId
                                    + " -> END"
                    );

                } else {

                    String destinationNode =
                            total >=
                                    config.getOverSleeperMinimumInSeconds()
                                    ? config.getOverSleeperNode()
                                    : config.getNextNodeId();

                    System.out.println(
                            workerId
                                    + " completed "
                                    + elementId
                                    + " -> "
                                    + destinationNode
                    );
                }

                System.out.println(
                        "================================"
                );

                return true;
            }

            /*
             * If completion fails, stop the worker
             * instead of retrieving another element.
             */
            System.err.println(
                    workerId
                            + " failed to complete "
                            + elementId
            );

            System.err.println(
                    "Stopping "
                            + workerId
                            + " to prevent getting another element."
            );

            System.out.println(
                    "================================"
            );

            return false;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.err.println(
                    workerId
                            + " was interrupted while processing "
                            + elementId
            );

            return false;
        }
    }
}