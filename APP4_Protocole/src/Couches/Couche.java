package Couches;

/**
 * Classe abstraite pour les couches. Contient un membre coucheSup et coucheInf.
 * Possède des méthodes pour set ses couches et recevoir ou envoyer des PDUs vers ses couches.
 */
public abstract class Couche {
    private Couche coucheSup;
    private Couche coucheInf;

    /**
     * Recoit un PDU de la couche superieur.
     *
     * @param PDU
     */
    protected abstract void recevoirDeCoucheSup(byte[] PDU);

    /**
     * Recoit un PDU de la couche inferieur.
     *
     * @param PDU
     * @throws ErreurDeTransmission
     */
    protected abstract void recevoirDeCoucheInf(byte[] PDU) throws ErreurDeTransmission;

    /**
     * Envoie un PDU vers la couche superieur
     *
     * @param PDU
     * @throws ErreurDeTransmission
     */
    protected void envoieVersCoucheSup(byte[] PDU) throws ErreurDeTransmission{
        coucheSup.recevoirDeCoucheInf(PDU);
    }

    /**
     * Envoie un PDU vers la couche inferieur
     *
     * @param PDU
     */
    protected void envoieVersCoucheInf(byte[] PDU){
        coucheInf.recevoirDeCoucheSup(PDU);
    }

    /**
     * Set la couche superieur.
     *
     * @param coucheSup
     */
    public void setCoucheSup(Couche coucheSup){
        this.coucheSup = coucheSup;
    }

    /**
     * Set la couche inferieur.
     *
     * @param coucheInf
     */
    public void setCoucheInf(Couche coucheInf){
        this.coucheInf = coucheInf;
    }
}
