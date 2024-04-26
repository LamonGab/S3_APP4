package Couches;

/**
 * Implementation de la couche reseau. Cette classe n'est pas utilise.
 */
public class CoucheReseau extends Couche {
    /**
     * Instance de couche reseau (Singleton)
     */
    private static CoucheReseau instance;

    /**
     * Methode pour aller chercher ou creer l'instance de couche reseau
     *
     * @return CoucheReseau instance
     */
    public static CoucheReseau getInstance(){
        return instance == null ? instance = new CoucheReseau() : instance;
    }

    /**
     * {@inheritDoc}
     *
     * @param PDU
     */
    @Override
    protected void recevoirDeCoucheSup(byte[] PDU) {
        envoieVersCoucheInf(PDU);
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
}
