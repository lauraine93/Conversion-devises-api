# API de Conversion de Devises — Documentation Complète

<img width="1897" height="981" alt="image" src="https://github.com/user-attachments/assets/5bcb5ab8-c6bd-487d-ae30-9f3d724ae108" />


##  Table des matières

1. [Présentation du projet](#1-présentation-du-projet)
2. [Stack technologique](#2-stack-technologique)
3. [Prérequis](#3-prérequis)
4. [Installation et démarrage](#4-installation-et-démarrage)
5. [Configuration](#5-configuration)
6. [Architecture du projet](#6-architecture-du-projet)
7. [Endpoints API](#7-endpoints-api)
8. [Codes de réponse HTTP](#8-codes-de-réponse-http)
9. [Système de cache](#9-système-de-cache)
10. [Gestion des erreurs](#10-gestion-des-erreurs)
11. [Exemples de requêtes](#11-exemples-de-requêtes)
12. [Tester l'API via Swagger](#12-tester-lapi-via-swagger)
13. [Tester l'API via cURL](#13-tester-lapi-via-curl)
14. [Limitations et améliorations futures](#14-limitations-et-améliorations-futures)

---

## 1. Présentation du projet

Cette API permet de **convertir une somme d'argent d'une devise à une autre** en utilisant des taux de change récupérés dynamiquement depuis l'API externe **ExchangeRate-API**.

### Fonctionnalités principales

- **Conversion de devises** : Endpoint REST prenant en entrée la devise source, la devise cible et le montant
- **Taux de change en temps réel** : Appel à l'API ExchangeRate-API via Spring WebClient
- **Cache intelligent** : Les taux de change sont mis en cache pendant 5 minutes grâce à Caffeine Cache
- **Documentation OpenAPI/Swagger** : Documentation interactive disponible via Swagger UI
- **Gestion robuste des erreurs** : Codes HTTP appropriés avec messages explicites

---

## 2. Stack technologique

| Technologie | Version | Rôle |
|-------------|---------|------|
| **Java** | 21 | Langage de programmation |
| **Spring Boot** | 4.1.1 | Framework backend |
| **Spring WebFlux** | (via starter) | Client HTTP réactif (WebClient) |
| **Spring Cache** | (via starter) | Abstraction du cache |
| **Caffeine** | 3.1.8 | Implémentation du cache haute performance |
| **SpringDoc OpenAPI** | 2.6.0 | Documentation Swagger |
| **Lombok** | (via starter) | Réduction de code boilerplate |
| **Jakarta Validation** | (via starter) | Validation des entrées |

---

## 3. Prérequis

- **Java 21** ou supérieur
- **Maven 3.8+** (ou utiliser le wrapper `mvnw`)
- **Clé API ExchangeRate-API** (gratuite sur [exchangerate-api.com](https://www.exchangerate-api.com/))

---

## 4. Installation et démarrage

### 4.1 Cloner le projet

```bash
git clone <URL_DU_DEPOT>
cd Conversion-de-Devises
```

### 4.2 Configurer la clé API

La clé API ne doit **jamais** être écrite en dur dans `application.properties` (ce fichier est versionné sur GitHub). Elle se configure via la variable d'environnement `EXCHANGE_API_KEY` :

```bash
# Linux / Mac
export EXCHANGE_API_KEY=votre_cle_api

# Windows (PowerShell)
$env:EXCHANGE_API_KEY="votre_cle_api"
```

Une clé gratuite est disponible sur [exchangerate-api.com](https://www.exchangerate-api.com/).

Sans cette variable définie, l'application démarre quand même (valeur par défaut `CHANGE_ME`), mais tout appel à `/api/v1/convert` échouera avec une erreur `502 Bad Gateway` tant qu'une vraie clé n'est pas fournie.

### 4.3 Compiler le projet

```bash
./mvnw clean compile
```

### 4.4 Lancer l'application

```bash
./mvnw spring-boot:run
```

L'application démarre sur le **port 8085**.

### 4.5 Vérifier le démarrage

L'application est accessible à l'adresse :

- **Swagger UI** : http://localhost:8085/swagger-ui.html
- **API Docs (JSON)** : http://localhost:8085/api-docs

---

## 5. Configuration

### Paramètres de configuration (`application.properties`)

| Paramètre | Valeur par défaut | Description |
|-----------|-------------------|-------------|
| `server.port` | `8085` | Port de l'application |
| `exchange.api.url` | `https://v6.exchangerate-api.com/v6` | URL de base de l'API externe |
| `exchange.api.key` | `CHANGE_ME` (via `EXCHANGE_API_KEY`) | Clé API ExchangeRate-API (obligatoire, voir §4.2) |
| `spring.cache.type` | `caffeine` | Type de cache utilisé |
| `spring.cache.caffeine.spec` | `expireAfterWrite=5m` | TTL du cache (5 minutes) |
| `webclient.connect-timeout` | `5000` | Timeout de connexion (ms) |
| `webclient.read-timeout` | `10000` | Timeout de lecture (ms) |

---

## 6. Architecture du projet

```
src/main/java/com/Conversion/de/Devises/
├── ConversionDeDevisesApplication.java    # Point d'entrée Spring Boot
├── config/
│   ├── CacheConfig.java                   # Configuration Caffeine Cache
│   └── WebClientConfig.java              # Configuration du client HTTP
├── controller/
│   └── ConversionController.java          # Endpoint REST principal
├── exception/
│   ├── InvalidCurrencyException.java      # Exception devise invalide
│   ├── ExternalApiException.java          # Exception API externe
│   └── GlobalExceptionHandler.java        # Gestionnaire centralisé des erreurs
├── model/
│   ├── ConversionRequest.java             # DTO de requête
│   ├── ConversionResponse.java            # DTO de réponse
│   ├── ErrorResponse.java                 # DTO d'erreur
│   └── ExchangeRateApiResponse.java       # Modèle de réponse API externe
└── service/
    └── ConversionService.java             # Logique métier + cache
```

---

## 7. Endpoints API

### `POST /api/v1/convert`

Convertit un montant d'une devise à une autre.

#### Headers

```
Content-Type: application/json
```

#### Corps de la requête (Request Body)

```json
{
  "sourceCurrency": "USD",
  "targetCurrency": "EUR",
  "amount": 100.00
}
```

| Champ | Type | Obligatoire | Description |
|-------|------|-------------|-------------|
| `sourceCurrency` | String | ✅ | Code ISO 4217 de la devise source (3 lettres) |
| `targetCurrency` | String | ✅ | Code ISO 4217 de la devise cible (3 lettres) |
| `amount` | Double | ✅ | Montant à convertir (> 0) |

#### Réponse en cas de succès (200 OK)

```json
{
  "sourceCurrency": "USD",
  "targetCurrency": "EUR",
  "amount": 100.00,
  "convertedAmount": 92.35,
  "rate": 0.9235,
  "timestamp": "2026-09-02T10:30:00"
}
```

| Champ | Type | Description |
|-------|------|-------------|
| `sourceCurrency` | String | Devise source utilisée |
| `targetCurrency` | String | Devise cible utilisée |
| `amount` | Double | Montant initial |
| `convertedAmount` | Double | Montant converti (arrondi à 2 décimales) |
| `rate` | Double | Taux de change appliqué |
| `timestamp` | LocalDateTime | Horodatage de la conversion |

---

## 8. Codes de réponse HTTP

| Code | Statut | Signification | Quand ? |
|------|--------|---------------|---------|
| **200** | OK |  Conversion réussie | La conversion a été effectuée correctement |
| **400** | Bad Request |  Requête invalide | Champs manquants, montant ≤ 0, devise inexistante ou format incorrect |
| **502** | Bad Gateway |  Erreur API externe | L'API ExchangeRate-API est indisponible ou a retourné une erreur |
| **500** | Internal Server Error |  Erreur interne | Erreur inattendue dans l'application (bug, exception non gérée) |

### Détail des erreurs 400 Bad Request

| Message d'erreur | Cause |
|------------------|-------|
| `sourceCurrency: La devise source ne peut pas être vide` | Le champ `sourceCurrency` est absent ou vide |
| `targetCurrency: La devise cible ne peut pas être vide` | Le champ `targetCurrency` est absent ou vide |
| `amount: Le montant doit être supérieur à zéro` | Le champ `amount` est nul ou négatif |
| `Le code de devise source doit contenir exactement 3 caractères` | Le code devise n'a pas 3 lettres |
| `La devise source 'XYZ' n'est pas supportée` | Le code devise n'est pas dans la liste ISO 4217 |

### Détail des erreurs 502 Bad Gateway

| Message d'erreur | Cause |
|------------------|-------|
| `Impossible de se connecter au service de taux de change` | Timeout ou panne de l'API externe |
| `L'API externe a retourné une réponse invalide` | La réponse de l'API ne contient pas de données valides |
| `Aucun taux de change disponible pour la devise X` | L'API n'a pas de taux pour la devise demandée |

---

## 9. Système de cache

### Principe

Les taux de change sont mis en cache pour éviter les appels répétés à l'API externe. Le cache est géré par **Caffeine** avec une durée de vie de **5 minutes**.

### Fonctionnement

1. **Premier appel** pour `USD` → Appel à l'API externe → Cache des taux → Réponse
2. **Appels suivants** (dans les 5 min) pour `USD` → Lecture depuis le cache → Réponse instantanée
3. **Après 5 minutes** → Nouvel appel à l'API externe

### Configuration

```properties
spring.cache.caffeine.spec=expireAfterWrite=5m
spring.cache.type=caffeine
```

### Avantages

- **Performance** : Réduction drastique du temps de réponse pour les appels répétés
- **Économie** : Moins d'appels API (limite gratuite : 1 500 requêtes/mois)
- **Fiabilité** : Le cache sert de secours temporaire en cas de panne de l'API externe

---

## 10. Gestion des erreurs

L'API utilise un `GlobalExceptionHandler` centralisé qui intercepte toutes les exceptions et retourne des réponses HTTP structurées.

### Types d'exceptions gérées

| Exception | Code HTTP | Description |
|-----------|-----------|-------------|
| `MethodArgumentNotValidException` | 400 | Erreurs de validation Bean (champs manquants/invalides) |
| `InvalidCurrencyException` | 400 | Devise non supportée ou format invalide |
| `ExternalApiException` | 502 | Erreur de connexion ou de réponse de l'API externe |
| `Exception` (toutes autres) | 500 | Erreurs inattendues non spécifiquement gérées |

### Format de réponse d'erreur

Toutes les erreurs retournent un corps JSON structuré :

```json
{
  "timestamp": "2026-09-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "description détaillée de l'erreur"
}
```

---

## 11. Exemples de requêtes

### Exemple 1 : Conversion USD → EUR

**Requête :**
```bash
curl -X POST http://localhost:8085/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCurrency": "USD",
    "targetCurrency": "EUR",
    "amount": 100.00
  }'
```

**Réponse (200 OK) :**
```json
{
  "sourceCurrency": "USD",
  "targetCurrency": "EUR",
  "amount": 100.00,
  "convertedAmount": 92.35,
  "rate": 0.9235,
  "timestamp": "2026-09-02T10:30:00"
}
```

### Exemple 2 : Conversion EUR → GBP

**Requête :**
```bash
curl -X POST http://localhost:8085/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCurrency": "EUR",
    "targetCurrency": "GBP",
    "amount": 50.00
  }'
```

### Exemple 3 : Erreur — Devise invalide

**Requête :**
```bash
curl -X POST http://localhost:8085/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCurrency": "XYZ",
    "targetCurrency": "EUR",
    "amount": 100.00
  }'
```

**Réponse (400 Bad Request) :**
```json
{
  "timestamp": "2026-09-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La devise source 'XYZ' n'est pas supportée. Codes supportés: USD, EUR, GBP, JPY, etc."
}
```

### Exemple 4 : Erreur — Montant invalide

**Requête :**
```bash
curl -X POST http://localhost:8085/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCurrency": "USD",
    "targetCurrency": "EUR",
    "amount": -50.00
  }'
```

**Réponse (400 Bad Request) :**
```json
{
  "timestamp": "2026-09-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "amount: Le montant doit être supérieur à zéro"
}
```

---

## 12. Tester l'API via Swagger

### Accéder à Swagger UI

1. Démarrer l'application
2. Ouvrir votre navigateur et aller sur : **http://localhost:8085/swagger-ui.html**
3. Cliquer sur l'endpoint `POST /api/v1/convert`
4. Cliquer sur **"Try it out"**
5. Modifier le corps de la requête et cliquer sur **"Execute"**

### Fonctionnalités Swagger

- **Documentation interactive** : Visualisez tous les endpoints et leurs paramètres
- **Test direct** : Exécutez des requêtes directement depuis l'interface
- **Exemples de réponses** : Consultez les exemples de codes HTTP et messages d'erreur
- **Schéma des modèles** : Visualisez la structure des objets Request/Response

---

## 13. Tester l'API via cURL

### Installation de cURL

- **Linux/macOS** : Préinstallé
- **Windows** : Préinstallé depuis Windows 10 (ou via Git Bash)

### Requêtes de test

```bash
# Conversion USD → EUR
curl -X POST http://localhost:8085/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{"sourceCurrency": "USD", "targetCurrency": "EUR", "amount": 100.00}'

# Conversion GBP → JPY
curl -X POST http://localhost:8085/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{"sourceCurrency": "GBP", "targetCurrency": "JPY", "amount": 250.00}'

# Conversion EUR → CHF
curl -X POST http://localhost:8085/api/v1/convert \
  -H "Content-Type: application/json" \
  -d '{"sourceCurrency": "EUR", "targetCurrency": "CHF", "amount": 75.50}'
```

---

## 14. Limitations et améliorations futures

### Limitations actuelles

- **Pas d'authentification** : L'API est ouverte (pas de clé API ou JWT requis)
- **Devises limitées** : Seules les devises majeures ISO 4217 sont supportées
- **Cache de 5 minutes** : Les taux peuvent être légèrement obsolètes
- **API externe** : Dépendance à ExchangeRate-API (limite gratuite de 1 500 requêtes/mois)

### Améliorations possibles

- Ajouter une authentification JWT ou API Key
- Supporter plus de devises (API premium)
- Ajouter un historique des conversions
- Ajouter des métriques et du monitoring (Prometheus/Grafana)
- Implémenter un circuit breaker pour la résilience
- Ajouter des tests unitaires et d'intégration

---

## 📞 Support

En cas de problème, vérifiez :
1. Que la clé API est correctement configurée dans `application.properties`
2. Que l'application tourne sur le port 8085
3. Que l'API ExchangeRate-API est accessible
4. Les logs de l'application pour plus de détails

---

*Dernière mise à jour : Septembre 2026*
