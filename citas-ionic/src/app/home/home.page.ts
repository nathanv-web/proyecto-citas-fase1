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

  esPaciente = false;
  esDoctor = false;
  esAdministrador = false;

  constructor(
    private authService: Auth,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuario = this.authService.getUsuario();

    this.esPaciente =
      this.usuario?.roles?.includes('ROLE_PATIENT') ?? false;

    this.esDoctor =
      this.usuario?.roles?.includes('ROLE_DOCTOR') ?? false;

    this.esAdministrador =
      this.usuario?.roles?.includes('ROLE_ADMIN') ?? false;
  }

  get tituloPortal(): string {
    if (this.esAdministrador) {
      return 'Portal Administrativo';
    }

    if (this.esDoctor) {
      return 'Portal Médico';
    }

    return 'Portal del Paciente';
  }

  get mensajeBienvenida(): string {
    if (this.esAdministrador) {
      return 'Administrá y supervisá el sistema CitaNova.';
    }

    if (this.esDoctor) {
      return 'Consultá la información correspondiente a tu perfil médico.';
    }

    return 'Gestioná tu atención médica de forma rápida y sencilla.';
  }

  get nombreRol(): string {
    if (this.esAdministrador) {
      return 'Administrador';
    }

    if (this.esDoctor) {
      return 'Médico';
    }

    return 'Paciente';
  }

  cerrarSesion(): void {
    this.authService.logout();

    this.router.navigateByUrl('/login', {
      replaceUrl: true
    });
  }
}