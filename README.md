# Projet de GL - Gestion d'un restaurant

# Base de données

Pour le stockage des données, nous avons utilisé une base de données relationnelle sous PostgreSQL.

## Diagramme de classes

![Diagramme de classes](https://imgur.com/hAkzK7t.png)

***Une table est unique et accueille plusieurs repas dans le temps.***  
***Chaque repas peut avoir plusieurs commandes et une facture.***

# Pré-requis

Avant de lancer l'application, assurez-vous d'avoir les éléments suivants : 
- une base de données PostgreSQL de prod. pour utiliser l'application,
- une base de données PostgreSQL de tests pour lancer les tests unitaires,
- un fichier `config.properties` à la racine du projet qui indique si vous utilisez la DB de prod. ou de tests,
- un fichier `db.properties` à la racine du projet qui doit décrire l'url vers votre DB ainsi que vos identifiants (pour la DB de prod.),
- un fichier `db.test.properties` à la racine du projet qui doit décrire l'url vers votre DB ainsi que vos identifiants (pour la DB de tests),

## Structure du fichier `config.properties`

```properties
environment=test # ou environment=prod
```

## Structure des fichiers `db.properties` et `db.test.properties`

```properties
url=<url vers votre DB>   # exemple : jdbc:postgresql://localhost:5432/restaurant_g11
username=<identifiant de connexion à la DB>
password=<mot de passe de connexion à la DB>
```
