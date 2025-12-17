package projet.exception;

/**
 * Exception de base pour tout le projet de gestion de réseau électrique.
 * Elle permet de centraliser la gestion des erreurs (Fichier, Menu, Logique).
 */
public class ReseauException extends Exception {
	private static final long serialVersionUID = 1L;
	/**
	 * Construit une exception générique avec un message simple.
	 * Ajoute automatiquement le préfixe "Erreur: ".
	 *
	 * @param message Le détail de l'erreur
	 */
	public ReseauException(String message) {
        super("Erreur: " + message);
    }

	/**
	 * Construit une exception contextuelle.
	 * Si numLigne > 0, affiche "Erreur ligne X : ...".
	 * Si numLigne == 0, affiche simplement "Erreur : ...".
	 *
	 * @param message Le détail de l'erreur
	 * @param numLigne Le numéro de ligne dans le fichier (ou 0 pour le mode manuel)
	 */
    public ReseauException(String message, int numLigne) {
    	super((numLigne == 0) ? "Erreur : " + message : "Erreur ligne " + numLigne + " : " + message);    
    }
    

    /**
     * Signale une erreur de format ou de syntaxe lors de la lecture du fichier.
     * (ex: parenthèses manquantes, point-virgule, nombre d'arguments incorrect).
     */
    public static class Syntaxe extends ReseauException {
    	private static final long serialVersionUID = 1L;
    	/**
    	 * Construit une erreur de format précise.
    	 * Message généré : "Format [element] incorrect. Attendu : [formatAttendu]."
    	 *
    	 * @param element Le nom de l'élément en erreur (ex: "generateur")
    	 * @param formatAttendu Le format qu'on attendait (ex: "generateur(nom,capacite)")
    	 * @param ligne Le numéro de la ligne en erreur
    	 */
        public Syntaxe(String element, String formatAttendu, int ligne) {
            super("Format " + element + " incorrect. Attendu : " + formatAttendu + ".", ligne);
        }
        
        /**
         * Construit une erreur de syntaxe générique.
         *
         * @param message Le message d'erreur
         * @param ligne Le numéro de la ligne en erreur
         */
        public Syntaxe(String message, int ligne) {
            super(message, ligne);
        }
    }

    /**
     * Signale qu'une donnée a un type ou une valeur invalide.
     * (ex: une chaîne au lieu d'un entier, ou un type de consommation inconnu).
     */
    public static class DonneeInvalide extends ReseauException {
    	private static final long serialVersionUID = 1L;
    	/**
    	 * Signale qu'un nombre entier était attendu mais n'a pas été trouvé.
    	 *
    	 * @param nomDonnee Le nom de la variable (ex: "capacité du générateur")
    	 * @param ligne Le numéro de la ligne (ou 0)
    	 */
        public DonneeInvalide(String nomDonnee, int ligne) {
            super("La " + nomDonnee + " doit être un nombre entier.", ligne);
        }
        
        /**
    	 * Signale qu'un nombre entier était attendu mais n'a pas été trouvé.
    	 *
    	 * @param msg Le message à afficher (maison ne peut pas etre vide)
    	 */
        public DonneeInvalide(String msg) {
            super(msg);
        }

        /**
         * Signale qu'un type énuméré (ex: TypeConsommation) est inconnu.
         *
         * @param typeRecu La valeur reçue qui est fausse
         * @param typesAttendus La liste des valeurs possibles
         * @param ligne Le numéro de la ligne (ou 0)
         */
        public DonneeInvalide(String typeRecu, String typesAttendus, int ligne) {
            super("Type '" + typeRecu + "' inconnu. Utilisez " + typesAttendus + ".", ligne);
        }
    }

    /**
     * Signale qu'une référence à un élément (Maison ou Générateur) est introuvable.
     * Utilisé lors de la création ou suppression de connexions.
     */
    public static class ElementIntrouvable extends ReseauException {
    	private static final long serialVersionUID = 1L;
    	/**
    	 * Version Fichier : Signale l'élément introuvable avec le numéro de ligne.
    	 *
    	 * @param typeElement Le type d'élément cherché (ex: "générateur")
    	 * @param nom Le nom de l'élément qui n'existe pas
    	 * @param ligne Le numéro de la ligne
    	 */
        public ElementIntrouvable(String typeElement, String nom, int ligne) {
            super(capitalize(typeElement) + " '" + nom + "' introuvable.", ligne);
        }

        /**
         * Version Menu : Signale l'élément introuvable sans numéro de ligne.
         *
         * @param typeElement Le type d'élément cherché
         * @param nom Le nom de l'élément qui n'existe pas
         */
        public ElementIntrouvable(String typeElement, String nom) {
            super(capitalize(typeElement) + " '" + nom + "' introuvable.");
        }

        // Petit utilitaire pour mettre la majuscule (Générateur vs générateur)
        private static String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
    
    /**
     * Signale que l'ordre des sections dans le fichier n'est pas respecté.
     * (Générateurs -> Maisons -> Connexions).
     */
    public static class Ordre extends ReseauException {
    	private static final long serialVersionUID = 1L;
    	/**
    	 * Construit une erreur d'ordre détaillée.
    	 *
    	 * @param element L'élément qui est mal placé (ex: "maison")
    	 * @param raison L'explication de la contrainte (ex: "doivent être définies avant les connexions")
    	 * @param ligne Le numéro de la ligne
    	 */
        public Ordre(String element, String raison, int ligne) {
            super("Ordre de définition invalide pour '" + element + "'. " + raison, ligne);
        }

        /**
         * Construit une erreur d'ordre simple.
         *
         * @param message Le message d'erreur
         * @param ligne Le numéro de la ligne
         */
        public Ordre(String message, int ligne) {
            super(message, ligne);
        }
    }
    
    /**
     * Signale une incohérence dans la logique métier du réseau.
     * (ex: Capacité 0, Maison déjà connectée, Réseau vide).
     */
    public static class Logique extends ReseauException {
    	private static final long serialVersionUID = 1L;
    	/**
    	 * Construit une erreur logique générale (souvent utilisée dans le Menu).
    	 * Ajoute le préfixe "Incohérence logique : ".
    	 *
    	 * @param details Les détails du problème
    	 */
        public Logique(String details) { 
            super("Incohérence logique : " + details);
        }
        
        /**
         * Construit une erreur logique détectée à une ligne précise d'un fichier.
         *
         * @param details Les détails du problème
         * @param ligne Le numéro de la ligne
         */
        public Logique(String details, int ligne) {
            super("Incohérence logique : " + details, ligne);
        }
    }
    
}