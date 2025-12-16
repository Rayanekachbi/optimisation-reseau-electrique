========================================================================
PROJET : GESTION DE RÉSEAU ÉLECTRIQUE
AUTEURS : RAYANE KACHBI & MUHAMMAD ABBAS
========================================================================

1. EXÉCUTION DU PROGRAMME
=========================
Pré-requis :
- Java JDK 17 ou supérieur.
- Bibliothèque JavaFX (si lancement hors d'un IDE configuré).
- Les fichiers compilés (.class) doivent être dans le dossier "bin".
- Le dossier "icones" doit être à la racine du projet pour l'affichage graphique.

A. Lancement de l'Interface Graphique (Recommandé) :
----------------------------------------------------
La classe principale est : packet.javafx.MainApp

Commande (si JavaFX est configuré dans le classpath) :
java -cp bin packet.javafx.MainApp

B. Lancement en Mode Texte (Console) :
--------------------------------------
La classe principale est : packet.java.InterfaceTexte

Commande Mode Manuel :
java -cp bin packet.java.InterfaceTexte

Commande Mode Fichier (Automatique) :
java -cp bin packet.java.InterfaceTexte <CheminFichier> <Lambda>

Exemple :
java -cp bin packet.java.InterfaceTexte Instances-20251127/instance7.txt 10.0


2. ALGORITHME DE RÉSOLUTION
===========================
Un algorithme d'optimisation avancé a été implémenté : le Recuit Simulé.

a) Initialisation Intelligente (Gloutonne) :
   Avant de lancer l'optimisation, le programme trie les maisons par consommation décroissante et les connecte au générateur ayant le taux d'utilisation le plus faible à l'instant T.

b) Recuit Simulé :
   L'algorithme effectue une boucle (50 000 itérations) :
   1. Perturbation : Changement aléatoire d'une connexion.
   2. Évaluation : Calcul du Delta de coût.
   3. Décision (Critère de Metropolis) : On accepte les améliorations, et parfois les dégradations (selon la Température) pour éviter les minima locaux.
   4. Refroidissement : La température diminue progressivement.


3. FONCTIONNALITÉS IMPLÉMENTÉES
===============================
Toutes les fonctionnalités obligatoires et les bonus (Partie 2) ont été implémentés.

Fonctionnalités Principales :
[x] Modélisation orientée objet (Maison, Generateur, Reseau).
[x] Gestion robuste des exceptions (ReseauException).
[x] Lecture et Sauvegarde de fichiers de configuration (.txt).
[x] Algorithme d'optimisation (Recuit Simulé).
[x] Calculs de coûts (Dispersion, Surcharge) avec paramètre Lambda modifiable.

Fonctionnalités Bonus (Partie 2) :
[x] Interface Graphique JavaFX complète et intuitive.
[x] Visualisation graphique du graphe (Noeuds et Arêtes).
[x] Tests Unitaires complets (JUnit 5) couvrant la logique métier, la gestion de fichiers et les cas limites.

4. STRUCTURE DU PROJET
======================
- src/packet/java/    : Contient la logique métier (Reseau, Algo, Main Console).
- src/packet/javafx/  : Contient l'interface graphique (Vues, Main JavaFX).
- tests/              : Contient les tests unitaires JUnit.
- Instances-20251127/ : Contient les fichiers de test (.txt).
- icones/             : Contient les ressources graphiques.


5. SOURCES ET RÉFÉRENCES
========================
L'algorithme d'optimisation (Recuit Simulé) implémenté dans ce projet est basé sur les travaux de :

• Article fondateur :
  S. Kirkpatrick, C. D. Gelatt et M. P. Vecchi, "Optimization by Simulated Annealing", 
  Science, vol. 220, n° 4598, pp. 671–680, 1983.

• Ressource web explicative :
  "Le recuit simulé", Interstices.info (INRIA), 2009.
  Disponible sur : https://interstices.info/le-recuit-simule/