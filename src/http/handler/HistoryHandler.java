package http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = new Gson();

    public HistoryHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                List<Task> history = manager.getHistory();
                sendText(exchange, gson.toJson(history));
            } else {
                sendNotFound(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }
}