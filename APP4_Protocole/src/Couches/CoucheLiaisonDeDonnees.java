package Couches;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.CRC32;
import static java.lang.System.arraycopy;

/**
 * Implementation de la couche liaison de donnees.
 */
public class CoucheLiaisonDeDonnees extends Couche {
    private int erreursCrc = 0;
    private int paquetsRecus = 0;
    private int paquetsTransmis = 0;

    // Singleton
    private static CoucheLiaisonDeDonnees instance;
    private CoucheLiaisonDeDonnees() {}
    static public CoucheLiaisonDeDonnees getInstance() {
        return instance == null ? new CoucheLiaisonDeDonnees() : instance;
    }

    /**
     * Initialise les données du singleton
     */
    public void Reset() {
        erreursCrc = 0;
        paquetsRecus = 0;
        paquetsTransmis = 0;
    }


    /**
     * Recois des données de la couche Physique, evalue la valeur du crc, jete le paquet si le
     * paquet n'est pas bon, le transmet si le paquet est bon.
     * @param PDU   Paquet de la couche physique
     */
    @Override
    protected void recevoirDeCoucheInf(byte[] PDU) throws ErreurDeTransmission {
        // Extract data from PDU
        byte[] paquet = new byte[PDU.length - 4];
        arraycopy(PDU, 4, paquet, 0, paquet.length);

        // Calculate & Check CRC
        CRC32 crc = new CRC32();
        crc.update(paquet);
        int valeurCrc = (int) crc.getValue();
        int oldCRC = (((int) PDU[0] << 24) & 0xFF000000) | (((int) PDU[1] << 16) & 0x00FF0000) | (((int) PDU[2] << 8) & 0x0000FF00) | ((int) PDU[3] & 0x000000FF);
        if (valeurCrc != oldCRC) {
            System.out.println("Error CRC32");
            erreursCrc++;
        }


        paquetsRecus++;
        logInfo("Envoie du paquet numero: " + paquetsRecus);
        envoieVersCoucheSup(paquet);
    }

    /**
     * Recois un paquet a transmettre de la couche reseau, ajoute un crc 32-bit
     * devant le paquet et le transmet a la couche physique.
     * @param PDU
     */
    @Override
    protected void recevoirDeCoucheSup(byte[] PDU) {
        byte[] trame = new byte[PDU.length + 4];

        CRC32 crc = new CRC32();
        crc.update(PDU);
        long valeurCrc = crc.getValue();
        byte[] CRCBytes = new byte[] {
                (byte) (valeurCrc >> 24),
                (byte) (valeurCrc >> 16),
                (byte) (valeurCrc >> 8),
                (byte) valeurCrc};
        arraycopy(CRCBytes, 0, trame, 0, CRCBytes.length);

        arraycopy(PDU, 0, trame, 4, PDU.length);

        paquetsTransmis++;
        logInfo("Reception du paquet numero: " + paquetsTransmis);
        envoieVersCoucheInf(trame);
    }

    /**
     * Cette methode log avec la date et l'heure le string passe en parametre.
     *
     * @param message
     */
    private void logInfo(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter("liaisonDeDonnes.log", true))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());
            out.println(timestamp + " - " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
