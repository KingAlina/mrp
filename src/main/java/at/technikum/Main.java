// mit ALLEN Befehlen für Datenbank abgeben
// dto?

package at.technikum;

import at.technikum.services.database.UnitOfWork;


public class Main {

    public static void main(String[] args) throws Exception {

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            System.out.println("UnitOfWork connection works!");
        }
    }
}