package dsa;

/**
 * A custom implementation of a generic FIFO (First-In, First-Out) Queue
 * using a singly linked list. This queue is thread-safe using synchronization,
 * making it suitable for multi-threaded HTTP server requests.
 *
 * @param <T> the type of elements held in this queue
 */
public class CustomQueue<T> {
    
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public CustomQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Inserts the specified element at the tail of the queue.
     */
    public synchronized void enqueue(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Queue does not support null values");
        }
        Node<T> newNode = new Node<>(item);
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Retrieves and removes the head of the queue, or returns null if the queue is empty.
     */
    public synchronized T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T data = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return data;
    }

    /**
     * Retrieves, but does not remove, the head of this queue, or returns null if empty.
     */
    public synchronized T peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    /**
     * Checks if the queue is empty.
     */
    public synchronized boolean isEmpty() {
        return head == null;
    }

    /**
     * Returns the number of elements in the queue.
     */
    public synchronized int size() {
        return size;
    }
}
