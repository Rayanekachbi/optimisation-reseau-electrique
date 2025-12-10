package packet;

import java.util.HashMap;
import java.util.Map;

public class Reseau {

    private static double LAMBDA = 10.0;

    private Map<String, Maison> maisons;
    private Map<String, Generateur> generateurs;
    private Map<Maison, Generateur> connexions;

    /**
     * Initialise un nouveau réseau électrique vide.
     */
    public Reseau() {
        this.maisons = new HashMap<>();
        this.generateurs = new HashMap<>();
        this.connexions = new HashMap<>();
    }

    /**
     * Ajoute un nouveau générateur ou met à jour sa capacité s'il existe déjà.
     *
     * @param nom Le nom unique du générateur
     * @param capacite La capacité maximale de production en kW
     * @return Un message confirmant la création ou la mise à jour
     * @throws ReseauException Si le nom est vide ou la capacité invalide
     */
    public String ajouterOuMajGenerateur(String nom, int capacite) throws ReseauException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new ReseauException.DonneeInvalide("Le nom du générateur ne peut pas être vide.", 0);
        }

        boolean existait = this.generateurs.containsKey(nom);
        if (existait) {
            // quand on crée un nouveau generateur existant,
            // si l'ancien generateur est utilisé comme valeur dans la map connexions,
            // les connexions existantes pointerons toujours vers l'ancien générateur et non
            // pas le nouveau
            double ancienneCapacite = this.generateurs.get(nom).getCapaciteMaximale();
            try {
                this.generateurs.get(nom).setCapaciteMaximale(capacite);
                
            } catch (IllegalArgumentException e) {
                throw new ReseauException("Impossible de mettre à jour le générateur : " + e.getMessage());
            }
            
            return "MAJ: Capacité du générateur " + nom + " mise à jour de " + ancienneCapacite + "kW à " + capacite
                    + "kW.";
        } else {
            // Création du nouvel objet
            try {
                this.generateurs.put(nom, new Generateur(nom, capacite));
                
            } catch (IllegalArgumentException e) {
                throw new ReseauException("Impossible de créer le générateur : " + e.getMessage());
            }
            
            return "OK: Générateur " + nom + " créé.";
        }
    }

    /**
     * Ajoute une nouvelle maison ou met à jour son type de consommation si elle existe déjà.
     *
     * @param nom Le nom unique de la maison
     * @param typeConsommation Le type de consommation (BASSE, NORMAL, FORTE)
     * @return Un message confirmant la création ou la mise à jour
     * @throws ReseauException Si le nom est vide ou le type null
     */
    public String ajouterOuMajMaison(String nom, TypeConsommation typeConsommation) throws ReseauException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new ReseauException.DonneeInvalide("Le nom de la maison ne peut pas être vide.", 0);
        }

        boolean existait = this.maisons.containsKey(nom);
        try {
        	if (existait) {
                Maison mExistante = this.maisons.get(nom);
                mExistante.setConsommation(typeConsommation);
                return "MAJ: Consommation de la maison " + nom + " mise à jour.";
            } else {
                this.maisons.put(nom, new Maison(nom, typeConsommation));
                return "OK: Maison " + nom + " créée.";
            }
        }catch (IllegalArgumentException e ) {
        	throw new ReseauException("Erreur qur la maison "+ nom + " : " + e.getMessage());
        }
        
    }

    /**
     * Crée ou met à jour une connexion entre une maison et un générateur.
     * L'ordre des paramètres n'a pas d'importance.
     *
     * @param nom1 Le nom du premier élément (Maison ou Générateur)
     * @param nom2 Le nom du second élément (Générateur ou Maison)
     * @return Un message confirmant la connexion
     * @throws ReseauException Si un élément est introuvable ou les noms invalides
     */
    public String ajouterConnexion(String nom1, String nom2) throws ReseauException {
        Maison m = null;
        Generateur g = null;

        if (this.maisons.containsKey(nom1) && this.generateurs.containsKey(nom2)) {
            m = this.maisons.get(nom1);
            g = this.generateurs.get(nom2);
        } else if (this.maisons.containsKey(nom2) && this.generateurs.containsKey(nom1)) {
            m = this.maisons.get(nom2);
            g = this.generateurs.get(nom1);
        } else {
            // Si nom1 est une Maison valide, c'est que nom2 (le générateur) est faux
            if (this.maisons.containsKey(nom1)) {
                throw new ReseauException.ElementIntrouvable("générateur", nom2);
            }
            // Si nom1 est un Générateur valide, c'est que nom2 (la maison) est faux
            if (this.generateurs.containsKey(nom1)) {
                throw new ReseauException.ElementIntrouvable("maison", nom2);
            }
            // Si nom2 est une Maison valide, c'est que nom1 (le générateur) est faux
            if (this.maisons.containsKey(nom2)) {
                throw new ReseauException.ElementIntrouvable("générateur", nom1);
            }
            // Si nom2 est un Générateur valide, c'est que nom1 (la maison) est faux
            if (this.generateurs.containsKey(nom2)) {
                throw new ReseauException.ElementIntrouvable("maison", nom1);
            }

            throw new ReseauException.ElementIntrouvable("élément", nom1 + " ou " + nom2);
        }

        if (this.connexions.containsKey(m)) {
            this.connexions.put(m, g);
            // met à jour le g du m déja existant (propriété de la méthode put pour les map)
            return "MAJ: Maison " + m.getNom() + " désormais connectée à " + g.getNom() + ".";
        } else {
            this.connexions.put(m, g);
            return "OK: Connexion entre " + m.getNom() + " et " + g.getNom() + " créée.";
        }
    }

    /**
     * Supprime la connexion existante entre une maison et un générateur.
     *
     * @param nom1 Le nom du premier élément
     * @param nom2 Le nom du second élément
     * @return Un message confirmant la suppression
     * @throws ReseauException Si un élément est introuvable ou si la connexion n'existe pas
     */
    public String suppConnexion(String nom1, String nom2) throws ReseauException {
        Maison m = null;
        Generateur g = null;

        if (this.maisons.containsKey(nom1) && this.generateurs.containsKey(nom2)) {
            m = this.maisons.get(nom1);
            g = this.generateurs.get(nom2);
        } else if (this.maisons.containsKey(nom2) && this.generateurs.containsKey(nom1)) {
            m = this.maisons.get(nom2);
            g = this.generateurs.get(nom1);
        }

        if (m == null || g == null) {
        	// même logique que dans ajouterConnexion pour trouver l'élement manquant
            if (this.maisons.containsKey(nom1) || this.maisons.containsKey(nom2)) {
                // Si on a trouvé la maison, c'est le générateur qui manque
                String nomGen = this.maisons.containsKey(nom1) ? nom2 : nom1;
                throw new ReseauException.ElementIntrouvable("générateur", nomGen);
            }
            if (this.generateurs.containsKey(nom1) || this.generateurs.containsKey(nom2)) {
                // Si on a trouvé le générateur, c'est la maison qui manque
                String nomMaison = this.generateurs.containsKey(nom1) ? nom2 : nom1;
                throw new ReseauException.ElementIntrouvable("maison", nomMaison);
            }
            throw new ReseauException.ElementIntrouvable("élément", nom1 + " ou " + nom2);
        }

        // Vérification si la connexion existe
        Generateur gActuel = this.connexions.get(m);
        if (gActuel == null || !gActuel.equals(g)) {
            throw new ReseauException.Logique("La connexion entre " + m.getNom() + " et " + g.getNom() + " n'existe pas.");
        }

        this.connexions.remove(m, g);
        return "La connexion entre la maison "+m.getNom()+" et le generateur "+g.getNom()+" a ete supprimee avec succes.";
        
    }

    /**
     * Vérifie la validité globale du réseau (présence d'éléments, couverture totale des maisons).
     *
     * @throws ReseauException.Logique Si le réseau est invalide (contient la liste des erreurs)
     */
    public void validerReseau() throws ReseauException{
        java.util.List<String> erreurs = new java.util.ArrayList<>();
        
        if (this.maisons.isEmpty()) {
            erreurs.add("Problème: Le réseau doit contenir au moins une maison.");
        }
        if (this.generateurs.isEmpty()) {
            erreurs.add("Problème: Le réseau doit contenir au moins un générateur.");
        }
        
        // La contrainte "une maison est connectée à un seul générateur au maximum"
        // est gérée par la conception de la Map et non par la méthode de validation
        // Remplacement Automatique : Si on appelle this.connexions.put(M1, G1) puis
        // this.connexions.put(M1, G2)
        // la deuxième opération remplace la valeur de la clé M1 de G1 par G2.
        // La maison M1 est alors connectée uniquement à G2.
        for (Maison m : this.maisons.values()) {
            if (!this.connexions.containsKey(m)) {
                erreurs.add("Problème: Maison " + m.getNom() + " n'a aucune connexion.");
            }
        }

        //on construit notre message d'erreurs et on le throw
        if (!erreurs.isEmpty()) {
            StringBuilder sb = new StringBuilder("Le réseau est INVALIDE :");
            for (String err : erreurs) {
                sb.append("\n\t- ").append(err);
            }
            // On lance notre exception logique
            throw new ReseauException.Logique(sb.toString());
        }
    }

    
    /**
     * Vérifie si une connexion existe entre deux éléments donnés (utilitaire pour l'affichage).
     *
     * @param nom1 Le nom du premier élément
     * @param nom2 Le nom du second élément
     * @return true si la connexion existe, false sinon
     */    
    public boolean isConnexionExiste(String nom1, String nom2) {

        // Trouver les objets Maison et Generateur 
        Maison m = null;
        Generateur g = null;
        
        if (nom1 == null || nom2 == null) return false;

        if (this.maisons.containsKey(nom1) && this.generateurs.containsKey(nom2)) {
            m = this.maisons.get(nom1);
            g = this.generateurs.get(nom2);
        } else if (this.maisons.containsKey(nom2) && this.generateurs.containsKey(nom1)) {
            m = this.maisons.get(nom2);
            g = this.generateurs.get(nom1);
        }

        // Gérer les objets non trouvés
        // Si la maison ou le générateur n'existe même pas, la connexion est impossible.
        if (m == null || g == null) {
            return false;
        }
        
        // Vérifier la connexion
        // On récupère le générateur ACTUELLEMENT connecté à cette maison
        // S'il n'y a pas de connexion, gActuel sera 'null'
        Generateur gActuel = this.connexions.get(m);
        
        // La connexion existe si gActuel n'est pas nul et que gAcuel est le meme g qu'on a trouvé auparavant
        return gActuel != null && gActuel.equals(g);
    }

    /////////////////////// 
    // !!! CALCULS !!! ///
    /////////////////////
    
    /**
     * Calcule la charge totale actuelle demandée à un générateur par ses maisons connectées.
     *
     * @param g Le générateur concerné
     * @return La somme des demandes en kW
     */
    public double calculerChargeActuelle(Generateur g) {
        double charge = 0.0;

        // Map.Entry permet d'accéder simultanément aux clés (Maison) et aux valeurs
        // (Generateur)
        // Maison m = entry.getKey();
        // Generateur g = entry.getValue();
        // this.connexions.entrySet(), exécute le corps de la boucle
        for (Map.Entry<Maison, Generateur> entry : this.connexions.entrySet()) {
            if (entry.getValue().equals(g)) {
                charge += entry.getKey().getConsommation().getDemandeKw();
            }
        }
        return charge;
    }

    /**
     * Calcule le taux d'utilisation d'un générateur (Charge / Capacité).
     *
     * @param g Le générateur concerné
     * @return Le taux d'utilisation (1.0 équivaut à 100%)
     * @throws ReseauException Si la capacité du générateur est de 0
     */
    public double calculerTauxUtilisation(Generateur g) throws ReseauException{
        double capacite = g.getCapaciteMaximale();

        // partie 2 traiter le l'erreur ou capacite = 0
        if (capacite == 0) {
            throw new ReseauException.Logique("Le générateur " + g.getNom() + " a une capacité de 0 kW. Calcul impossible.", 0);
        }

        double charge = calculerChargeActuelle(g);
        return charge / capacite;
    }

    /**
     * Calcule la dispersion des taux d'utilisation des générateurs (écart moyen à la moyenne).
     *
     * @return La valeur de dispersion (plus elle est basse, plus le réseau est équilibré)
     * @throws ReseauException En cas d'erreur de calcul sur un générateur
     */
    public double calculerDispersion() throws ReseauException{
        java.util.List<Double> taux = new java.util.ArrayList<>();

        // on rempli la liste "taux " avec les taux des générateurs
        for (Generateur g : this.generateurs.values())
            taux.add(calculerTauxUtilisation(g));
        // le cas liste vide
        if (taux.isEmpty())
            return 0.0;

        // la somme des taux
        double sommeTaux = 0.0;
        for (Double t : taux)
            sommeTaux += t;

        // moyenne des taux
        double moyenneTaux = sommeTaux / taux.size();

        // calcul final
        double dispersion = 0.0;
        for (Double t : taux)
            dispersion += Math.abs(t - moyenneTaux);

        return dispersion;
    }

    /**
     * Calcule la pénalité totale liée à la surcharge des générateurs.
     *
     * @return La somme des surplus de charge
     * @throws ReseauException En cas d'erreur de calcul sur un générateur
     */
    public double calculerSurcharge() throws ReseauException{
        double surcharge = 0.0;

        for (Generateur g : this.generateurs.values()) {
            double penalite = calculerTauxUtilisation(g) - 1;
            surcharge += Math.max(penalite, 0);
        }

        return surcharge;
    }

    /**
     * Calcule le coût total du réseau selon la formule : Dispersion + Lambda * Surcharge.
     *
     * @return Le coût global 
     * @throws ReseauException En cas d'erreur de calcul
     */
    public double calculerCout() throws ReseauException{
        return calculerDispersion() + LAMBDA * calculerSurcharge();
    }

    /**
     * Affiche l'état complet du réseau (Maisons, Générateurs, Connexions) dans la console.
     * Gère l'affichage des erreurs de calcul (ex: capacité 0) sans planter.
     */
    public void afficherReseau() {
        System.out.println("\n==================================");
        System.out.println("||     ETAT ACTUEL DU RÉSEAU    ||");
        System.out.println("==================================");

        // Affichage des Maisons
        System.out.println("--- Maisons (" + this.maisons.size() + ") ---");
        for (Maison m : this.maisons.values()) {
            Generateur gConnecte = this.connexions.get(m);
            String statut;
            if (gConnecte != null) {
                statut = "Connectée à " + gConnecte.getNom();
            } else {
                statut = "Non connectée";
            }

            System.out.printf("- %s : %s (%d kW) | Statut: %s\n",
                    m.getNom(), m.getConsommation().toString(), m.getConsommation().getDemandeKw(), statut);
        }

        // Affichage des Générateurs
        System.out.println("\n--- Générateurs (" + this.generateurs.size() + ") ---");
        for (Generateur g : this.generateurs.values()) {
            double charge = calculerChargeActuelle(g);
            String tauxStr;
            String etat;

            try {
                double taux = calculerTauxUtilisation(g);
                tauxStr = String.format("%.2f", taux * 100);

                if (taux > 1.0) {
                    etat = " [SURCHARGE]";
                } else {
                    etat = " [OK]";
                }
            } catch (ReseauException e) {
                tauxStr = "N/A";
                etat = " [ERREUR]";
            }

            // printf a été utilisée parfois afin d'éviter d'écrire tout ceci comme ça ^^
            System.out.println("- " + g.getNom() +
                    " : Capacité " + String.format("%.0f", g.getCapaciteMaximale()) + " kW" +
                    " | Charge " + String.format("%.0f", charge) + " kW" +
                    " | Taux Utilisation: " + tauxStr + "%" +
                    etat);
            
        }

        // Affichage des Connexions
        System.out.println("\n--- Détail des Connexions ---");
        for (Generateur g : this.generateurs.values()) {
            System.out.print("-> Générateur " + g.getNom() + " alimente: ");

            java.util.List<String> maisonsConnectees = new java.util.ArrayList<>();
            for (Map.Entry<Maison, Generateur> entry : this.connexions.entrySet()) {
                if (entry.getValue().equals(g)) {
                    maisonsConnectees.add(entry.getKey().getNom());
                }
            }

            if (maisonsConnectees.isEmpty()) {
                System.out.println("Aucune maison connectée.");
            } else {
                System.out.println(String.join(", ", maisonsConnectees));
                // join sert à assembler tous les éléments d'une liste ou d'un tableau en une
                // seule chaîne de caractères,
                // en insérant un séparateur spécifique entre chaque élément
            }
        }
        System.out.println("==================================");
    }
    
    
    // Getters et Setters
    /**
     * Récupère la map des maisons du réseau.
     *
     * @return La map associant nom -> objet Maison
     */
    public Map<String, Maison> getMaisonsMap() {
        return this.maisons;
    }

    /**
     * Récupère la map des générateurs du réseau.
     *
     * @return La map associant nom -> objet Generateur
     */
    public Map<String, Generateur> getGenerateursMap() {
        return this.generateurs;
    }

    /**
     * Récupère la map des connexions actives.
     *
     * @return La map associant Maison -> Generateur
     */
    public Map<Maison, Generateur> getConnexionsMap() {
        return this.connexions;
    }
    
    /**
     * Récupère le facteur de pénalité Lambda.
     *
     * @return La valeur actuelle de Lambda
     */
    public double getLambda() {
    	return LAMBDA;
    }
    
    /**
     * Définit le facteur de pénalité Lambda pour le calcul du coût.
     *
     * @param L La nouvelle valeur de Lambda
     */
    public void setLambda(double L) {
    	LAMBDA = L;
    	
    }
}