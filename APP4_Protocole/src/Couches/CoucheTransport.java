package Couches;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;

/**
 * Implementation de la couche de transport.
 */
public class CoucheTransport extends Couche {
    private static CoucheTransport instance;

    private final char CODE_DEBUT = 'd';
    private final char CODE_FIN = 'f';
    private final char CODE_NORMAL = ' ';
    private final char CODE_ACK = 'a';
    private final char CODE_REENVOIE = 'r';

    private final int OFFSET = 11;
    private final int SIZE = 188;
    private final int SIZE_HEADER_POS = 9;
    private final int SEQ_HEADER_POS = 1;

    private int erreurs;

    private byte[][] TPDU;
    private Map<Integer, byte[]> bufferReception;
    private int finSequence = -1;

    public static CoucheTransport getInstance(){
        return instance == null ? instance = new CoucheTransport() : instance;
    }

    /**
     * Convertis les bytes en ASCII
     * @param data
     * @param size
     * @return
     */
    private byte[] convertIntToASCII(int data, int size) {
        String convertedInt = Integer.toString(data);
        byte[] convertedString = convertedInt.getBytes(StandardCharsets.US_ASCII);

        byte[] newData = new byte[size];
        fill(newData, (byte) '0');
        arraycopy(convertedString, 0, newData, size-convertedString.length, convertedString.length);

        return newData;
    }

    /**
     * Convertis les données ASCII en integer
     * @param data
     * @return
     */
    private int convertASCIItoInt(byte[] data) {
        String data_string = new String(data);
        String regex = "^0+(?!$)";
        data_string = data_string.replaceAll(regex, "");
        return Integer.parseInt(data_string);
    }

    /**
     * Recois un paquet de la couche Reseau, transmet le paquet a la couche Physique
     * @param PDU
     */
    @Override
    protected void recevoirDeCoucheSup(byte[] PDU) {
        int count = (int) Math.ceil((double) PDU.length / SIZE);
        TPDU = new byte[count][200];

        for (int i = 0; i < count; i++) {
            int taille = SIZE;
            if (i == count - 1){
                taille = PDU.length % SIZE;
            }

            arraycopy(PDU, i * SIZE, TPDU[i], OFFSET + 1, taille);

            char code = CODE_NORMAL;
            if (i == 0) {
                code = CODE_DEBUT;
            } else if (i == count - 1) {
                code = CODE_FIN;
            }

            TPDU[i][0] = (byte) code;
            arraycopy(convertIntToASCII(i, 8), 0, TPDU[i], SEQ_HEADER_POS, 8);
            arraycopy(convertIntToASCII(taille, 3), 0, TPDU[i], SIZE_HEADER_POS, 3);

            envoieVersCoucheInf(TPDU[i]);
        }
        System.out.println("Transmisssion des paquets termines");
    }


    /**
     * Recois un paquet de la couche Physique, le transmet a la couche Reseau
     * @param PDU
     * @throws ErreurDeTransmission
     */
    @Override
    protected void recevoirDeCoucheInf(byte[] PDU) throws ErreurDeTransmission {
        byte[] seq_bytes = Arrays.copyOfRange(PDU, SEQ_HEADER_POS, SIZE_HEADER_POS);
        byte[] size_bytes = Arrays.copyOfRange(PDU, SIZE_HEADER_POS, OFFSET+1);

        char code = (char) PDU[0];
        int seq = convertASCIItoInt(seq_bytes);
        int size = convertASCIItoInt(size_bytes);

        byte[] data_bytes = Arrays.copyOfRange(PDU, OFFSET + 1, OFFSET + 1 + size);

        switch (code) {
            case CODE_DEBUT:
                finSequence = -1;
                bufferReception = new HashMap<>();
                sauvegardePDU(0, data_bytes);
                break;

            case CODE_FIN:
                finSequence = seq;
                sauvegardePDU(seq, data_bytes);
                break;

            case CODE_NORMAL:
                sauvegardePDU(seq, data_bytes);
                break;

            case CODE_ACK:
                break;

            case CODE_REENVOIE:
                erreurs++;
                System.out.println("Reenvoye paquet: " + erreurs);
                break;
        }
        if (finSequence != -1) {
            System.out.println("Taille " + bufferReception.size() + " sequence fin " + finSequence);
            if (bufferReception.size() <= finSequence)
                return;
            int arrayL = (bufferReception.size() - 1) * SIZE + bufferReception.get(finSequence).length;
            byte[] bufferEnvoieCoucheSup = new byte[arrayL];
            int count = 0;
            for (Map.Entry<Integer, byte[]> key_value : bufferReception.entrySet()) {
                arraycopy(key_value.getValue(), 0, bufferEnvoieCoucheSup, count, key_value.getValue().length);
                count += key_value.getValue().length;
            }
            System.out.println("Erreurs: " + erreurs);
            envoieVersCoucheSup(bufferEnvoieCoucheSup);
        }
    }

    /**
     * Sauvegarde un paquet.
     * S'il manque un paquet, envois une requete de retransmission.
     * Si le paquet est recu, envois une confirmation.
     *
     * @param seq
     * @param data_bytes
     * @throws ErreurDeTransmission
     */
    private void sauvegardePDU(int seq, byte[] data_bytes) throws ErreurDeTransmission {
        if (seq != 0 && bufferReception.get(seq - 1) == null){
            erreurs++;
            if (erreurs >= 3)
                throw new ErreurDeTransmission("Il y a plus de 3 erreurs, la connection sera perdue.");
            byte[] rPDU = creePDUReenvoie(seq - 1);
            envoieVersCoucheInf(rPDU);
        }
        if (bufferReception.get(seq) != null)
            return;
        bufferReception.put(seq, data_bytes);
        byte[] ackPDU = creePDUAck(seq);
        envoieVersCoucheInf(ackPDU);
    }

    /**
     * Cree un paquet ack avec un opcode ack, un numero de sequence specifique
     * et aucune donnees
     * @param seq   Numero de sequence specifique.
     * @return      PDU qui contient l'approbation
     */
    private byte[] creePDUAck(int seq) {
        byte[] ackPDU = new byte[200];

        ackPDU[0] = (byte) CODE_ACK;
        arraycopy(convertIntToASCII(seq, 8), 0, ackPDU, SEQ_HEADER_POS, 8);
        arraycopy(convertIntToASCII(0, 3), 0, ackPDU, SIZE_HEADER_POS, 3);

        return ackPDU;
    }

    /**
     * Cree un paquet a renvoyer avec un opcode et un numero
     * de sequence specifique.
     * @param seq   Numero specifique au paquet demande.
     * @return      Le PDU qui contient la demande de renvois
     */
    private byte[] creePDUReenvoie(int seq) {
        int taille = 0;
        byte [] PDU_Reenvoie = new byte[200];

        PDU_Reenvoie[0] = (byte) CODE_REENVOIE;
        arraycopy(convertIntToASCII(seq, 8), 0, PDU_Reenvoie, SEQ_HEADER_POS, 8);
        arraycopy(convertIntToASCII(taille, 3), 0, PDU_Reenvoie, SIZE_HEADER_POS, 3);

        return PDU_Reenvoie;
    }
}
