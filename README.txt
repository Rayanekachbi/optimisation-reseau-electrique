========================================================================
PROJET : GESTION DE RÉSEAU ÉLECTRIQUE DE RAYANE KACHBI & MUHAMMAD ABBAS
========================================================================

1. EXÉCUTION DU PROGRAMME
=========================
La classe principale contenant la méthode main est : packet.InterfaceTexte

Pré-requis :
Les fichiers sources (.java) doivent être compilés. Par exemple, si les fichiers compilés (.class) sont dans un dossier "bin" :

A. Lancement en Mode Manuel (Menu interactif) :
-----------------------------------------------
Commande : java -cp bin packet.InterfaceTexte

B. Lancement en Mode Fichier (Automatique) :
--------------------------------------------
Commande : java -cp bin packet.InterfaceTexte <CheminFichier> <Lambda>

Exemple concret (comme testé) :
java -cp bin packet.InterfaceTexte Instances-20251127/instance7.txt 10.0

Détail des arguments :
- <CheminFichier> : Le chemin relatif ou absolu vers le fichier de configuration (ex: instance7.txt).
- <Lambda> : (Optionnel) Le coefficient de pénalité pour la surcharge (double). Par défaut = 10.0.


2. ALGORITHME DE RÉSOLUTION
===========================
Un algorithme d'optimisation avancé a été implémenté : le Recuit Simulé (sources citées tout en bas).

Il est bien plus efficace que l'approche aléatoire simple car il permet d'éviter les minima locaux. Voici son fonctionnement :

a) Initialisation Intelligente (Gloutonne) :
   Avant de lancer l'optimisation, le programme ne connecte pas les maisons au hasard.
   1. Il trie les maisons par consommation décroissante (les plus gourmandes en premier).
   2. Il connecte chaque maison au générateur ayant le taux d'utilisation le plus faible à l'instant T.
   Cela donne une solution de départ déjà très correcte.

b) Recuit Simulé :
   L'algorithme effectue ensuite une boucle (ex: 50 000 itérations) :
   1. Perturbation : On change la connexion d'une maison au hasard vers un autre générateur.
   2. Évaluation : On calcule la différence de coût (Delta).
   3. Décision (Critère de Metropolis) :
      - Si le nouveau coût est meilleur (Delta < 0) : On garde la solution.
      - Si le nouveau coût est pire : On peut quand même l'accepter avec une probabilité dépendante de la "Température" actuelle. Cela permet de sortir des blocages.
   4. Refroidissement : La température diminue progressivement, réduisant la probabilité d'accepter des mauvaises solutions vers la fin.


3. FONCTIONNALITÉS IMPLÉMENTÉES
===============================
Toutes les fonctionnalités demandées dans le sujet ont été implémentées :

[x] Modélisation orientée objet (Maison, Generateur, Reseau).
[x] Gestion robuste des exceptions (ReseauException avec détails et numéros de lignes).
[x] Lecture de fichiers de configuration avec vérification de syntaxe et d'ordre.
[x] Sauvegarde de l'état du réseau dans un fichier .txt valide.
[x] Calculs de coûts (Charge, Taux, Dispersion, Surcharge).
[x] Mode Manuel complet (Ajout, Modification, Suppression, Validation).
[x] Mode Fichier avec passage d'arguments en ligne de commande.
[x] Javadoc complète fournie pour toutes les classes.

État actuel :
Le programme est fonctionnel et gère les erreurs de saisie ou de logique (ex: division par zéro, fichiers introuvables) sans planter.
Aucune fonctionnalité n'est manquante.


4. SOURCES ET RÉFÉRENCES
========================
L'algorithme d'optimisation (Recuit Simulé) implémenté dans ce projet est basé sur les travaux de :

• Article fondateur :
  S. Kirkpatrick, C. D. Gelatt et M. P. Vecchi, "Optimization by Simulated Annealing", 
  Science, vol. 220, n° 4598, pp. 671–680, 1983.

• Ressource web explicative :
  "Le recuit simulé", Interstices.info (INRIA), 2009.
  Disponible sur : https://interstices.info/le-recuit-simule/