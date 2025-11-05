import java.util.HashMap;
import java.util.Map;

public class Reseau {

    private static final double LAMBDA = 10.0;

    private Map<String, Maison> maisons;
    private Map<String, Generateur> generateurs;
    private Map<Maison, Generateur> connexions;

    public Reseau() {
        this.maisons = new HashMap<>();
        this.generateurs = new HashMap<>();
        this.connexions = new HashMap<>();
    }

    public String ajouterOuMajGenerateur(String nom, int capacite) {
        boolean existait = this.generateurs.containsKey(nom);
        if (existait) {
            // quand on crée un nouveau generateur existant,
            // si l'ancien generateur est utilisé comme valeur dans la map connexions,
            // les connexions existantes pointerons toujours vers l'ancien générateur et non
            // pas le nouveau
            double ancienneCapacite = this.generateurs.get(nom).getCapaciteMaximale();
            this.generateurs.get(nom).setCapaciteMaximale(capacite);
            return "MAJ: Capacité du générateur " + nom + " mise à jour de " + ancienneCapacite + "kW à " + capacite
                    + "kW.";
        } else {
            // Création du nouvel objet
            this.generateurs.put(nom, new Generateur(nom, capacite));
            return "OK: Générateur " + nom + " créé.";
        }
    }

    public String ajouterOuMajMaison(String nom, TypeConsommation typeConsommation) {
        boolean existait = this.maisons.containsKey(nom);
        if (existait) {
            Maison mExistante = this.maisons.get(nom);
            mExistante.setConsommation(typeConsommation);
            return "MAJ: Consommation de la maison " + nom + " mise à jour.";
        } else {
            Maison m = new Maison(nom, typeConsommation);
            this.maisons.put(nom, m);
            return "OK: Maison " + nom + " créée.";
        }
    }

    public String ajouterConnexion(String nom1, String nom2) {
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
            return "Erreur: Maison ou générateur introuvable. Vérifiez que '" + nom1 + "' et '" + nom2 + "' existent.";
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

    public java.util.List<String> validerReseau() {
        java.util.List<String> erreurs = new java.util.ArrayList<>();
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
        return erreurs;

        // peut servire pour l'algo de deuxième partie :

        // // Vérification de la surcharge des générateurs
        // // Calculer la charge totale sur chaque générateur
        // Map<Generateur, Double> charges = new HashMap<>();
        // for (Maison m : this.connexions.keySet()) {
        // Generateur g = this.connexions.get(m);
        //
        // // Si le générateur g est dans la Map charges, on récupère sa charge.
        // // Sinon (si c'est la première maison connectée à g),
        // // on prend la valeur par défaut 0.0
        // double chargeActuelle = charges.getOrDefault(g, 0.0);
        // charges.put(g, chargeActuelle + m.getConsommation().getDemandeKw());
        // }
        // // Compare la charge à la capacité maximale
        // for (Generateur g : charges.keySet()) {
        // double charge = charges.get(g);
        // if (charge > g.getCapaciteMaximale()) {
        // erreurs.add("Problème: Générateur " + g.getNom() + " est surchargé. Charge: "
        // + charge
        // + " kW > Capacité: " + g.getCapaciteMaximale() + " kW.");
        // }
        // }
    }

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

    public double calculerTauxUtilisation(Generateur g) {
        double charge = calculerChargeActuelle(g);
        double capacite = g.getCapaciteMaximale();

        // partie 2 traiter le l'erreur ou capacite = 0
        return charge / capacite;
    }

    public double calculerDispersion() {
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

    public double calculerSurcharge() {
        double surcharge = 0.0;

        for (Generateur g : this.generateurs.values()) {
            double penalite = calculerTauxUtilisation(g) - 1;
            surcharge += Math.max(penalite, 0);
        }

        return surcharge;
    }

    public double calculerCout() {
        return calculerDispersion() + LAMBDA * calculerSurcharge();
    }

    public void afficherReseau() {
    System.out.println("\n==================================");
    System.out.println("||     ETAT ACTUEL DU RÉSEAU    ||");
    System.out.println("==================================");

    //Affichage des Maisons
    System.out.println("--- Maisons (" + this.maisons.size() + ") ---");
    for (Maison m : this.maisons.values()) {
        Generateur gConnecte = this.connexions.get(m);
        String statut = "Connectée à " + gConnecte.getNom();
        
        System.out.printf("- %s : %s (%d kW) | Statut: %s\n",
                m.getNom(), m.getConsommation().toString(), m.getConsommation().getDemandeKw(), statut);
    }

    //Affichage des Générateurs
    System.out.println("\n--- Générateurs (" + this.generateurs.size() + ") ---");
    for (Generateur g : this.generateurs.values()) {
        double charge = calculerChargeActuelle(g);
        double taux = calculerTauxUtilisation(g);

        String etat;
        if (taux > 1.0) {
            etat = " [SURCHARGE]";
        } else {
            etat = " [OK]";
        }

        // Utilisation de printf c'est largement mieu que ça
        System.out.println("- " + g.getNom() + 
                   " : Capacité " + String.format("%.0f", g.getCapaciteMaximale()) + " kW" +
                   " | Charge " + String.format("%.0f", charge) + " kW" +
                   " | Taux Utilisation: " + String.format("%.2f", taux * 100) + "%" + 
                   etat);
        //System.out.printf("- %s : Capacité %.0f kW | Charge %.0f kW | Taux Utilisation: %.2f%%%s\n",
        //g.getNom(), g.getCapaciteMaximale(), charge, taux * 100, etat);
    }

    //Affichage des Connexions
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
            //join sert à assembler tous les éléments d'une liste ou d'un tableau en une seule chaîne de caractères, 
            //en insérant un séparateur spécifique entre chaque élément
        }
    }
    System.out.println("==================================");
}
}
