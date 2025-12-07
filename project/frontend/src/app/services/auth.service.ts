import { inject, Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly keycloak = inject(Keycloak);
  constructor() {}

  async isLoggedIn(): Promise<boolean> {
    return this.keycloak.authenticated;
  }

  async login(): Promise<void> {
    await this.keycloak.login();
  }

  async logout(): Promise<void> {
    await this.keycloak.logout({ redirectUri: window.location.origin });
  }

  async getUsername(): Promise<string> {
    const profile = await this.keycloak.loadUserProfile();
    return profile.username || '';
  }

  async hasRole(role: string, client?: string): Promise<boolean> {
    console.log('AUTHSERVICE', this.keycloak.hasRealmRole(role));
    return await this.keycloak.hasRealmRole(role);
  }

}
