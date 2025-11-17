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
