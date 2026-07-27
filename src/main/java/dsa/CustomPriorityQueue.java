package dsa;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * A custom implementation of a generic Priority Queue using a Binary Min-Heap.
 * It is thread-safe and allows passing a custom Comparator to define priorities.
 *
 * @param <T> the type of elements held in this priority queue
 */
public class CustomPriorityQueue<T> {

    private final ArrayList<T> heap;
    private final Comparator<? super T> comparator;

    public CustomPriorityQueue(Comparator<? super T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator cannot be null");
        }
        this.heap = new ArrayList<>();
        this.comparator = comparator;
    }

    /**
     * Inserts the specified element into the priority queue.
     */
    public synchronized void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Queue does not support null values");
        }
        heap.add(item);
        siftUp(heap.size() - 1);
    }

    /**
     * Retrieves and removes the head of this queue (highest priority / smallest element),
     * or returns null if the queue is empty.
     */
    public synchronized T poll() {
        if (isEmpty()) {
            return null;
        }
        T root = heap.get(0);
        T lastItem = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, lastItem);
            siftDown(0);
        }
        return root;
    }

    /**
     * Retrieves, but does not remove, the head of this queue, or returns null if empty.
     */
    public synchronized T peek() {
        if (isEmpty()) {
            return null;
        }
        return heap.get(0);
    }

    /**
     * Checks if the queue is empty.
     */
    public synchronized boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Returns the size of the priority queue.
     */
    public synchronized int size() {
        return heap.size();
    }

    /**
     * Clears all elements from the queue.
     */
    public synchronized void clear() {
        heap.clear();
    }

    /**
     * Bubble up element to restore heap invariant.
     */
    private void siftUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (comparator.compare(heap.get(index), heap.get(parentIndex)) >= 0) {
                break;
            }
            swap(index, parentIndex);
            index = parentIndex;
        }
    }

    /**
     * Bubble down element to restore heap invariant.
     */
    private void siftDown(int index) {
        int half = heap.size() / 2;
        while (index < half) {
            int leftChildIndex = 2 * index + 1;
            int rightChildIndex = leftChildIndex + 1;
            int smallestIndex = leftChildIndex;

            if (rightChildIndex < heap.size() && comparator.compare(heap.get(rightChildIndex), heap.get(leftChildIndex)) < 0) {
                smallestIndex = rightChildIndex;
            }

            if (comparator.compare(heap.get(index), heap.get(smallestIndex)) <= 0) {
                break;
            }

            swap(index, smallestIndex);
            index = smallestIndex;
        }
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
