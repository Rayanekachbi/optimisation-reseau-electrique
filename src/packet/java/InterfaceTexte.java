package packet.java;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.IOException;

/**
 * Classe principale gérant l'interface utilisateur en ligne de commande (CLI).
 * Elle permet de configurer le réseau manuellement ou via un fichier, 
 * de lancer les calculs de coûts et d'exécuter l'algorithme d'optimisation.
 */
public class InterfaceTexte {

    private Reseau reseau;
    private Scanner scanner;

    /**
     * Initialise l'application avec un réseau vide et un scanner pour lire les entrées utilisateur.
     */
    public InterfaceTexte() {
        this.reseau = new Reseau();
        this.scanner = new Scanner(System.in);
    }

    //MAIN
    /**
     * Point d'entrée principal du programme.
     * Analyse les arguments pour déterminer le mode de lancement :
     * - Si aucun argument : Mode Manuel (Menu de configuration interactif).
     * - Si 1 argument (chemin) : Mode Fichier (Chargement et Menu Automatique).
     * - Si 2 arguments : Mode Fichier avec définition du paramètre Lambda.
     *
     * @param args Les arguments de la ligne de commande [cheminFichier, lambda]
     */
    public static void main(String[] args){
        InterfaceTexte app = new InterfaceTexte();

        if (args.length > 0) {
            String cheminFichier = args[0];
            
            // Si un 2ème argument est présent, c'est le lambda
            if (args.length >= 2) {
                try {
                    app.reseau.setLambda(Double.parseDouble(args[1]));
                } catch (NumberFormatException e) {
                    System.out.println("Attention: Lambda invalide, utilisation de 10.0 par défaut.");
                }
            }
            
            // On lance la logique fichier
            app.lancerModeFichier(cheminFichier);
            
        } else {
            app.demarrer();
        }
    }

    // PARTIE 2 : LOGIQUE FICHIER
    /**
     * Lance le mode "Fichier" : charge un réseau existant depuis le disque.
     * Si le chargement réussit, le menu automatique est affiché.
     * Gestion centralisée des exceptions (ReseauException, IOException) pour ce mode.
     *
     * @param chemin Le chemin vers le fichier de configuration à lire
     */
    public void lancerModeFichier(String chemin) {
        System.out.println("--- Mode Fichier détecté ---");
        System.out.println("Chargement de : " + chemin);
        System.out.println("Paramètre Lambda : " + this.reseau.getLambda());

        try {
            // délégation à GestionFichier pour la lecture et validation
            this.reseau = GestionFichier.lireFichier(chemin);
            System.out.println("Succès : Fichier chargé et validé !");
            
            // lancement du menu auto
            menuAutomatique();

        } catch (ReseauException e) {
            // Gestion des erreurs métier (Syntaxe, Logique, etc.)
            System.err.println("\nERREUR CRITIQUE dans le fichier de configuration :");
            System.err.println(">> " + e.getMessage());
            System.err.println("Le programme va s'arrêter.");
            
        } catch (IOException e) {
            // Erreur technique (Fichier introuvable, disque illisible...)
            System.err.println("\nERREUR lors de l'accès au fichier :");
            System.err.println(">> " + e.getMessage());
            
        } catch (Exception e) {
            // Erreur imprévue (Bug de programmation)
            System.err.println("\nERREUR INATTENDUE :");
            e.printStackTrace();
        }
    }

