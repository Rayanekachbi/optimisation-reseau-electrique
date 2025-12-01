package packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AlgoOptimiseur {

    private Reseau reseau;
    private Random random;

    public AlgoOptimiseur(Reseau reseau) {
        this.reseau = reseau;
        this.random = new Random();
    }

    /**
     * Lance l'algorithme de Recuit Simulé pour optimiser le réseau.
     * @param nbIterations Nombre d'essais (ex: 10000 ou 50000)
     */
    public void resoudre(int nbIterations) {
        System.out.println("Début de l'optimisation (Recuit Simulé)...");

        // 1. Initialisation Gloutonne (Partir d'une bonne base)
        initialisationIntelligente();
        
        double coutActuel = reseau.calculerCout();
        
        // On sauvegarde la MEILLEURE solution trouvée jusqu'ici
        // (Car le recuit simulé peut parfois finir sur une solution un peu moins bonne en explorant)
        Map<Maison, Generateur> meilleureConnexions = new HashMap<>(reseau.getConnexionsMap());
        double meilleurCout = coutActuel;

        // Paramètres du Recuit
        double temperature = 100.0;
        double refroidissement = 0.99; // Diminue lentement la température

        // Conversion en listes pour accès rapide par index
        List<Maison> maisons = new ArrayList<>(reseau.getMaisonsMap().values());
        List<Generateur> generateurs = new ArrayList<>(reseau.getGenerateursMap().values());

        if (maisons.isEmpty() || generateurs.isEmpty()) {
            System.out.println("Réseau vide, rien à optimiser.");
            return;
        }

        // 2. Boucle principale
        for (int i = 0; i < nbIterations; i++) {
            // --- A. Mutation : On change une connexion au hasard ---
            Maison mChoisie = maisons.get(random.nextInt(maisons.size()));
            Generateur gActuel = reseau.getConnexionsMap().get(mChoisie);
            
            // Choisir un nouveau générateur différent de l'actuel
            Generateur gNouveau = generateurs.get(random.nextInt(generateurs.size()));
            
            // Petit filet de sécurité si on a qu'un seul générateur
            if (generateurs.size() > 1) {
                while (gNouveau == gActuel) {
                    gNouveau = generateurs.get(random.nextInt(generateurs.size()));
                }
            }

            // --- B. Test du nouveau coût ---
            // On applique le changement temporairement
            reseau.getConnexionsMap().put(mChoisie, gNouveau);
            double nouveauCout = reseau.calculerCout();
            
            double delta = nouveauCout - coutActuel;

            // --- C. Décision (Critère de Metropolis) ---
            boolean accepterChangement = false;

            if (delta < 0) {
                // Amélioration : on garde toujours
                accepterChangement = true;
            } else {
                // Dégradation : on garde avec une probabilité liée à la température
                // Plus il fait "froid", moins on accepte de dégrader
                if (random.nextDouble() < Math.exp(-delta / temperature)) {
                    accepterChangement = true;
                }
            }

            if (accepterChangement) {
                coutActuel = nouveauCout;
                
                // Est-ce le record absolu ?
                if (coutActuel < meilleurCout) {
                    meilleurCout = coutActuel;
                    // On fait une copie de sauvegarde de cette configuration gagnante
                    meilleureConnexions = new HashMap<>(reseau.getConnexionsMap());
                }
            } else {
                // On annule le changement (Rollback)
                if (gActuel != null) {
                    reseau.getConnexionsMap().put(mChoisie, gActuel);
                } else {
                    // Si elle n'était pas connectée avant (cas rare avec init intelligente)
                    reseau.getConnexionsMap().remove(mChoisie);
                }
            }

            // --- D. Refroidissement ---
            temperature *= refroidissement;
            
            // Optionnel : Arrêt si température très basse
            if (temperature < 0.0001) break;
        }

        // 3. Restauration de la meilleure solution trouvée
        // On remplace les connexions actuelles par le "Best of" sauvegardé
        reseau.getConnexionsMap().clear();
        reseau.getConnexionsMap().putAll(meilleureConnexions);

        System.out.println("Optimisation terminée.");
        System.out.println("Meilleur coût trouvé : " + String.format("%.4f", meilleurCout));
    }

    // Méthode privée pour l'initialisation gloutonne
    private void initialisationIntelligente() {
        // On vide tout pour repartir de zéro
        reseau.viderConnexions();
        
        List<Maison> maisonsTriees = new ArrayList<>(reseau.getMaisonsMap().values());
        // Tri décroissant : les plus grosses demandes d'abord
        maisonsTriees.sort((m1, m2) -> Integer.compare(
            m2.getConsommation().getDemandeKw(), 
            m1.getConsommation().getDemandeKw()
        ));

        for (Maison m : maisonsTriees) {
            Generateur meilleurG = null;
            double meilleurScore = Double.MAX_VALUE;

            // On cherche le générateur qui a le taux le plus bas
            for (Generateur g : reseau.getGenerateursMap().values()) {
                double taux = reseau.calculerTauxUtilisation(g);
                if (taux < meilleurScore) {
                    meilleurScore = taux;
                    meilleurG = g;
                }
            }
            
            if (meilleurG != null) {
                reseau.getConnexionsMap().put(m, meilleurG);
            }
        }
    }
}