import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { UserService } from '../../services/user.service';
import { UserRequest } from '../../models/user-request.model';
import { UserUpdate } from '../../models/user-update.model';


@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './user-form.html',
  styleUrl: './user-form.css'
})
export class UserForm implements OnInit {

  private fb = inject(FormBuilder);
  private usuarioService = inject(UserService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  editando = false;
  idUsuario: number | null = null;

  cargando = false;
  guardando = false;
  error = '';

  // ===============================================
  // FORMULARIO REACTIVO
  // ===============================================

 // Solo letras, espacios y letras con acentos.
private readonly patronNombre =
  /^[\p{L}]+(?:[ '\u2019-][\p{L}]+)*$/u;

// Solo números del 0 al 9.
private readonly patronTelefono = /^[0-9]+$/;

formulario = this.fb.nonNullable.group({

  nombre: ['', [
    Validators.required,
    Validators.minLength(3),
    Validators.maxLength(50),
    Validators.pattern(this.patronNombre)
  ]],

  apellido: ['', [
    Validators.required,
    Validators.minLength(3),
    Validators.maxLength(50),
    Validators.pattern(this.patronNombre)
  ]],

  correo: ['', [
    Validators.required,
    Validators.email
  ]],

  telefono: ['', [
    Validators.required,
    Validators.minLength(8),
    Validators.maxLength(20),
    Validators.pattern(this.patronTelefono)
  ]],

  contrasena: ['', [
    Validators.required,
    Validators.minLength(8),
    Validators.maxLength(12)
  ]]

});

  // ===============================================
  // INICIALIZAR
  // ===============================================

  ngOnInit(): void {

    const id = this.route.snapshot.paramMap.get('id');

    if (id === null) {
      return;
    }

    const idNumerico = Number(id);

    if (!Number.isSafeInteger(idNumerico) || idNumerico <= 0) {
      this.error = 'El ID del usuario no es válido.';
      return;
    }

    this.editando = true;
    this.idUsuario = idNumerico;

    // En edición no modificamos la contraseña.
    this.formulario.controls.contrasena.clearValidators();
    this.formulario.controls.contrasena.updateValueAndValidity();

    this.cargarUsuario(idNumerico);
  }

  // ===============================================
  // CARGAR USUARIO PARA EDITAR
  // ===============================================

  cargarUsuario(id: number): void {

  this.cargando = true;
  this.error = '';

  this.usuarioService.buscarUsuario(id).subscribe({

    next: (usuario) => {

      console.log('User cargado:', usuario.idUsuario);

      this.formulario.patchValue({
        nombre: usuario.nombre,
        apellido: usuario.apellido,
        correo: usuario.correo,
        telefono: usuario.telefono
      });

      this.cargando = false;

      // Actualizar la vista después de recibir HTTP.
      this.cdr.detectChanges();
    },

    error: (err: HttpErrorResponse) => {

      this.error = this.obtenerError(
        err,
        'No se pudo cargar el usuario.'
      );

      this.cargando = false;

      this.cdr.detectChanges();
    }

  });
}
  // ===============================================
  // GUARDAR
  // ===============================================

  guardar(): void {

    if (this.guardando || this.cargando || this.error) {
      return;
    }

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const datos = this.formulario.getRawValue();

    const base = {
      nombre: datos.nombre.trim(),
      apellido: datos.apellido.trim(),
      correo: datos.correo.trim(),
      telefono: datos.telefono.trim()
    };

    if (
      !base.nombre ||
      !base.apellido ||
      !base.correo ||
      !base.telefono
    ) {
      this.formulario.markAllAsTouched();
      this.error = 'Los campos no pueden contener solo espacios.';
      return;
    }

    this.guardando = true;

    if (this.editando && this.idUsuario !== null) {

      const request: UserUpdate = base;

      this.usuarioService
        .actualizarUsuario(this.idUsuario, request)
        .subscribe({
          next: () => this.finalizarGuardado(),
          error: err => this.manejarError(err)
        });

    } else if (!this.editando) {

      const request: UserRequest = {
        ...base,
        contrasena: datos.contrasena
      };

      this.usuarioService.crearUsuario(request).subscribe({
        next: () => this.finalizarGuardado(),
        error: err => this.manejarError(err)
      });
    }
  }

  // ===============================================
  // RESPUESTAS HTTP
  // ===============================================

  finalizarGuardado(): void {
    this.guardando = false;
    this.router.navigate(['/admin/users']);
  }

  manejarError(err: HttpErrorResponse): void {
  this.guardando = false;
  this.error = this.obtenerError(
    err,
    'No se pudo guardar el usuario.'
  );
  this.cdr.detectChanges();
}

  obtenerError(
    err: HttpErrorResponse,
    mensaje: string
  ): string {

    return typeof err.error?.message === 'string'
      ? err.error.message
      : mensaje;
  }

}