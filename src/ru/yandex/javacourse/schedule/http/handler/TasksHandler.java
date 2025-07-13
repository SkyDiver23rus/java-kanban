package ru.yandex.javacourse.schedule.http.handler;

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
    private final Gson gson = new Gson();

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query == null) {
                        List<Task> tasks = manager.getAllTasks();
                        sendText(exchange, gson.toJson(tasks));
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.replace("id=", ""));
                        Task task = manager.getTaskById(id);
                        if (task == null) {
                            sendNotFound(exchange, "Задача не найдена");
                        } else {
                            sendText(exchange, gson.toJson(task));
                        }
                    } else {
                        sendNotFound(exchange, "Некорректный запрос");
                    }
                    break;
                case "POST":
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
                    break;
                case "DELETE":
                    if (query == null) {
                        manager.removeAllTasks();
                        sendCreated(exchange);
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.replace("id=", ""));
                        manager.removeTask(id);
                        sendCreated(exchange);
                    } else {
                        sendNotFound(exchange, "Некорректный запрос");
                    }
                    break;
                default:
                    sendNotFound(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }
}