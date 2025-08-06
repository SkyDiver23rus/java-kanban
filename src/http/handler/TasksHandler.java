package http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/"); // /tasks or /tasks/{id}

            switch (method) {
                case "GET":
                    handleGet(exchange, parts);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, parts);
                    break;
                default:
                    sendNotFound(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) { // /tasks
            List<Task> tasks = manager.getAllTasks();
            sendText(exchange, gson.toJson(tasks));
        } else if (parts.length == 3) { // /tasks/{id}
            try {
                int id = Integer.parseInt(parts[2]);
                Task task = manager.getTaskById(id);
                if (task == null) {
                    sendNotFound(exchange, "Задача не найдена");
                } else {
                    sendText(exchange, gson.toJson(task));
                }
            } catch (NumberFormatException ex) {
                sendNotAcceptable(exchange, "Некорректный id");
            }
        } else {
            sendNotAcceptable(exchange, "Некорректный эндпоинт для GET");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Task task = gson.fromJson(reader, Task.class);
            Task created = manager.createTask(task);
            if (created == null) {
                sendServerError(exchange, "Задача пересекается по времени с другой задачей");
            } else {
                sendCreated(exchange);
            }
        } catch (Exception ex) {
            sendServerError(exchange, "Ошибка парсинга JSON: " + ex.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) { // /tasks
            manager.removeAllTasks();
            sendCreated(exchange);
        } else if (parts.length == 3) { // /tasks/{id}
            try {
                int id = Integer.parseInt(parts[2]);
                manager.removeTask(id);
                sendCreated(exchange);
            } catch (NumberFormatException ex) {
                sendNotAcceptable(exchange, "Некорректный id");
            }
        } else {
            sendNotAcceptable(exchange, "Некорректный эндпоинт для DELETE");
        }
    }
}