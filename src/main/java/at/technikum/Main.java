// mit ALLEN Befehlen für Datenbank abgeben
// dto?

package at.technikum;

import at.technikum.repository.JdbcMediaRepository;
import at.technikum.repository.JdbcUserRepository;
import at.technikum.repository.MediaRepository;
import at.technikum.repository.UserRepository;
import at.technikum.server.Server;

public class Main {

    public static void main(String[] args) throws Exception {

        UserRepository userRepository = new JdbcUserRepository();
        MediaRepository mediaRepository = new JdbcMediaRepository();

        Server server = new Server(userRepository, mediaRepository);

        server.start();
    }
}
