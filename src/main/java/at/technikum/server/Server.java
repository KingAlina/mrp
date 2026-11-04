package at.technikum.server;

import at.technikum.dto.LoginRequest;
import at.technikum.dto.RegisterRequest;
import at.technikum.model.User;
import at.technikum.repository.UserRepository;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import at.technikum.model.Genre;
import at.technikum.model.Media;
import at.technikum.model.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import at.technikum.dto.CreateMediaRequest;


public class Server {

    private final UserRepository userRepository;

    private final Map<String, Integer> tokens = new HashMap<>();

    // Constructor
    public Server(UserRepository userRepository) {
        this.userRepository = userRepository;
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

            String token = exchange.getRequestHeaders().getFirst("Authorization");

            if (token == null || !tokens.containsKey(token)) {
                exchange.sendResponseHeaders(401, -1);
                exchange.close();
                return;
            }

            String method = exchange.getRequestMethod();

            // GET /media
            if (method.equals("GET")) {

                // later from DB
                Media media = new Media(
                        1,
                        "Interstellar",
                        "Science-Fiction Film",
                        MediaType.MOVIE,
                        2014,
                        List.of(Genre.SCIFI),
                        12
                );

                // Java object -> JSON
                String response = mapper.writeValueAsString(media);

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
                CreateMediaRequest request = mapper.readValue(
                        exchange.getRequestBody(), CreateMediaRequest.class
                );

                Media media = new Media(
                        request.getCreatorId(),
                        request.getTitle(),
                        request.getDescription(),
                        request.getMediaType(),
                        request.getReleaseYear(),
                        request.getGenres(),
                        request.getAgeRestriction()
                );

                // Media object -> JSON
                String response = mapper.writeValueAsString(media);

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

                RegisterRequest request = mapper.readValue(
                        exchange.getRequestBody(),
                        RegisterRequest.class
                );

                User existingUser =
                        userRepository.findByUsername(request.getUsername());

                if (existingUser != null) {
                    exchange.sendResponseHeaders(409, -1);
                    exchange.close();
                    return;
                }

                User user = new User(
                        request.getUsername(),
                        request.getPassword()
                );

                userRepository.save(user);

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

                LoginRequest request = mapper.readValue(
                        exchange.getRequestBody(),
                        LoginRequest.class
                );

                User user = userRepository.findByUsername(
                        request.getUsername()
                );

                if (user == null) {
                    exchange.sendResponseHeaders(401, -1);
                    exchange.close();
                    return;
                }

                if (!user.getPassword().equals(request.getPassword())) {
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