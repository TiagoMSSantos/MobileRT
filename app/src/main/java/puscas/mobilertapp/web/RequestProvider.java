package puscas.mobilertapp.web;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;
import androidx.annotation.NonNull;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import org.jetbrains.annotations.Nullable;
import puscas.mobilertapp.utils.UtilsLogging;
import puscas.mobilertapp.web.pojos.ResponseCreateConsumer;

public class RequestProvider {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(RequestProvider.class.getName());

    /**
     * The Kafka consumer id.
     */
    private static ResponseCreateConsumer responseCreateConsumer = null;

    /**
     * The JSON object mapper.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The Bitmap.
     */
    private static final Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

    /**
     * List Kafka topics.
     *
     * @param context The Android {@link Context}.
     */
    public static void listTopics(@NonNull final Context context) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(context);

        final String url = "http://192.168.1.138:8082/topics";
        final RequestFuture<String> future = RequestFuture.newFuture();

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(
            Request.Method.GET,
            url,
            response -> LOGGER.info("Response is: " + response),
            error -> LOGGER.info("That didn't work: " + error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String>  params = new HashMap<>();
                params.put("Content-Type", "application/vnd.kafka.json.v2+json");
                return params;
            }
        };

        future.setRequest(stringRequest);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        queue.start();

        try {
            final String text = future.get(10L, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
            UtilsLogging.logThrowable(ex, "listTopics");
        }
    }

    /**
     * Create consumer.
     *
     * @param context The Android {@link Context}.
     */
    public static void createConsumerInConsumerGroup(@NonNull final Context context) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(context);
        final String url = "http://192.168.1.138:8082/consumers/group_name";
        final RequestFuture<String> future = RequestFuture.newFuture();

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(
            Request.Method.POST,
            url,
            response -> {
                try {
                    responseCreateConsumer = objectMapper.readValue(response, new TypeReference<ResponseCreateConsumer>() { });
                } catch (final JsonProcessingException ex) {
                    LOGGER.warning(ex.getMessage());
                }
                LOGGER.info("Response is: " + response);
            },
            error -> LOGGER.info("That didn't work!")
        ) {
            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String>  params = new HashMap<>();
                params.put("Content-Type", "application/vnd.kafka.jsonschema.v2+json");
                return params;
            }
        };

        future.setRequest(stringRequest);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        queue.start();

        try {
            final String text = future.get(10L, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
        }
    }

    /**
     * Subscribe consumer to topic.
     *
     * @param context The Android {@link Context}.
     */
    public static void subscribeConsumerToTopic(@NonNull final Context context) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(context);
        final String url = "http://192.168.1.138:8082/consumers/group_name_test/instances/"
            + responseCreateConsumer.getInstanceId() + "/subscription";

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(
            Request.Method.GET,
            url,
            response -> LOGGER.info("Response is: " + response),
            error -> LOGGER.info("That didn't work!")
        ) {
            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String>  params = new HashMap<>();
                params.put("Content-Type", "application/vnd.kafka.v2+json");
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /**
     * Produce message to topic.
     *
     * @param context The Android {@link Context}.
     */
    public static void produceMessageToTopic(@NonNull final Context context) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(context);
        final String url = "http://192.168.1.138:8082/topics/test1";


        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(
            Request.Method.POST,
            url,
            response -> LOGGER.info("Response is: " + response),
            error -> LOGGER.info("That didn't work: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String>  params = new HashMap<>();
                params.put("Content-Type", "application/vnd.kafka.json.v2+json");
                return params;
            }

            @Override
            public @Nullable byte[] getBody() {
                final String serializedBitmap = serializeBitmap();
                final String httpPostBody = "{\n" +
                    "  \"value_schema\": \"{\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"name\\\":{\\\"type\\\":\\\"string\\\"}}}\",\n" +
                    "  \"records\": [\n" +
                    "    {\n" +
                    "      \"value\": {\n" +
                    "      \"name\": \"testUser\",\n" +
                    "      \"bitmap\": \"" + serializedBitmap + "\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
                final byte[] bytes = httpPostBody.getBytes(Charset.forName("UTF-32"));
                return bytes;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /**
     * Serialize Bitmap.
     *
     * @return The bitmap serialized.
     */
    private static String serializeBitmap () {
        bitmap.eraseColor(Color.RED);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        final byte[] byteArray = byteArrayOutputStream .toByteArray();
        final String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        return encoded;
    }
}
