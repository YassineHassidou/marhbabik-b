# Marhababik360 Backend

Backend Java 21 pur, Maven multi-modules, microservices HTTP sans Spring.

## Services

| Service | Port | Rôle |
| --- | ---: | --- |
| api-gateway | 8080 | Routage, CORS, vérification JWT |
| auth-service | 8081 | Register, login, JWT, BCrypt |
| user-service | 8082 | Profil utilisateur |
| category-service | 8083 | Catégories fixes |
| service-catalog-service | 8084 | CRUD services |
| chat-service | 8085 | Messages |

## Lancer avec Docker

```bash
docker compose up --build
```

L’API publique est exposée sur `http://localhost:8080`. Les services internes sont aussi exposés sur `8081` à `8085` pour debug local.

Définir une vraie clé JWT en local :

```bash
set JWT_SECRET=une-cle-longue-et-secrete
docker compose up --build
```

PowerShell :

```powershell
$env:JWT_SECRET="une-cle-longue-et-secrete"
docker compose up --build
```

## Endpoints principaux

`POST /auth/register`

```json
{
  "fullName": "Worker One",
  "email": "worker@example.com",
  "password": "password123",
  "role": "worker",
  "category": "transport",
  "businessName": "Worker Travel"
}
```

`POST /auth/login`

```json
{
  "email": "worker@example.com",
  "password": "password123"
}
```

Utiliser ensuite :

```http
Authorization: Bearer <accessToken>
```

Routes protégées :

```text
GET /users/me
PUT /users/me
GET /users/{id}
GET /categories
GET /services?category=transport
POST /services
GET /services/{id}
PUT /services/{id}
DELETE /services/{id}
GET /messages?receiverId=<uuid>
POST /messages
```

## Règles implémentées

- Passwords hashés avec BCrypt.
- `passwordHash` jamais exposé dans les DTO de réponse.
- JWT généré par `auth-service` et vérifié par `api-gateway`.
- Rôles vérifiés dans les services métier.
- CORS activé.
- La clé JSON est `category`.
- Erreurs JSON standardisées : `timestamp`, `status`, `error`, `message`, `path`.

## Scripts SQL

Chaque service contient son script dans `*/sql/schema.sql`.

## Build Maven local

```bash
mvn clean package
```

Java 21 est requis.
