package manager;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    private Node head;
    private Node tail;

    private final HashMap<Integer, Node> nodeMap = new HashMap<>();

    @Override
    public void clear() {
        nodeMap.clear();
        head = null;
        tail = null;
    }

    @Override
    public void add(Task task) {
        if (task == null) return;

        remove(task.getId());

        Node node = new Node(task);
        linkLast(node);

        nodeMap.put(task.getId(), node);
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    private void linkLast(Node node) {
        if (tail == null) {
            head = tail = node;
        } else {
            node.prev = tail;
            tail.next = node;
            tail = node;
        }
    }

    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {

            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {

            tail = node.prev;
        }
        node.prev = null;
        node.next = null;
    }
}