package community.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import community.json.PermRequest;

import java.io.IOException;
import java.io.OutputStream;

public class PermissionHandler implements HttpHandler {
    private final PermissionService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public PermissionHandler(PermissionService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // parse JSON → PermRequest
        PermRequest req = mapper.readValue(exchange.getRequestBody(), PermRequest.class);

        boolean allowed = service.isAllowed(req);
        byte[] resp = allowed ? "OK".getBytes() : "Forbidden".getBytes();
        int code = allowed ? 200 : 403;

        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        exchange.sendResponseHeaders(code, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
    }
}
