package at.technikum.server;


import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import at.technikum.model.Media;
import at.technikum.model.User;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import at.technikum.repository.UserRepository;
import at.technikum.repository.MediaRepository;


public class Server {

    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    private final Map<String, Integer> tokens = new HashMap<>();

    // Constructor
    public Server(UserRepository userRepository, MediaRepository mediaRepository) {
        this.userRepository = userRepository;
        this.mediaRepository = mediaRepository;
    }

    public void start() throws IOException {

        // create HTTP-server on port 8080
        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        // Jackson: Java object <-> JSON
        ObjectMapper mapper = new ObjectMapper();

        // Create /media endpoint
        server.createContext("/media", exchange -> {

            String authorization = exchange.getRequestHeaders().getFirst("Authorization");

            if (authorization == null ||
                    !authorization.startsWith("Bearer ")) {

                exchange.sendResponseHeaders(401, -1);
                exchange.close();
                return;
            }

            String token = authorization.substring(7).trim();

            if (!tokens.containsKey(token)) {
                exchange.sendResponseHeaders(401, -1);
                exchange.close();
                return;
            }

            String method = exchange.getRequestMethod();

            // GET /media
            if (method.equals("GET")) {

                List<Media> mediaList = mediaRepository.readAll();

                // Java object -> JSON
                String response = mapper.writeValueAsString(mediaList);

                exchange.getResponseHeaders().set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                exchange.sendResponseHeaders(
                        200,
                        response.getBytes().length
                );

                exchange.getResponseBody().write(
                        response.getBytes()
                );

                exchange.getResponseBody().close();
            }

            // POST /media
            else if (method.equals("POST")) {

                Media media = mapper.readValue(exchange.getRequestBody(), Media.class);

                // Save to DB
                Media createdMedia = mediaRepository.create(media);

                // Media object -> JSON
                String response = mapper.writeValueAsString(createdMedia);

                exchange.getResponseHeaders().set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                exchange.sendResponseHeaders(
                        201,
                        response.getBytes().length
                );

                exchange.getResponseBody().write(
                        response.getBytes()
                );

                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        });

        // Create /users/register endpoint
        server.createContext("/users/register", exchange -> {

            String method = exchange.getRequestMethod();

            if (method.equals("POST")) {

                User user = mapper.readValue(exchange.getRequestBody(), User.class);

                User existingUser = userRepository.findByUsername(user.getUsername());

                if (existingUser != null) {
                    exchange.sendResponseHeaders(409, -1);
                    exchange.close();
                    return;
                }

                userRepository.create(user);

                exchange.sendResponseHeaders(201, -1);
                exchange.close();

            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        });

        // Create /users/login endpoint
        server.createContext("/users/login", exchange -> {

            String method = exchange.getRequestMethod();

            if (method.equals("POST")) {

                User loginUser = mapper.readValue(exchange.getRequestBody(), User.class);

                User user = userRepository.findByUsername(loginUser.getUsername());

                if (user == null) {
                    exchange.sendResponseHeaders(401, -1);
                    exchange.close();
                    return;
                }

                if (!user.getPassword().equals(loginUser.getPassword())) {
                    exchange.sendResponseHeaders(401, -1);
                    exchange.close();
                    return;
                }

                String token = UUID.randomUUID().toString();
                tokens.put(token, user.getId());

                String response = mapper.writeValueAsString(
                        java.util.Map.of("token", token)
                );

                exchange.getResponseHeaders().set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                exchange.sendResponseHeaders(
                        200,
                        response.getBytes().length
                );

                exchange.getResponseBody().write(
                        response.getBytes()
                );

                exchange.getResponseBody().close();

            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        });

        server.start();

        System.out.println("server started at :8080");
    }
}