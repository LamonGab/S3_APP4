package Client;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Cette classe permet de simuler un client.
 */
public class Client {
    /**
     * Methode pour simuler un client.
     *
     * @param args String nom de fichier, String ip de destination, String port de destination, bool pour simuler des erreurs
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String nom_fichier = args[0];
        String ip_destination = args[1];
        String port = args[2];
        boolean ajout_erreurs = Boolean.parseBoolean(args[3]);

        InstanceClient instance = new InstanceClient(nom_fichier, ip_destination, port, ajout_erreurs);
        instance.DemarrageClient();
    }
}
