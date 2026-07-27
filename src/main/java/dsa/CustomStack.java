package dsa;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom implementation of a generic LIFO (Last-In, First-Out) Stack
 * using a singly linked list. This stack is thread-safe.
 * It is used for storing and displaying customer order history (newest first).
 *
 * @param <T> the type of elements held in this stack
 */
public class CustomStack<T> {

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> top;
    private int size;

    public CustomStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes an item onto the top of this stack.
     */
    public synchronized void push(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Stack does not support null values");
        }
        Node<T> newNode = new Node<>(item);
        newNode.next = top;
        top = newNode;
        size++;
    }

    /**
     * Removes the object at the top of this stack and returns that object.
     */
    public synchronized T pop() {
        if (isEmpty()) {
            return null;
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    /**
     * Looks at the object at the top of this stack without removing it.
     */
    public synchronized T peek() {
        if (isEmpty()) {
            return null;
        }
        return top.data;
    }

    /**
     * Checks if the stack is empty.
     */
    public synchronized boolean isEmpty() {
        return top == null;
    }

    /**
     * Returns the size of the stack.
     */
    public synchronized int size() {
        return size;
    }

    /**
     * Converts the stack elements to a standard list ordered from top to bottom (LIFO).
     * This is useful for serializing to JSON format for the front-end.
     */
    public synchronized List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> current = top;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }
}
