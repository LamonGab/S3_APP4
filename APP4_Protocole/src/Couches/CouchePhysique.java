package Couches;

import java.io.IOException;
import java.net.*;

/**
 * Implementation de la couche physique.
 */
public class CouchePhysique extends Couche {
    /**
     * Adresse IP de destination
     */
    InetAddress adresse = null;
    /**
     * port de destination
     */
    int port = 0;
    /**
     * thread pour recevoir les donnees
     */
    protected ThreadReception thread;
    /**
     * Valeur pour les delais
     */
    public int delai = 0;
    /**
     * Quantite d'erreur du au delai
     */
    public int erreurDelai = -1;
    /**
     * Nombre de packet envoye
     */
    public int packetEnvoye = 0;
    /**
     * Instance de couchePhysique
     */
    private static CouchePhysique instance;
    private CouchePhysique(){}

    /**
     * Constructeur Singleton de la couche. Va creer ou aller chercher l'instance.
     *
     * @return instance de la couche
     */
    static public CouchePhysique getInstance(){
        return instance == null ? new CouchePhysique() : instance;
    }

    /**
     * Set l'adresse IP de destination.
     *
     * @param adresse IP de destination
     */
    public void setAdresseDestination(String adresse){
        try {
            this.adresse = InetAddress.getByName(adresse);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set le port de destination.
     *
     * @param port
     */
    public void setPortDestination(int port){
        this.port = port;
    }

    /**
     * Debute le thread et change son attribut running pour le mettre true.
     */
    public void start(){
        thread.running = true;
        thread.start();
    }

    /**
     * Termine le thread et change son attribut running pour false.
     */
    public void stop(){
        thread.running = false;
        thread.stop();
    }

    /**
     * Permet de savoir si le thread fonctionne.
     *
     * @return la valeur de thread.running
     */
    public boolean isThreadRunning() {
        return thread.running;
    }

    /**
     * {@inheritDoc}
     *
     * @param PDU
     */
    @Override
    protected void recevoirDeCoucheSup(byte[] PDU) {
        DatagramSocket socket = null;
        // Initiation d'un socket
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        // Change la valeur de packetEnvoye
        packetEnvoye++;
        if (packetEnvoye == erreurDelai)
            PDU[10] <<= 1;

        // Creation d'un packet
        DatagramPacket packet = new DatagramPacket(PDU, PDU.length, adresse, port);
        // Envoie le packet
        try {
            socket.send(packet);
            Thread.sleep(delai);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param PDU
     * @throws ErreurDeTransmission
     */
    @Override
    protected void recevoirDeCoucheInf(byte[] PDU) throws ErreurDeTransmission {
        envoieVersCoucheSup(PDU);
    }

    /**
     * Methode pour creer un thread.
     *
     * @param port
     * @throws IOException
     */
    public void createThreadReception(int port) throws IOException {
        this.thread = new ThreadReception(port, this);
    }

    /**
     * Classe pour le thread qui recevra les donnees
     */
    public class ThreadReception extends Thread {
        protected DatagramSocket socket = null;
        private CouchePhysique parent;
        public boolean running = true;

        /**
         * Constructeur du thread. Celui-ci cree egalement un socket au port en parametre.
         *
         * @param port
         * @param parent
         * @throws SocketException
         */
        public ThreadReception(int port, CouchePhysique parent) throws SocketException {
            super("FTP Thread Reception " + Math.random());
            socket = new DatagramSocket(port);
            this.parent = parent;
        }

        /**
         * Methode pour faire fonctionner le thread. Pendant qu'il run, le thread tente de recevoir un packet et il
         * l'envoie ensuite a la couche physique.
         */
        public void run(){
            while (running) {
                try {
                    byte[] buf = new byte[204];

                    //Recoit requete
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    //Envoie packet de donnee au parent
                    parent.recevoirDeCoucheInf(packet.getData());

                } catch (IOException | ErreurDeTransmission e) {
                    //throw new RuntimeException(e);
                    running = false;
                    socket.close();
                    System.out.println(e.getLocalizedMessage());
                }
            }
        }
    }
}
