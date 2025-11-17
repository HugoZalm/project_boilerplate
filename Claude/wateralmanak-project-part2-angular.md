## Part 2: Frontend (Angular 20)

### `/frontend/Dockerfile`

```dockerfile
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/wateralmanak-frontend/browser /usr/share/nginx/html
EXPOSE 80
```

---

### `/frontend/.dockerignore`

```
node_modules
dist
.angular
.git
.gitignore
*.md
```

---

### `/frontend/package.json`

```json
{
  "name": "wateralmanak-frontend",
  "version": "1.0.0",
  "scripts": {
    "ng": "ng",
    "start": "ng serve",
    "build": "ng build",
    "watch": "ng build --watch --configuration development",
    "test": "ng test"
  },
  "private": true,
  "dependencies": {
    "@angular/animations": "^20.0.0",
    "@angular/common": "^20.0.0",
    "@angular/compiler": "^20.0.0",
    "@angular/core": "^20.0.0",
    "@angular/forms": "^20.0.0",
    "@angular/platform-browser": "^20.0.0",
    "@angular/platform-browser-dynamic": "^20.0.0",
    "@angular/router": "^20.0.0",
    "keycloak-angular": "^16.0.1",
    "keycloak-js": "^23.0.0",
    "rxjs": "~7.8.0",
    "tslib": "^2.3.0",
    "zone.js": "~0.15.0"
  },
  "devDependencies": {
    "@angular-devkit/build-angular": "^20.0.0",
    "@angular/cli": "^20.0.0",
    "@angular/compiler-cli": "^20.0.0",
    "@types/node": "^20.0.0",
    "typescript": "~5.6.2"
  }
}
```

---

### `/frontend/angular.json`

```json
{
  "$schema": "./node_modules/@angular/cli/lib/config/schema.json",
  "version": 1,
  "newProjectRoot": "projects",
  "projects": {
    "wateralmanak-frontend": {
      "projectType": "application",
      "schematics": {},
      "root": "",
      "sourceRoot": "src",
      "prefix": "app",
      "architect": {
        "build": {
          "builder": "@angular-devkit/build-angular:application",
          "options": {
            "outputPath": "dist/wateralmanak-frontend",
            "index": "src/index.html",
            "browser": "src/main.ts",
            "polyfills": [
              "zone.js"
            ],
            "tsConfig": "tsconfig.app.json",
            "assets": [
              {
                "glob": "**/*",
                "input": "public"
              }
            ],
            "styles": [
              "src/styles.css"
            ],
            "scripts": []
          },
          "configurations": {
            "production": {
              "budgets": [
                {
                  "type": "initial",
                  "maximumWarning": "500kB",
                  "maximumError": "1MB"
                },
                {
                  "type": "anyComponentStyle",
                  "maximumWarning": "2kB",
                  "maximumError": "4kB"
                }
              ],
              "outputHashing": "all"
            },
            "development": {
              "optimization": false,
              "extractLicenses": false,
              "sourceMap": true
            }
          },
          "defaultConfiguration": "production"
        },
        "serve": {
          "builder": "@angular-devkit/build-angular:dev-server",
          "configurations": {
            "production": {
              "buildTarget": "wateralmanak-frontend:build:production"
            },
            "development": {
              "buildTarget": "wateralmanak-frontend:build:development"
            }
          },
          "defaultConfiguration": "development"
        }
      }
    }
  }
}
```

---

### `/frontend/tsconfig.json`

```json
{
  "compileOnSave": false,
  "compilerOptions": {
    "outDir": "./dist/out-tsc",
    "strict": true,
    "noImplicitOverride": true,
    "noPropertyAccessFromIndexSignature": true,
    "noImplicitReturns": true,
    "noFallthroughCasesInSwitch": true,
    "skipLibCheck": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "sourceMap": true,
    "declaration": false,
    "experimentalDecorators": true,
    "moduleResolution": "bundler",
    "importHelpers": true,
    "target": "ES2022",
    "module": "ES2022",
    "useDefineForClassFields": false,
    "lib": [
      "ES2022",
      "dom"
    ]
  },
  "angularCompilerOptions": {
    "enableI18nLegacyMessageIdFormat": false,
    "strictInjectionParameters": true,
    "strictInputAccessModifiers": true,
    "strictTemplates": true
  }
}
```

