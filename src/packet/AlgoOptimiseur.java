package packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Classe responsable de l'optimisation du réseau électrique.
 * Elle utilise un algorithme de Recuit Simulé (Simulated Annealing) pour modifier
 * les connexions entre maisons et générateurs afin de minimiser le coût total (Dispersion + Surcharge).
 */
public class AlgoOptimiseur {

    private Reseau reseau;
    private Random random;

    /**
     * Initialise l'optimiseur pour un réseau donné.
     *
     * @param reseau Le réseau électrique à optimiser
     */
    public AlgoOptimiseur(Reseau reseau) {
        this.reseau = reseau;
        this.random = new Random();
    }

    /**
     * Exécute l'algorithme d'optimisation (Recuit Simulé).
     * L'algorithme part d'une solution gloutonne, puis explore des changements aléatoires de connexion.
     * Il accepte parfois des solutions moins bonnes (selon la température) pour éviter les minima locaux.
     * A la fin, la meilleure solution trouvée est appliquée au réseau.
     *
     * @param nbIterations Le nombre d'itérations de l'algorithme (ex: 50000, modifiable dans la methode du menu automatique)
     * @throws ReseauException En cas d'erreur lors du calcul des coûts (ex: capacité nulle)
     */
    public void resoudre(int nbIterations) throws ReseauException{
        System.out.println("Début de l'optimisation ...");

        // Initialisation Gloutonne
        initialisationIntelligente();
        
        double coutActuel = reseau.calculerCout();
        
        // On sauvegarde la MEILLEURE solution trouvée jusqu'ici
        // Car le recuit simulé peut parfois finir sur une solution un peu moins bonne en explorant
        Map<Maison, Generateur> meilleureConnexions = new HashMap<>(reseau.getConnexionsMap());
        double meilleurCout = coutActuel;

        // Paramètres du Recuit
        double temperature = 100.0;
        double refroidissement = 0.9997; // pour diminuer lentement la température

        // Conversion en listes pour accès rapide par index
        List<Maison> maisons = new ArrayList<>(reseau.getMaisonsMap().values());
        List<Generateur> generateurs = new ArrayList<>(reseau.getGenerateursMap().values());

        if (maisons.isEmpty() || generateurs.isEmpty()) {
            System.out.println("Réseau vide, rien à optimiser.");
            return;
        }

        // Boucle principale
        for (int i = 0; i < nbIterations; i++) {
            //On change une connexion au hasard
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

            //Test du nouveau coût
            //On applique le changement temporairement
            reseau.getConnexionsMap().put(mChoisie, gNouveau);
            double nouveauCout = reseau.calculerCout();
            
            double delta = nouveauCout - coutActuel;

            //Décision (Critère de Metropolis)
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

                if (coutActuel < meilleurCout) {
                    meilleurCout = coutActuel;
                    // On fait une copie de sauvegarde de cette configuration gagnante
                    meilleureConnexions = new HashMap<>(reseau.getConnexionsMap());
                }
            } else {
                // On annule le changement
                if (gActuel != null) {
                    reseau.getConnexionsMap().put(mChoisie, gActuel);
                } else {
                    // Si elle n'était pas connectée avant (cas rare avec init intelligente)
                    reseau.getConnexionsMap().remove(mChoisie);
                }
            }

            // Refroidissement
            temperature *= refroidissement;
            
            //Arrêt si température très basse
            if (temperature < 0.0001) break;
        }

        // Restauration de la meilleure solution trouvée
        reseau.getConnexionsMap().clear();
        reseau.getConnexionsMap().putAll(meilleureConnexions);

        System.out.println("Optimisation terminée.");
        System.out.println("Meilleur coût trouvé : " + String.format("%.4f", meilleurCout));
    }

    /**
     * Stratégie d'initialisation gloutonne (Greedy).
     * Trie les maisons par consommation décroissante (les plus grosses d'abord)
     * et les connecte au générateur ayant le taux d'utilisation le plus faible à ce moment-là.
     * Cela permet de partir d'une solution "correcte" avant de lancer le recuit simulé.
     *
     * @throws ReseauException Si une erreur survient lors du calcul des taux d'utilisation
     */
    private void initialisationIntelligente() throws ReseauException{
        // On vide tout pour repartir de zéro
        reseau.getConnexionsMap().clear();
        
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