package dsa;

import java.util.ArrayList;
import java.util.List;
import model.Restaurant;

/**
 * A custom Merge Sort implementation to sort lists of Restaurant objects.
 * Demonstrates divide-and-conquer algorithm design.
 */
public class Sort {

    /**
     * Sorts the given list of restaurants using the Merge Sort algorithm
     * based on the specified criterion (name, rating, deliveryTime).
     *
     * @param list   the list of restaurants to be sorted
     * @param sortBy the field to sort by ("name", "rating", "deliveryTime")
     */
    public static void mergeSort(List<Restaurant> list, String sortBy) {
        if (list == null || list.size() < 2) {
            return;
        }

        int mid = list.size() / 2;
        List<Restaurant> left = new ArrayList<>(list.subList(0, mid));
        List<Restaurant> right = new ArrayList<>(list.subList(mid, list.size()));

        // Recursively sort both halves
        mergeSort(left, sortBy);
        mergeSort(right, sortBy);

        // Merge sorted halves
        merge(list, left, right, sortBy);
    }

    private static void merge(List<Restaurant> list, List<Restaurant> left, List<Restaurant> right, String sortBy) {
        int i = 0, j = 0, k = 0;

        while (i < left.size() && j < right.size()) {
            if (compare(left.get(i), right.get(j), sortBy) <= 0) {
                list.set(k++, left.get(i++));
            } else {
                list.set(k++, right.get(j++));
            }
        }

        while (i < left.size()) {
            list.set(k++, left.get(i++));
        }

        while (j < right.size()) {
            list.set(k++, right.get(j++));
        }
    }

    /**
     * Comparator logic for sorting.
     */
    private static int compare(Restaurant r1, Restaurant r2, String sortBy) {
        if ("rating".equalsIgnoreCase(sortBy)) {
            // Sort by rating (Descending: higher ratings first)
            return Double.compare(r2.getRating(), r1.getRating());
        } else if ("deliveryTime".equalsIgnoreCase(sortBy)) {
            // Sort by delivery time (Ascending: faster deliveries first)
            return Integer.compare(r1.getDeliveryTime(), r2.getDeliveryTime());
        } else {
            // Sort by name (Ascending: A-Z)
            return r1.getName().compareToIgnoreCase(r2.getName());
        }
    }
}