---

### `/frontend/tsconfig.app.json`

```json
{
  "extends": "./tsconfig.json",
  "compilerOptions": {
    "outDir": "./out-tsc/app",
    "types": []
  },
  "files": [
    "src/main.ts"
  ],
  "include": [
    "src/**/*.d.ts"
  ]
}
```

---

### `/frontend/src/main.ts`

```typescript
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
```

---

### `/frontend/src/index.html`

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Wateralmanak</title>
  <base href="/">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="favicon.ico">
</head>
<body>
  <app-root></app-root>
</body>
</html>
```

---

### `/frontend/src/styles.css`

```css
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
  color: #333;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

button {
  cursor: pointer;
  border: none;
  border-radius: 4px;
  padding: 10px 20px;
  font-size: 14px;
  transition: all 0.3s ease;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background-color: #c82333;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #5a6268;
}

input, textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  margin-bottom: 10px;
}

input:focus, textarea:focus {
  outline: none;
  border-color: #007bff;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border-radius: 4px;
  overflow: hidden;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background-color: #007bff;
  color: white;
  font-weight: 600;
}

tr:hover {
  background-color: #f8f9fa;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.alert {
  padding: 12px 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.alert-error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.alert-success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}
```

---

### `/frontend/src/app/app.config.ts`

```typescript
import { ApplicationConfig, APP_INITIALIZER, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth.interceptor';
import { environment } from '../environments/environment';

function initializeKeycloak(keycloak: KeycloakService) {
  return () =>
    keycloak.init({
      config: {
        url: environment.keycloakUrl,
        realm: environment.keycloakRealm,
        clientId: environment.keycloakClientId,
      },
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri:
          window.location.origin + '/assets/silent-check-sso.html',
        checkLoginIframe: false,
      },
      enableBearerInterceptor: false,
    });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    KeycloakService,
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService],
    },
  ],
};
```

---

### `/frontend/src/app/app.routes.ts`

```typescript
import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { HomeComponent } from './components/home/home.component';
import { VoorzieningenComponent } from './components/voorzieningen/voorzieningen.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  {
    path: 'voorzieningen',
    component: VoorzieningenComponent,
    canActivate: [AuthGuard],
  },
  { path: '**', redirectTo: '' },
];
```

---

### `/frontend/src/app/app.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {
  title = 'Wateralmanak';
  isLoggedIn = false;
  username = '';
  isAdmin = false;

  constructor(private authService: AuthService) {}

  async ngOnInit() {
    this.isLoggedIn = await this.authService.isLoggedIn();
    if (this.isLoggedIn) {
      this.username = await this.authService.getUsername();
      this.isAdmin = await this.authService.hasRole('admin');
    }
  }

  async login() {
    await this.authService.login();
  }

  async logout() {
    await this.authService.logout();
  }
}
```

---

### `/frontend/src/app/app.component.html`

```html
<nav class="navbar">
  <div class="nav-container">
    <h1 class="nav-title">{{ title }}</h1>
    <ul class="nav-links">
      <li>
        <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
          Home
        </a>
      </li>
      @if (isLoggedIn) {
        <li>
          <a routerLink="/voorzieningen" routerLinkActive="active">
            Voorzieningen
          </a>
        </li>
      }
    </ul>
    <div class="nav-auth">
      @if (isLoggedIn) {
        <span class="username">
          {{ username }}
          @if (isAdmin) {
            <span class="badge">Admin</span>
          }
        </span>
        <button class="btn-secondary" (click)="logout()">Logout</button>
      } @else {
        <button class="btn-primary" (click)="login()">Login</button>
      }
    </div>
  </div>
