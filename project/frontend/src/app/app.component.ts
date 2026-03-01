import { Component, inject, OnInit } from '@angular/core';

import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './services/auth.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {
  private translate = inject(TranslateService);

  title = 'Wateralmanak';
  isLoggedIn = false;
  username = '';
  isAdmin = false;

  constructor(private authService: AuthService) {
    this.translate.addLangs(['en', 'nl']);
    this.translate.setFallbackLang('nl');
    this.translate.use('nl');
  }

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
