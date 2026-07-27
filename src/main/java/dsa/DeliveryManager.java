package dsa;

import java.util.Comparator;
import java.util.List;
import model.DeliveryPartner;
import model.Order;

/**
 * Manages the assignment of delivery partners using a CustomPriorityQueue.
 * The priority is given to the available partner with the highest rating.
 */
public class DeliveryManager {

    /**
     * Automatically assigns the best available delivery partner to the given order.
     * Uses CustomPriorityQueue based on Max-Heap of delivery partner ratings.
     *
     * @param order    the order to assign a partner to
     * @param partners the list of all delivery partners in the system
     * @return the assigned DeliveryPartner, or null if no partner is available
     */
    public static synchronized DeliveryPartner assignPartner(Order order, List<DeliveryPartner> partners) {
        if (order == null || partners == null || partners.isEmpty()) {
            return null;
        }

        // Max-heap: Comparator prioritizing higher ratings (p2 vs p1)
        CustomPriorityQueue<DeliveryPartner> priorityQueue = new CustomPriorityQueue<>(new Comparator<DeliveryPartner>() {
            @Override
            public int compare(DeliveryPartner p1, DeliveryPartner p2) {
                return Double.compare(p2.getRating(), p1.getRating());
            }
        });

        // Insert all available partners into the priority queue
        for (DeliveryPartner partner : partners) {
            if (partner.isAvailable()) {
                priorityQueue.add(partner);
            }
        }

        // Retrieve the highest-rated available partner
        DeliveryPartner bestPartner = priorityQueue.poll();
        if (bestPartner != null) {
            // Update partner status
            bestPartner.setAvailable(false);
            bestPartner.setCurrentOrderId(order.getOrderId());
            
            // Link order and partner
            order.setDeliveryPartnerId(bestPartner.getId());
            // Transition order status to READY (assigned)
            order.setStatus("READY");
        }

        return bestPartner;
    }
}