</nav>

<main>
  <router-outlet></router-outlet>
</main>
```

---

### `/frontend/src/app/app.component.css`

```css
.navbar {
  background-color: #007bff;
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
}

.nav-links {
  display: flex;
  list-style: none;
  gap: 2rem;
  margin: 0;
  padding: 0;
}

.nav-links a {
  color: white;
  text-decoration: none;
  font-weight: 500;
  transition: opacity 0.3s;
}

.nav-links a:hover {
  opacity: 0.8;
}

.nav-links a.active {
  border-bottom: 2px solid white;
  padding-bottom: 4px;
}

.nav-auth {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.username {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.badge {
  background-color: #ffc107;
  color: #333;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

main {
  min-height: calc(100vh - 80px);
}
```

---

### `/frontend/src/app/services/auth.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor(private keycloakService: KeycloakService) {}

  async isLoggedIn(): Promise<boolean> {
    return await this.keycloakService.isLoggedIn();
  }

  async login(): Promise<void> {
    await this.keycloakService.login();
  }

  async logout(): Promise<void> {
    await this.keycloakService.logout(window.location.origin);
  }

  async getUsername(): Promise<string> {
    const profile = await this.keycloakService.loadUserProfile();
    return profile.username || '';
  }

  async getToken(): Promise<string> {
    return await this.keycloakService.getToken();
  }

  async hasRole(role: string): Promise<boolean> {
    return this.keycloakService.isUserInRole(role);
  }
}
```

---

### `/frontend/src/app/services/voorziening.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Voorziening } from '../models/voorziening.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class VoorzieningService {
  private apiUrl = `${environment.apiUrl}/voorzieningen`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Voorziening[]> {
    return this.http.get<Voorziening[]>(this.apiUrl);
  }

  getById(id: string): Observable<Voorziening> {
    return this.http.get<Voorziening>(`${this.apiUrl}/${id}`);
  }

  create(voorziening: Voorziening): Observable<Voorziening> {
    return this.http.post<Voorziening>(this.apiUrl, voorziening);
  }

  update(id: string, voorziening: Voorziening): Observable<Voorziening> {
    return this.http.put<Voorziening>(`${this.apiUrl}/${id}`, voorziening);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

---

### `/frontend/src/app/interceptors/auth.interceptor.ts`

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { from, switchMap } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return from(authService.getToken()).pipe(
    switchMap((token) => {
      if (token) {
        const clonedRequest = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        });
        return next(clonedRequest);
      }
      return next(req);
    })
  );
};
```

---

### `/frontend/src/app/guards/auth.guard.ts`

```typescript
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const AuthGuard: CanActivateFn = async (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isLoggedIn = await authService.isLoggedIn();

  if (!isLoggedIn) {
    await authService.login();
    return false;
  }

  return true;
};
```

---

### `/frontend/src/app/models/voorziening.model.ts`

```typescript
export interface Voorziening {
  id?: string;
  naam: string;
  beschrijving: string;
  longitude: number;
  latitude: number;
  createdAt?: string;
  updatedAt?: string;
}
```

---

### `/frontend/src/app/components/home/home.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  isLoggedIn = false;

  constructor(private authService: AuthService) {}

  async ngOnInit() {
    this.isLoggedIn = await this.authService.isLoggedIn();
  }

  async login() {
    await this.authService.login();
  }
}
```

---

### `/frontend/src/app/components/home/home.component.html`

```html
<div class="container">
  <div class="hero">
    <h1>Welcome to Wateralmanak</h1>
    <p class="subtitle">
      Manage water facilities and infrastructure with ease
    </p>

    @if (!isLoggedIn) {
      <div class="cta">
        <p>Please login to access the application</p>
        <button class="btn-primary btn-large" (click)="login()">
          Login with Keycloak
        </button>
      </div>
    } @else {
      <div class="features">
        <div class="feature-card">
          <h3>📍 Facility Management</h3>
          <p>View and manage water facilities with location data</p>
        </div>
        <div class="feature-card">
          <h3>🔒 Secure Access</h3>
          <p>Role-based access control with Keycloak</p>
        </div>
        <div class="feature-card">
          <h3>🗺️ PostGIS Integration</h3>
          <p>Spatial data support for geographic queries</p>
        </div>
      </div>
    }
  </div>
