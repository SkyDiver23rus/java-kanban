package http.handler;

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
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            // /epics or /epics/{id}
            switch (method) {
                case "GET" -> handleGet(exchange, parts);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange, parts);
                case null, default -> sendNotFound(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) { // /epics
            List<Epic> epics = manager.getAllEpics();
            sendText(exchange, gson.toJson(epics));
        } else if (parts.length == 3) { // /epics/{id}
            try {
                int id = Integer.parseInt(parts[2]);
                Epic epic = manager.getEpicById(id);
                if (epic == null) {
                    sendNotFound(exchange, "Эпик не найден");
                } else {
                    sendText(exchange, gson.toJson(epic));
                }
            } catch (NumberFormatException ex) {
                sendNotAcceptable(exchange, "Некорректный id");
            }
        } else {
            sendNotAcceptable(exchange, "Некорректный эндпоинт для GET");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(reader, Epic.class);
        manager.createEpic(epic);
        sendCreated(exchange); // 201
    }

    private void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) { // /epics
            manager.removeAllEpic();
            sendText(exchange, "Все эпики удалены");
        } else if (parts.length == 3) { // /epics/{id}
            try {
                int id = Integer.parseInt(parts[2]);
                manager.removeEpic(id);
                sendText(exchange, "Эпик удалён");
            } catch (NumberFormatException ex) {
                sendNotAcceptable(exchange, "Некорректный id");
            }
        } else {
            sendNotAcceptable(exchange, "Некорректный эндпоинт для DELETE");
        }
    }
}