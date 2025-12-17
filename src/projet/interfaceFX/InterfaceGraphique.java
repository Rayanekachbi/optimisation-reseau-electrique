package projet.interfaceFX;

import javafx.geometry.Insets;   
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import projet.algo.AlgoOptimiseur;
import projet.io.GestionFichier;
import projet.reseau.Reseau;
import projet.reseau.TypeConsommation;

/**
 * Conteneur principal de l'interface graphique du réseau.
 * Cette classe hérite de BorderPane et organise l'écran en trois zones :
 * - Haut : Barre de messages (Erreurs/Infos).
 * - Centre : Visualisation graphique du réseau (VueReseau).
 * - Bas : Barre de boutons dynamique qui change selon le contexte (Configuration, Gestion, Automatique).
 */
public class InterfaceGraphique extends BorderPane {

    private Reseau reseau;
    private VueReseau vueReseau;
    private Label messageLabel;
    private HBox bottomBar;
    private boolean modeFichier;

    // Constructeur
    /**
     * Initialise l'interface graphique principale.
     * Configure la vue centrale, la barre de message et charge le menu approprié 
     * selon le mode de lancement choisi (Importation de fichier ou Création manuelle).
     *
     * @param reseau L'objet Reseau contenant les données à afficher et manipuler
     * @param modeFichier Indique si l'application est en mode "Fichier" (true) ou "Manuel" (false)
     */
    public InterfaceGraphique(Reseau reseau, boolean modeFichier) {
        this.reseau = reseau;
        this.modeFichier = modeFichier;
        
        // Vue Centrale
        this.vueReseau = new VueReseau(reseau);
        this.setCenter(vueReseau);

        // Barre du Haut (Messages)
        messageLabel = new Label(modeFichier ? "Mode Fichier chargé." : "Mode Manuel : Configuration du réseau.");
        messageLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox topBar = new HBox(messageLabel);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #2b2b2b;");
        this.setTop(topBar);

        // Barre du Bas
        this.bottomBar = new HBox(15);
        this.bottomBar.setPadding(new Insets(15));
        this.bottomBar.setAlignment(Pos.CENTER);
        this.bottomBar.setStyle("-fx-background-color: #3c3f41; -fx-border-color: #555; -fx-border-width: 1 0 0 0;");
        this.setBottom(bottomBar);
        
        // Style global
        this.setStyle("-fx-background-color: #2b2b2b;");

        // Charger le bon menu au démarrage
        if (this.modeFichier) {
            chargerMenuAutomatique();
        } else {
            chargerMenuConfigurationManuel();
        }
        
        // Premier rafraîchissement visuel
        vueReseau.rafraichir();
    }