</div>
```

---

### `/frontend/src/app/components/home/home.component.css`

```css
.hero {
  text-align: center;
  padding: 60px 20px;
}

.hero h1 {
  font-size: 3rem;
  color: #007bff;
  margin-bottom: 20px;
}

.subtitle {
  font-size: 1.25rem;
  color: #666;
  margin-bottom: 40px;
}

.cta {
  margin: 40px 0;
}

.cta p {
  font-size: 1.1rem;
  margin-bottom: 20px;
}

.btn-large {
  padding: 15px 40px;
  font-size: 1.1rem;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
  margin-top: 60px;
}

.feature-card {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
  color: #333;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}
```

---

### `/frontend/src/app/components/voorzieningen/voorzieningen.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VoorzieningService } from '../../services/voorziening.service';
import { AuthService } from '../../services/auth.service';
import { Voorziening } from '../../models/voorziening.model';

@Component({
  selector: 'app-voorzieningen',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './voorzieningen.component.html',
  styleUrls: ['./voorzieningen.component.css'],
})
export class VoorzieningenComponent implements OnInit {
  voorzieningen: Voorziening[] = [];
  isAdmin = false;
  loading = false;
  error = '';
  success = '';

  showForm = false;
  editMode = false;
  currentVoorziening: Voorziening = this.getEmptyVoorziening();

  constructor(
    private voorzieningService: VoorzieningService,
    private authService: AuthService
  ) {}

  async ngOnInit() {
    this.isAdmin = await this.authService.hasRole('admin');
    this.loadVoorzieningen();
  }

  loadVoorzieningen() {
    this.loading = true;
    this.error = '';
    this.voorzieningService.getAll().subscribe({
      next: (data) => {
        this.voorzieningen = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load voorzieningen: ' + err.message;
        this.loading = false;
      },
    });
  }

  openCreateForm() {
    this.showForm = true;
    this.editMode = false;
    this.currentVoorziening = this.getEmptyVoorziening();
  }

  openEditForm(voorziening: Voorziening) {
    this.showForm = true;
    this.editMode = true;
    this.currentVoorziening = { ...voorziening };
  }

  closeForm() {
    this.showForm = false;
    this.currentVoorziening = this.getEmptyVoorziening();
    this.error = '';
    this.success = '';
  }

  save() {
    this.loading = true;
    this.error = '';
    this.success = '';

    if (this.editMode && this.currentVoorziening.id) {
      this.voorzieningService
        .update(this.currentVoorziening.id, this.currentVoorziening)
        .subscribe({
          next: () => {
            this.success = 'Voorziening updated successfully';
            this.loadVoorzieningen();
            this.closeForm();
            this.loading = false;
          },
          error: (err) => {
            this.error = 'Failed to update voorziening: ' + err.message;
            this.loading = false;
          },
        });
    } else {
      this.voorzieningService.create(this.currentVoorziening).subscribe({
        next: () => {
          this.success = 'Voorziening created successfully';
          this.loadVoorzieningen();
          this.closeForm();
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to create voorziening: ' + err.message;
          this.loading = false;
        },
      });
    }
  }

  delete(id: string) {
    if (!confirm('Are you sure you want to delete this voorziening?')) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    this.voorzieningService.delete(id).subscribe({
      next: () => {
        this.success = 'Voorziening deleted successfully';
        this.loadVoorzieningen();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to delete voorziening: ' + err.message;
        this.loading = false;
      },
    });
  }

