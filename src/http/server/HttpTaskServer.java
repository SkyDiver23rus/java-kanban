package http.server;

import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import http.adapters.GsonDateAdapters;
import http.handler.*;
import manager.Managers;
import manager.TaskManager;


import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer server;
    protected final TaskManager manager;
    private final int port;

    public HttpTaskServer() throws IOException {
        this(8080, Managers.getDefault(), GsonDateAdapters.buildGson());
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this(8080, manager, GsonDateAdapters.buildGson());
    }

    public HttpTaskServer(int port, TaskManager manager) throws IOException {
        this(port, manager, GsonDateAdapters.buildGson());
    }

    public HttpTaskServer(int port, TaskManager manager, Gson gson) throws IOException {
        this.manager = manager;
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/tasks", new TasksHandler(this.manager, gson));
        server.createContext("/subtasks", new SubtasksHandler(this.manager, gson));
        server.createContext("/epics", new EpicsHandler(this.manager, gson));
        server.createContext("/history", new HistoryHandler(this.manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(this.manager, gson));
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