package Server;
import Couches.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Classe pour instancier un serveur et ses couches.
 */
public class InstanceServer {
    private CouchePhysique couchePhysique;

    /**
     * constructeur de l'instanceServer
     * @param port
     * @throws IOException
     */
    public InstanceServer(String port) throws IOException {
        constructionInstanceServer(port);
    }

    /**
     * Construit les couches et les associent ensemble.
     * @param port
     * @throws IOException
     */
    private void constructionInstanceServer(String port) throws IOException {
        //Instanciation des couches
        CoucheApplication coucheApplication = CoucheApplication.getInstance();
        CoucheTransport coucheTransport = CoucheTransport.getInstance();
        CoucheReseau coucheReseau = CoucheReseau.getInstance();
        CoucheLiaisonDeDonnees coucheLiaisonDeDonnees = CoucheLiaisonDeDonnees.getInstance();
        couchePhysique = CouchePhysique.getInstance();

        //Liaisons des couches
        couchePhysique.setCoucheSup(coucheLiaisonDeDonnees);
        coucheLiaisonDeDonnees.setCoucheInf(couchePhysique);
        coucheLiaisonDeDonnees.setCoucheSup(coucheReseau);
        coucheReseau.setCoucheInf(coucheLiaisonDeDonnees);
        coucheReseau.setCoucheSup(coucheTransport);
        coucheTransport.setCoucheInf(coucheReseau);
        coucheTransport.setCoucheSup(coucheApplication);
        coucheApplication.setCoucheInf(coucheTransport);

        couchePhysique.createThreadReception(Integer.parseInt(port));
        couchePhysique.setAdresseDestination("localhost");
        couchePhysique.setPortDestination(25001);
    }

    /**
     * Methode pour demarrer le serveur. Permet de fermer le serveur en entrant 'q'.
     *
     * @throws IOException
     */
    public void DemarrageServer() throws IOException {
        couchePhysique.start();
        System.out.print("Serveur initialise: ");
        System.out.println("Q pour terminer le thread");
        System.out.println("Entrer");
        while (couchePhysique.isThreadRunning()) {
            int command = System.in.read();
            switch (command) {
                case 113:
                case 81:
                    System.exit(0);
                    break;
            }
        }
    }

}
