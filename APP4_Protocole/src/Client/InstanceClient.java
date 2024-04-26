package Client;

import Couches.*;

import java.io.IOException;

/**
 * Classe pour instancier un client et ses couches.
 */
public class InstanceClient {
    CouchePhysique couchePhysique;
    CoucheApplication coucheApplication;
    String nom_fichier;

    /**
     * constructeur de l'instance client
     * @param nom_fichier
     * @param ip_destination
     * @param port
     * @param ajout_erreurs
     * @throws IOException
     */
    public InstanceClient(String nom_fichier, String ip_destination, String port, boolean ajout_erreurs) throws IOException {
        this.nom_fichier = nom_fichier;
        creeInstanceClient(ip_destination, port, ajout_erreurs);
    }

    /**
     * Construit les couches et les associent ensemble.
     * @param ipDestination
     * @param port
     * @param ajoutErreurs
     * @throws IOException
     */
    private void creeInstanceClient(String ipDestination, String port, boolean ajoutErreurs) throws IOException {
        //Instanciation des couches
        CoucheTransport coucheTransport = CoucheTransport.getInstance();
        CoucheReseau coucheReseau = CoucheReseau.getInstance();
        CoucheLiaisonDeDonnees coucheLiaisonDeDonnees = CoucheLiaisonDeDonnees.getInstance();
        couchePhysique = CouchePhysique.getInstance();
        coucheApplication = CoucheApplication.getInstance();

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
        couchePhysique.erreurDelai = ajoutErreurs ? 10 : -1;
        couchePhysique.delai = 1;
        couchePhysique.setPortDestination(25002);
        couchePhysique.setAdresseDestination(ipDestination);
    }

    /**
     * Methode pour demarrer le client.
     * @throws IOException
     * @throws InterruptedException
     */
    public void DemarrageClient() throws IOException, InterruptedException {
        System.out.println("Demarrage Client");
        couchePhysique.start();
        coucheApplication.EnvoieFichier(nom_fichier);
    }
}
