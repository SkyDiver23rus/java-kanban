package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = new Gson();

    public EpicsHandler(TaskManager manager) {
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
                        List<Epic> epics = manager.getAllEpics();
                        sendText(exchange, gson.toJson(epics));
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.replace("id=", ""));
                        Epic epic = manager.getEpicById(id);
                        if (epic == null) {
                            sendNotFound(exchange, "Эпик не найден");
                        } else {
                            sendText(exchange, gson.toJson(epic));
                        }
                    } else {
                        sendNotFound(exchange, "Некорректный запрос");
                    }
                    break;
                case "POST":
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                    Epic epic = gson.fromJson(reader, Epic.class);
                    manager.createEpic(epic);
                    sendText(exchange, gson.toJson(epic));
                    break;
                case "DELETE":
                    if (query == null) {
                        manager.removeAllEpic();
                        sendText(exchange, "Все эпики удалены");
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.replace("id=", ""));
                        manager.removeEpic(id);
                        sendText(exchange, "Эпик удалён");
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