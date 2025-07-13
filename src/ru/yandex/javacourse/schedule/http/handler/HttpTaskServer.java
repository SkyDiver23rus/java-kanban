package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer server;
    protected final TaskManager manager;
    private final int port;

    // Конструктор по умолчанию (8080)
    public HttpTaskServer() throws IOException {
        this(8080, Managers.getDefault());
    }

    // Конструктор с передачей менеджера (8080)
    public HttpTaskServer(TaskManager manager) throws IOException {
        this(8080, manager);
    }

    // Конструктор с указанием порта
    public HttpTaskServer(int port, TaskManager manager) throws IOException {
        this.manager = manager;
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/tasks", new TasksHandler(this.manager));
        server.createContext("/subtasks", new SubtasksHandler(this.manager));
        server.createContext("/epics", new EpicsHandler(this.manager));
        server.createContext("/history", new HistoryHandler(this.manager));
        server.createContext("/prioritized", new PrioritizedHandler(this.manager));
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + port);
    }

    public void stop() {
        server.stop(1);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }
}