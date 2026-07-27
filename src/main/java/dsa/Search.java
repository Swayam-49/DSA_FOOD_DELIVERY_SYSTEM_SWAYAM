package dsa;

import java.util.ArrayList;
import java.util.List;
import model.Restaurant;

/**
 * A custom implementation of the Binary Search algorithm.
 * Used to search for a Restaurant by name in a sorted list.
 */
public class Search {

    /**
     * Searches for a restaurant by its exact name (case-insensitive) using Binary Search.
     * Prerequisite: The list is copied and sorted by name using Merge Sort before searching.
     *
     * @param list       the list of restaurants to search in
     * @param targetName the name of the restaurant to find
     * @return the found Restaurant object, or null if not found
     */
    public static Restaurant binarySearchByName(List<Restaurant> list, String targetName) {
        if (list == null || list.isEmpty() || targetName == null || targetName.trim().isEmpty()) {
            return null;
        }

        // 1. Create a copy and sort it by name to satisfy binary search requirements
        List<Restaurant> sortedList = new ArrayList<>(list);
        Sort.mergeSort(sortedList, "name");

        // 2. Perform Binary Search
        int low = 0;
        int high = sortedList.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            Restaurant midRestaurant = sortedList.get(mid);
            int cmp = midRestaurant.getName().compareToIgnoreCase(targetName.trim());

            if (cmp == 0) {
                return midRestaurant; // Exact match found
            } else if (cmp < 0) {
                low = mid + 1; // Search right half
            } else {
                high = mid - 1; // Search left half
            }
        }

        return null; // Restaurant not found
    }
}
