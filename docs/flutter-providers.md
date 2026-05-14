# Adapter les providers Flutter

## Base URL

Toutes les requêtes Flutter doivent passer par la gateway :

```dart
const apiBaseUrl = 'http://10.0.2.2:8080'; // Android emulator
// const apiBaseUrl = 'http://localhost:8080'; // desktop/web local
```

## AuthProvider

Après `POST /auth/login` ou `POST /auth/register`, stocker `accessToken` et `user`.

```dart
final response = await http.post(
  Uri.parse('$apiBaseUrl/auth/login'),
  headers: {'Content-Type': 'application/json'},
  body: jsonEncode({'email': email, 'password': password}),
);

final data = jsonDecode(response.body);
token = data['accessToken'];
currentUser = data['user'];
```

## Client HTTP commun

Ajouter le token sur toutes les routes protégées :

```dart
Map<String, String> authHeaders(String token) => {
  'Content-Type': 'application/json',
  'Authorization': 'Bearer $token',
};
```

## UserProvider

```dart
final response = await http.get(
  Uri.parse('$apiBaseUrl/users/me'),
  headers: authHeaders(token),
);
```

Mise à jour :

```dart
await http.put(
  Uri.parse('$apiBaseUrl/users/me'),
  headers: authHeaders(token),
  body: jsonEncode({
    'fullName': fullName,
    'age': age,
    'cn': cn,
    'phone': phone,
    'category': category,
    'businessName': businessName,
    'profilePhoto': profilePhoto,
  }),
);
```

## CategoryProvider

La gateway protège aussi `/categories`, donc envoyer le JWT :

```dart
final response = await http.get(
  Uri.parse('$apiBaseUrl/categories'),
  headers: authHeaders(token),
);
```

## ServiceCatalogProvider

Lecture :

```dart
final response = await http.get(
  Uri.parse('$apiBaseUrl/services?category=$category'),
  headers: authHeaders(token),
);
```

Création worker :

```dart
await http.post(
  Uri.parse('$apiBaseUrl/services'),
  headers: authHeaders(token),
  body: jsonEncode({
    'title': title,
    'category': category,
    'description': description,
    'price': price,
    'images': images,
    'location': location,
  }),
);
```

## ChatProvider

```dart
await http.post(
  Uri.parse('$apiBaseUrl/messages'),
  headers: authHeaders(token),
  body: jsonEncode({'receiverId': receiverId, 'text': text}),
);

final response = await http.get(
  Uri.parse('$apiBaseUrl/messages?receiverId=$receiverId'),
  headers: authHeaders(token),
);
```

## Gestion des erreurs

Tous les services retournent :

```json
{
  "timestamp": "2026-05-14T20:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "category is required",
  "path": "/services"
}
```

Dans Flutter, lire `message` pour afficher l’erreur utilisateur.
