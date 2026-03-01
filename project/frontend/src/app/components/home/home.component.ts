import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import {
    TranslateService,
    TranslatePipe,
    TranslateDirective
} from "@ngx-translate/core";


@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    TranslatePipe
],
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
