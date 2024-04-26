package Server;

import java.io.IOException;

/**
 * Cette classe permet de simuler un server.
 */
public class Server {
    /**
     * Methode pour simuler le serveur.
     *
     * @param args String port
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String port = args[0];
        InstanceServer instance = new InstanceServer(port);
        instance.DemarrageServer();
        System.out.println("Terminer");
        //System.out.println("Current Working Directory: " + System.getProperty("user.dir"));

    }
}