  private getEmptyVoorziening(): Voorziening {
    return {
      naam: '',
      beschrijving: '',
      longitude: 0,
      latitude: 0,
    };
  }
}
```

---

### `/frontend/src/app/components/voorzieningen/voorzieningen.component.html`

```html
<div class="container">
  <div class="header">
    <h1>Voorzieningen</h1>
    @if (isAdmin) {
      <button class="btn-primary" (click)="openCreateForm()">
        + Create New
      </button>
    }
  </div>

  @if (error) {
    <div class="alert alert-error">{{ error }}</div>
  }

  @if (success) {
    <div class="alert alert-success">{{ success }}</div>
  }

  @if (loading && !showForm) {
    <div class="loading">Loading voorzieningen...</div>
  }

  @if (!loading && voorzieningen.length === 0) {
    <div class="card">
      <p>No voorzieningen found. Create your first one!</p>
    </div>
  }

  @if (!loading && voorzieningen.length > 0) {
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>Naam</th>
            <th>Beschrijving</th>
            <th>Longitude</th>
            <th>Latitude</th>
            <th>Created</th>
            @if (isAdmin) {
              <th>Actions</th>
            }
          </tr>
        </thead>
        <tbody>
          @for (voorziening of voorzieningen; track voorziening.id) {
            <tr>
              <td>{{ voorziening.naam }}</td>
              <td>{{ voorziening.beschrijving }}</td>
              <td>{{ voorziening.longitude | number:'1.4-4' }}</td>
              <td>{{ voorziening.latitude | number:'1.4-4' }}</td>
              <td>{{ voorziening.createdAt | date:'short' }}</td>
              @if (isAdmin) {
                <td class="actions">
                  <button class="btn-sm btn-primary" (click)="openEditForm(voorziening)">
                    Edit
                  </button>
                  <button class="btn-sm btn-danger" (click)="delete(voorziening.id!)">
                    Delete
                  </button>
                </td>
              }
            </tr>
          }
        </tbody>
      </table>
    </div>
  }

  @if (showForm) {
    <div class="modal-backdrop" (click)="closeForm()"></div>
    <div class="modal">
      <div class="modal-header">
        <h2>{{ editMode ? 'Edit' : 'Create' }} Voorziening</h2>
        <button class="close-btn" (click)="closeForm()">&times;</button>
      </div>
      <div class="modal-body">
        <form (ngSubmit)="save()">
          <div class="form-group">
            <label for="naam">Naam *</label>
            <input
              type="text"
              id="naam"
              [(ngModel)]="currentVoorziening.naam"
              name="naam"
              required
            />
          </div>

          <div class="form-group">
            <label for="beschrijving">Beschrijving</label>
            <textarea
              id="beschrijving"
              [(ngModel)]="currentVoorziening.beschrijving"
              name="beschrijving"
              rows="4"
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="longitude">Longitude *</label>
              <input
                type="number"
                id="longitude"
                [(ngModel)]="currentVoorziening.longitude"
                name="longitude"
                step="0.0001"
                required
              />
            </div>

            <div class="form-group">
              <label for="latitude">Latitude *</label>
              <input
                type="number"
                id="latitude"
                [(ngModel)]="currentVoorziening.latitude"
                name="latitude"
                step="0.0001"
                required
              />
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn-secondary" (click)="closeForm()">
              Cancel
            </button>
            <button type="submit" class="btn-primary" [disabled]="loading">
              {{ loading ? 'Saving...' : 'Save' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  }
</div>
```

---

### `/frontend/src/app/components/voorzieningen/voorzieningen.component.css`

```css
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h1 {
  margin: 0;
  color: #333;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.table-container {
  overflow-x: auto;
  margin-top: 20px;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  z-index: 1001;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ddd;
}

.modal-header h2 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ddd;
}
```

---

### `/frontend/src/environments/environment.ts`

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost/api',
  keycloakUrl: 'http://localhost/auth',
  keycloakRealm: 'wateralmanak',
  keycloakClientId: 'wateralmanak-frontend',
};
```

---