    /**
     * Affiche le menu dédié au mode fichier.
     * Propose principalement l'optimisation et la sauvegarde.
     * Gère la boucle d'interaction jusqu'à ce que l'utilisateur décide de quitter.
     */
    private void menuAutomatique(){
        int choix = 0;
        
        while (choix != 3) {
            System.out.println("\n--- Menu Automatique (Mode Fichier) ---");
            System.out.println("1) Résolution automatique (Optimisation)");
            System.out.println("2) Sauvegarder la solution actuelle");
            System.out.println("3) Fin");
            System.out.print("Votre choix : ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine(); // Vider buffer
                switch (choix) {
                    case 1:
                    	try {
                    		System.out.println();
                    		System.out.println("Côut avant optimisation de l'algorithme : "+ String.format("%.4f", reseau.calculerCout()));
                    		System.out.println();
                    		
                            AlgoOptimiseur algo = new AlgoOptimiseur(reseau);
                            algo.resoudre(50000);
                    	} catch (ReseauException e) {
                    		System.out.println("Erreur lors de l'optimisation : " + e.getMessage());
                    	}
                    	
                        
                        break;

                    case 2:
                        sauvegarderSous();
                        break;

                    case 3:
                        System.out.println("Au revoir.");
                        break;

                    default:
                        System.out.println("Choix invalide.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur de saisie (entrez un chiffre).");
                scanner.nextLine();
            }
        }
    }

    /**
     * Demande à l'utilisateur un nom de fichier et sauvegarde l'état actuel du réseau.
     * Délègue l'opération d'écriture à la classe utilitaire GestionFichier.
     */
    private void sauvegarderSous() {
        System.out.print("Entrez le nom du fichier de sauvegarde : ");
        String nomFichier = scanner.nextLine();
        
        try {
            // DÉLÉGATION à GestionFichier pour l'écriture
            GestionFichier.ecrireFichier(this.reseau, nomFichier);
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }



    // partie 1
    /**
     * Lance le mode "Manuel".
     * Affiche la bannière d'accueil et redirige vers le menu de configuration principal.
     */
    public void demarrer(){
        System.out.println("==============================================");
        System.out.println("|| Programme de Gestion de Réseau Électrique ||");
        System.out.println("==============================================");
        menuPrincipal();
    }

    // Menu Configuration (Menu Principal)
    /**
     * Affiche le menu principal de configuration (Mode Manuel).
     * Permet à l'utilisateur de construire le réseau étape par étape (Générateurs, Maisons, Connexions).
     * Boucle tant que la configuration n'est pas finalisée et validée.
     */
    private void menuPrincipal(){
        int choix = 0;
        boolean configurationFinie = false;
        while (!configurationFinie) {
            System.out.println("\n--- Menu Configuration du Réseau ---");
            System.out.println("1) Ajouter/Mettre à jour un générateur");
            System.out.println("2) Ajouter/Mettre à jour une maison");
            System.out.println("3) Ajouter/Modifier une connexion (Maison <-> Générateur)");
            System.out.println("4) Supprimer une connexion (Maison <-> Générateur)");
            System.out.println("5) Finaliser la configuration et passer à la gestion");
            System.out.print("Votre choix: ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine();

                switch (choix) {
                    case 1:
                        ajouterGenerateur();
                        break;
                    case 2:
                        ajouterMaison();
                        break;
                    case 3:
                        ajouterConnexion();
                        break;
                    case 4:
                        supprimerConnexion();
                        break;
                    case 5:
                        configurationFinie = finaliserConfiguration();
                        break;
                    default:
                        System.out.println("Choix invalide. Veuillez saisir un nombre entre 1 et 5.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur: Veuillez entrer un nombre entier pour le choix du menu.");
                scanner.nextLine();
                choix = 0;
            }
        }
    }

    /**
     * Gère l'interaction utilisateur pour l'ajout ou la mise à jour d'un générateur.
     * Récupère la saisie, vérifie le format et appelle la méthode métier correspondante.
     * Affiche les erreurs éventuelles (format, capacité invalide) sans planter.
     */
    private void ajouterGenerateur() {
        System.out.print("Saisissez le nom du générateur et sa capacité maximale (format : G1 60): ");
        String ligne = scanner.nextLine().trim();
        String[] parties = ligne.split(" ");

        
        try {
        	if (parties.length != 2) {
                throw new ReseauException.Syntaxe("saisie", "[Nom] [Capacité]", 0);
            }

            String nom = parties[0];
            
            int capacite = Integer.parseInt(parties[1]);
            String resultat = reseau.ajouterOuMajGenerateur(nom, capacite);
            System.out.println(resultat);
            
        } catch (NumberFormatException e) {
            System.out.println("Erreur: La capacité maximale doit être un nombre entier.");
        } catch (ReseauException e) {
            // Erreur métier (Capacité négative, nom vide)
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    /**
     * Gère l'interaction utilisateur pour l'ajout ou la mise à jour d'une maison.
     * Vérifie que le type de consommation saisi est valide (BASSE, NORMAL, FORTE) avant l'ajout.
     */
    private void ajouterMaison() {
        System.out.print(
                "Saisissez le nom de la maison et son type de consommation (format : M1 NORMAL). Types: BASSE, NORMAL, FORTE: ");
        String ligne = scanner.nextLine().trim();
        String[] parties = ligne.split(" ");
        
        try {
	        if (parties.length != 2) {
	        	throw new ReseauException.Syntaxe("saisie", "[Nom] [TypeConsommation]", 0);
	        }
	
	        String nom = parties[0];
	        String typeStr = parties[1].toUpperCase();
	
	        TypeConsommation type = TypeConsommation.fromString(typeStr);
	
	        if (type == null) {
	        	throw new ReseauException.DonneeInvalide(typeStr, "BASSE, NORMAL, ou FORTE", 0);
	        }
	        
	        String resultat = reseau.ajouterOuMajMaison(nom, type);
	        System.out.println(resultat);
	                
	    } catch (ReseauException e) {
	    	System.out.println(e.getMessage());
	      }
	        
    }

    /**
     * Gère l'interaction utilisateur pour la création d'une connexion.
     * Demande les deux noms (Maison/Générateur) et tente de créer le lien dans le réseau.
     */
    private void ajouterConnexion() {
        System.out.print("Saisissez la connexion (format: M1 G1 ou G1 M1): ");
        String ligne = scanner.nextLine().trim();
        String[] parties = ligne.split(" ");

        try {
            if (parties.length != 2) {
                throw new ReseauException.Syntaxe("saisie", "[Nom1] [Nom2]", 0);
            }

	        String nom1 = parties[0];
	        String nom2 = parties[1];
	
	        String resultat = reseau.ajouterConnexion(nom1, nom2);
	        System.out.println(resultat);
	        
        } catch(ReseauException e) {
        	System.out.println(e.getMessage());
        }
    }

    /**
     * Gère l'interaction utilisateur pour la suppression d'une connexion existante.
     */
    private void supprimerConnexion() {
        System.out.print("Saisissez la connexion à supprimer (format: M1 G1 ou G1 M1): ");
        String ligne = scanner.nextLine().trim();
        String[] parties = ligne.split(" ");

        try {
            if (parties.length != 2) {
                throw new ReseauException.Syntaxe("saisie", "[Nom1] [Nom2]", 0);
            }

	        String nom1 = parties[0];
	        String nom2 = parties[1];
	
	        String resultat = reseau.suppConnexion(nom1, nom2);
	        System.out.println(resultat);
	        
        }catch(ReseauException e) {
        	System.out.println(e.getMessage());
        }
    }

    /**
     * Permet de modifier une connexion existante en deux étapes :
     * 1. Vérifie que l'ancienne connexion existe.
     * 2. Demande la nouvelle connexion et l'applique (mise à jour ou création).
     * 3. Supprime l'ancienne PUIS ajoute la nouvelle.
     */
    private void modifierConnexion() {

        System.out.print("Veuillez saisir la connexion que vous souhaitez modifier : ");
        // trim sert a retirer les espaces au debut et a la fin comme sur " g1 m1 "
        String ligneAncienne = scanner.nextLine().trim();

        // Vérification du format de l'ancienne connexion
        String[] partiesAnciennes = ligneAncienne.split(" ");
        try {
	        if (partiesAnciennes.length != 2) {
	        	throw new ReseauException.Syntaxe("saisie de l'ancienne connexion", "[Nom1] [Nom2]", 0);
	        }
	
	        String anc1 = partiesAnciennes[0];
            String anc2 = partiesAnciennes[1];
            
	        // vérifiacation si la connnexion existe
	        boolean existe = reseau.isConnexionExiste(partiesAnciennes[0], partiesAnciennes[1]);
	        if (!existe) {
	        	throw new ReseauException.ElementIntrouvable("connexion", ligneAncienne);
	        }
	
	        System.out.print("Veuillez saisir la nouvelle connexion : ");
	        String ligneNouvelle = scanner.nextLine().trim();
	
	        // Vérification du format de la nouvelle connexion
	        String[] partiesNouvelles = ligneNouvelle.split(" ");
	        if (partiesNouvelles.length != 2) {
	            throw new ReseauException.Syntaxe("saisie de la nouvelle connexion", "[Nom1] [Nom2]", 0);
	        }
	
	        String nouv1 = partiesNouvelles[0];
            String nouv2 = partiesNouvelles[1];
	        
	        boolean gardeElement1 = nouv1.equals(anc1) || nouv1.equals(anc2);
            boolean gardeElement2 = nouv2.equals(anc1) || nouv2.equals(anc2);

            if (!gardeElement1 && !gardeElement2) {
                throw new ReseauException("La nouvelle connexion doit conserver au moins un élément (Maison ou Générateur) de l'ancienne !");
            }
	
            //reseau.suppConnexion(anc1, anc2);
	        // Le réseau.ajouterConnexion() gère la validation et la mise à jour si la
	        // Maison existe déjà.
	        String resultat = reseau.ajouterConnexion(nouv1 , nouv2);
	
	        // Afficher le résultat, comme toutes les erreurs sont gérées par les try catch il ne reste donc que le cas ou c'est une réussite
	        System.out.println("Modification réussie: " + resultat.replace("MAJ:", "").replace("OK:", "").trim());
	        
        }catch (ReseauException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Tente de valider la configuration actuelle du réseau pour passer au mode Gestion.
     * Vérifie les règles métier (au moins 1 maison, 1 générateur, tout est connecté).
     *
     * @return true si le réseau est valide et qu'on peut changer de menu, false sinon
     */
    private boolean finaliserConfiguration() {
        

        try {
        	reseau.validerReseau();
            System.out.println("\nConfiguration terminée, le réseau est valide.");
            menuGestion();
            return true;
            
        } catch (ReseauException e) {
        	System.out.println("\n" + e.getMessage());
            System.out.println("Veuillez corriger les problèmes avant de continuer.");
            return false;
        }
    }

    // Menu Gestion (Menu Opérations)
    /**
     * Affiche le menu de gestion (Opérations).
     * Accessible uniquement une fois le réseau validé.
     * Permet de calculer les coûts, modifier des connexions à la volée ou visualiser le réseau.
     */
    private void menuGestion() {
        int choix = 0;
        while (choix != 4) {
            System.out.println("\n--- Menu Gestion du Réseau ---");
            System.out.println("1) Calculer le coût du réseau électrique actuel");
            System.out.println("2) Modifier une connexion");
            System.out.println("3) Afficher le réseau");
            System.out.println("4) Quitter le programme");
            System.out.print("Votre choix: ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine();

                switch (choix) {
                    case 1:
                        calculerCout();
                        break;
                    case 2:
                        modifierConnexion();
                        break;
                    case 3:
                        reseau.afficherReseau();
                        break;
                    case 4:
                        System.out.println("\nProgramme terminé. Au revoir.");
                        break;
                    default:
                        System.out.println("Choix invalide. Veuillez saisir un nombre entre 1 et 4.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Erreur: Veuillez entrer un nombre entier pour le choix du menu.");
                scanner.nextLine();
                choix = 0;
            }
        }
        scanner.close();
    }

    /**
     * Lance les calculs de performance du réseau (Dispersion, Surcharge, Coût total).
     * Affiche les résultats formatés avec 4 décimales.
     * Attrape les erreurs de calcul (ex: division par zéro si un générateur a une capacité de 0).
     */
    private void calculerCout() {
    	try {
    		double disp = reseau.calculerDispersion();
    		double surcharge = reseau.calculerSurcharge();
    		double cout = reseau.calculerCout();

	        System.out.println("\n--- Résultat du Calcul de Coût (Lambda = " + reseau.getLambda() + ") ---");
	        System.out.printf("Dispersion des écarts : %.4f\n", disp);
	        System.out.printf("Pénalisation de la surcharge : %.4f\n", surcharge);
	        System.out.printf("Coût total : %.4f\n", cout);
	        System.out.println("-------------------------------------------------");
        }catch (ReseauException e) {
            System.out.println("\nERREUR DE CALCUL :");
            System.out.println(">> " + e.getMessage());
            System.out.println("Vérifiez qu'aucun générateur n'a une capacité de 0 kW.");
        }
    }
}