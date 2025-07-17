package http.adapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.Duration;

public class GsonDateAdapters {
    public static Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return LocalDateTime.parse(json.getAsString());
            }
        });
        builder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.toString());
            }
        });

        builder.registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
            @Override
            public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return Duration.parse(json.getAsString());
            }
        });
        builder.registerTypeAdapter(Duration.class, new JsonSerializer<Duration>() {
            @Override
            public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.toString());
            }
        });

        return builder.create();
    }
}