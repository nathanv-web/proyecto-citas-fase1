import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Auth } from '../core/auth/services/auth';
import { LoginUsuario } from '../core/auth/models/login-response';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
  standalone: false,
})
export class HomePage implements OnInit {

  usuario: LoginUsuario | null = null;

  constructor(
    private authService: Auth,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuario = this.authService.getUsuario();
  }

  cerrarSesion(): void {
    this.authService.logout();

    this.router.navigateByUrl('/login', {
      replaceUrl: true
    });
  }
}