    /**
     * Configure la barre de boutons pour le mode "Fichier" (Automatique).
     * Propose les options de résolution algorithmique, de sauvegarde et de fermeture.
     * Les actions sont exécutées dans des threads séparés pour ne pas bloquer l'interface.
     */
    private void chargerMenuAutomatique() {
        bottomBar.getChildren().clear(); 

        Button btnResoudre = creerBouton("Résolution Automatique");
        Button btnSauvegarder = creerBouton("Sauvegarder");
        Button btnFin = creerBouton("Fin");

        btnResoudre.setOnAction(e -> {
            afficherMessage("Optimisation en cours... Veuillez patienter.", false);
            
            // On lance le calcul dans un Thread séparé pour ne pas bloquer l'interface
            new Thread(() -> {
                try {
                    // On lance l'algorithme
                    AlgoOptimiseur algo = new AlgoOptimiseur(reseau);
                    algo.resoudre(50000); 
                    
                    // Une fois fini, on met à jour l'interface (sur le thread JavaFX principal)
                    Platform.runLater(() -> {
                        vueReseau.rafraichir(); 
                        // On redessine les nouvelles connexions
                        
                        try {
                            double nouveauCout = reseau.calculerCout();
                            afficherMessage("Optimisation terminée ! Nouveau coût : " + String.format("%.9f", nouveauCout), false);
                        } catch (Exception ex) {
                            afficherMessage(ex.getMessage(), true);
                        }
                    });
                    
                } catch (Exception ex) {
                    Platform.runLater(() -> afficherMessage(ex.getMessage(), true));
                }
            }).start();
        });

        //SAUVEGARDE
        btnSauvegarder.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sauvegarder le réseau");
            
            // Filtre pour les fichiers texte
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Texte", "*.txt"));
            // Nom par défaut
            fileChooser.setInitialFileName("solution_optimisee.txt"); 
            
            // Dossier par défaut (racine du projet)
            File dossierCourant = new File(".");
            if(dossierCourant.exists()) fileChooser.setInitialDirectory(dossierCourant);

            // Ouvrir la boîte de dialogue de sauvegarde
            File fichier = fileChooser.showSaveDialog(this.getScene().getWindow());
            
            if (fichier != null) {
                try {
                    GestionFichier.ecrireFichier(reseau, fichier.getAbsolutePath());
                    afficherMessage("Succès : Réseau sauvegardé dans " + fichier.getName(), false);
                } catch (Exception ex) {
                    afficherMessage(ex.getMessage(), true);
                }
            }
        });
        
        // FIN
        btnFin.setOnAction(e -> System.exit(0));

        bottomBar.getChildren().addAll(btnResoudre, btnSauvegarder, btnFin);
    }

    /**
     * Configure la barre de boutons pour la phase 1 du mode Manuel (Configuration).
     * Permet l'ajout et la suppression d'éléments (Générateurs, Maisons, Connexions)
     * et la validation finale du réseau.
     */
    private void chargerMenuConfigurationManuel() {
        bottomBar.getChildren().clear();
        afficherMessage("Mode Configuration : Ajoutez vos éléments.", false);

        Button btnGen = creerBouton("Ajouter Générateur");
        Button btnMaison = creerBouton("Ajouter Maison");
        Button btnConnexion = creerBouton("Ajouter Connexion");
        Button btnSuppr = creerBouton("Supprimer Connexion");
        Button btnFinaliser = creerBouton("Finaliser Configuration");
        
        btnFinaliser.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        btnGen.setOnAction(e -> ouvrirFormulaireGenerateur());
        btnMaison.setOnAction(e -> ouvrirFormulaireMaison());
        btnConnexion.setOnAction(e -> ouvrirFormulaireConnexion(false)); // false = mode ajout
        btnSuppr.setOnAction(e -> ouvrirFormulaireConnexion(true));      // true = mode suppression
        
        btnFinaliser.setOnAction(e -> {
            try {
                reseau.validerReseau();
                afficherMessage("Réseau validé ! Passage au mode gestion.", false);
                chargerMenuGestionManuel(); 
            } catch (Exception ex) {
                afficherMessage(ex.getMessage(), true);
            }
        });

        bottomBar.getChildren().addAll(btnGen, btnMaison, btnConnexion, btnSuppr, btnFinaliser);
    }

    /**
     * Configure la barre de boutons pour la phase 2 du mode Manuel (Gestion).
     * Accessible uniquement après validation du réseau. Permet le calcul des coûts 
     * et la modification des connexions.
     */
    private void chargerMenuGestionManuel() {
        bottomBar.getChildren().clear();
        afficherMessage("Mode Gestion : Optimisez votre réseau.", false);

        Button btnCout = creerBouton("Calculer Coût");
        Button btnModif = creerBouton("Modifier Connexion");
        Button btnQuitter = creerBouton("Quitter");

        btnCout.setOnAction(e -> {
            try {
                double cout = reseau.calculerCout();
                afficherMessage("Coût total actuel : " + String.format("%.4f", cout), false);
            } catch (Exception ex) {
                afficherMessage(ex.getMessage(), true);
            }
        });

        btnModif.setOnAction(e -> ouvrirFormulaireModification());
        
        btnQuitter.setOnAction(e -> System.exit(0));

        bottomBar.getChildren().addAll(btnCout, btnModif, btnQuitter);
    }
    
    
    // utilitaire !!!
    /**
     * Méthode utilitaire pour créer un bouton avec le style visuel de l'application.
     * Applique les couleurs (Dark mode), les bordures et les effets de survol.
     *
     * @param texte Le texte à afficher dans le bouton
     * @return Le bouton configuré et stylisé
     */
    private Button creerBouton(String texte) {
        Button b = new Button(texte);
        // Style gris par défaut
        b.setStyle("-fx-background-color: #505354; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-border-radius: 5; -fx-background-radius: 5;");
        b.setMinWidth(130);
        
        // Effet Hover simple
        b.setOnMouseEntered(e -> {
        	// le style du bouton finalisé est ignoré
            if (!texte.contains("Finaliser")) 
                b.setStyle("-fx-background-color: #6a6d6e; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;");
        });
        b.setOnMouseExited(e -> {
            if (!texte.contains("Finaliser"))
                b.setStyle("-fx-background-color: #505354; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;");
        });
        
        return b;
    }

    /**
     * Affiche un message d'information ou d'erreur dans la barre supérieure de la fenêtre.
     * La couleur du texte s'adapte au type de message (Rouge pour erreur, Vert pour info).
     *
     * @param texte Le contenu du message
     * @param estErreur True si c'est un message d'erreur (rouge), False pour une info (vert)
     */
    public void afficherMessage(String texte, boolean estErreur) {
        messageLabel.setText(texte);
        if (estErreur) {
            messageLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
    }
    
    /**
     * Ouvre une fenêtre contenant le formulaire d'ajout ou de mise à jour d'un générateur.
     * Gère la saisie du nom et de la capacité, ainsi que la validation des données.
     */
    private void ouvrirFormulaireGenerateur() {
        Stage fenetre = creerFenetreSaisie("Nouveau Générateur");
        VBox contenu = (VBox) fenetre.getScene().getRoot();

        TextField champNom = new TextField();
        champNom.setPromptText("Nom (ex: Gen1)");
        
        TextField champCapacite = new TextField();
        champCapacite.setPromptText("Capacité en kW (ex: 60)");

        Button btnValider = creerBouton("Valider");
        btnValider.setOnAction(e -> {
            try {
                String nom = champNom.getText();
                int cap = Integer.parseInt(champCapacite.getText());
                
                // Appel Logique
                String resultat = reseau.ajouterOuMajGenerateur(nom, cap);
                
                // Mise à jour Vue
                vueReseau.rafraichir();
                afficherMessage(resultat, false);
                fenetre.close();
            } catch (NumberFormatException nfe) {
                afficherMessage("La capacité doit être un nombre entier !", true);
            } catch (Exception ex) {
                afficherMessage(ex.getMessage(), true);
            }
        });

        contenu.getChildren().addAll(new Label("Nom :"), champNom, new Label("Capacité :"), champCapacite, btnValider);
        fenetre.showAndWait();
    }
    
    /**
     * Ouvre une fenêtre contenant le formulaire d'ajout ou de mise à jour d'une maison.
     * Utilise une ComboBox pour forcer la sélection d'un type de consommation valide.
     */
    private void ouvrirFormulaireMaison() {
        Stage fenetre = creerFenetreSaisie("Nouvelle Maison");
        VBox contenu = (VBox) fenetre.getScene().getRoot();

        TextField champNom = new TextField();
        champNom.setPromptText("Nom (ex: Maison1)");

        // ComboBox pour éviter les erreurs de frappe sur le type
        ComboBox<TypeConsommation> comboType = new ComboBox<>();
        comboType.getItems().addAll(TypeConsommation.values());
        comboType.setValue(TypeConsommation.NORMAL);
        comboType.setStyle("-fx-background-color: white;");

        Button btnValider = creerBouton("Valider");
        btnValider.setOnAction(e -> {
            try {
                String nom = champNom.getText();
                TypeConsommation type = comboType.getValue();
                
                String resultat = reseau.ajouterOuMajMaison(nom, type);
                
                vueReseau.rafraichir();
                afficherMessage(resultat, false);
                fenetre.close();
            } catch (Exception ex) {
                afficherMessage(ex.getMessage(), true);
            }
        });

        contenu.getChildren().addAll(new Label("Nom :"), champNom, new Label("Type :"), comboType, btnValider);
        fenetre.showAndWait();
    }
    
    /**
     * Ouvre une fenêtre pour gérer les connexions entre éléments.
     * Cette méthode sert à la fois pour l'ajout et la suppression de connexions.
     *
     * @param isSuppression True pour ouvrir le formulaire en mode "Suppression", False pour "Ajout"
     */
    private void ouvrirFormulaireConnexion(boolean isSuppression) {
        String titre = isSuppression ? "Supprimer une connexion" : "Créer une connexion";
        Stage fenetre = creerFenetreSaisie(titre);
        VBox contenu = (VBox) fenetre.getScene().getRoot();

        TextField champNom1 = new TextField();
        champNom1.setPromptText("Nom élément 1 (ex: Gen1)");
        
        TextField champNom2 = new TextField();
        champNom2.setPromptText("Nom élément 2 (ex: Maison1)");

        Button btnValider = creerBouton(isSuppression ? "Supprimer" : "Connecter");
        // On change la couleur si c'est une suppression (Rouge)
        if (isSuppression) btnValider.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");

        btnValider.setOnAction(e -> {
            try {
                String n1 = champNom1.getText();
                String n2 = champNom2.getText();
                String res;
                
                if (isSuppression) {
                    res = reseau.suppConnexion(n1, n2);
                } else {
                    res = reseau.ajouterConnexion(n1, n2);
                }
                
                vueReseau.rafraichir();
                afficherMessage(res, false);
                fenetre.close();
            } catch (Exception ex) {
                afficherMessage(ex.getMessage(), true);
            }
        });

        contenu.getChildren().addAll(new Label("Élément A :"), champNom1, new Label("Élément B :"), champNom2, btnValider);
        fenetre.showAndWait();
    }
    
    /**
     * Ouvre une fenêtre pour modifier une connexion existante.
     * Applique les règles de gestion strictes : conservation d'un élément commun,
     * vérification de l'existence de l'ancienne connexion.
     */
    private void ouvrirFormulaireModification() {
        Stage fenetre = creerFenetreSaisie("Modifier une connexion");
        VBox contenu = (VBox) fenetre.getScene().getRoot();

        TextField champAncien1 = new TextField();
        champAncien1.setPromptText("Ancienne connexion - Nom 1");
        TextField champAncien2 = new TextField();
        champAncien2.setPromptText("Ancienne connexion - Nom 2");
        
        Label separateur = new Label("--- Vers ---");
        separateur.setStyle("-fx-text-fill: yellow;");

        TextField champNouveau1 = new TextField();
        champNouveau1.setPromptText("Nouvelle connexion - Nom 1");
        TextField champNouveau2 = new TextField();
        champNouveau2.setPromptText("Nouvelle connexion - Nom 2");

        Button btnValider = creerBouton("Modifier");
        
        btnValider.setOnAction(e -> {
            try {
                // On récupère et on nettoie les entrées
                String anc1 = champAncien1.getText().trim();
                String anc2 = champAncien2.getText().trim();
                String nouv1 = champNouveau1.getText().trim();
                String nouv2 = champNouveau2.getText().trim();

                // On vérifie que la nouvelle connexion conserve au moins un élément de l'ancienne
                boolean gardeElement1 = nouv1.equals(anc1) || nouv1.equals(anc2);
                boolean gardeElement2 = nouv2.equals(anc1) || nouv2.equals(anc2);

                if (!gardeElement1 && !gardeElement2) {
                    throw new Exception("La modification doit conserver au moins un élément (Maison ou Générateur) !");
                }

                if (!reseau.isConnexionExiste(anc1, anc2)) {
                    throw new Exception("L'ancienne connexion n'existe pas !");
                }

                String res = reseau.ajouterConnexion(nouv1, nouv2);
                
                // Mise à jour de l'interface
                vueReseau.rafraichir();
                afficherMessage("Modification réussie : " + res, false);
                fenetre.close();
                
            } catch (Exception ex) {
                // En cas d'erreur, on l'affiche en rouge en haut de la fenêtre principale
                afficherMessage(ex.getMessage(), true);
            }
        });

        contenu.getChildren().addAll(
            new Label("À remplacer :"), champAncien1, champAncien2, 
            separateur, 
            new Label("Par :"), champNouveau1, champNouveau2, 
            btnValider
        );
        
        fenetre.showAndWait();
    }
    
    /**
     * Méthode utilitaire pour initialiser une fenêtre (Stage) secondaire.
     * Configure la modalité (bloque la fenêtre principale), l'icône, le titre et le style CSS de base
     * pour assurer la cohérence visuelle avec le reste de l'application.
     *
     * @param titre Le titre de la fenêtre
     * @return Une instance de Stage prête à être remplie et affichée
     */
    private Stage creerFenetreSaisie(String titre) {
        Stage fenetre = new Stage();
        try {
            Image icon = new Image("file:icones/generateur.png"); 
            fenetre.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Erreur : Impossible de charger l'icône (vérifie le chemin).");
        }
        fenetre.initModality(Modality.APPLICATION_MODAL); // Bloque la fenêtre principale
        fenetre.setTitle(titre);
        
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #333333;");

        // Style par défaut des labels dans la fenêtre
        layout.getChildren().addListener((javafx.collections.ListChangeListener<javafx.scene.Node>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (javafx.scene.Node node : change.getAddedSubList()) {
                        if (node instanceof Label) {
                            ((Label) node).setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                        }
                    }
                }
            }
        });

        Scene scene = new Scene(layout, 300, 350); 
        fenetre.setScene(scene);
        return fenetre;
    }
    
    
}