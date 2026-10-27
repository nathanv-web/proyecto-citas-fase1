import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

/**
 * ============================================================================
 * INTERFAZ: Usuario
 * Define la estructura y contrato de datos para cada usuario del sistema.
 * ============================================================================
 */
interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  rol: string;
  estado: string;
}

/**
 * ============================================================================
 * COMPONENTE: UsuariosComponent
 * Módulo del Panel Administrativo (Integrante 5 - Nathan)
 * Encargado de la gestión de usuarios, filtrado reactivo y modales.
 * ============================================================================
 */
@Component({
  selector: 'app-usuarios',
  standalone: true, // Componente Standalone de Angular 17+
  imports: [CommonModule, FormsModule], // Módulos para directivas (*ngIf, *ngFor) y [(ngModel)]
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent {
  // Variables para el control de búsqueda y filtros en la tabla
  searchTerm: string = '';
  estadoFilter: string = '';

  // Variables para el control visual de la ventana modal
  mostrarModal: boolean = false;
  modoEdicion: boolean = false;

  // Modelo de datos enlazado al formulario del modal (NgModel)
  usuarioForm: Usuario = {
    id: 0,
    nombre: '',
    correo: '',
    rol: 'PACIENTE',
    estado: 'ACTIVO'
  };

  // Base de datos local (Mock data para pruebas completas en el Frontend)
  usuarios: Usuario[] = [
    { id: 101, nombre: 'Nathan Administrador', correo: 'nathan@citanova.com', rol: 'ADMINISTRADOR', estado: 'ACTIVO' },
    { id: 102, nombre: 'Maria Lopez', correo: 'maria@citanova.com', rol: 'MEDICO', estado: 'ACTIVO' },
    { id: 103, nombre: 'Juan Perez', correo: 'juan@citanova.com', rol: 'PACIENTE', estado: 'INACTIVO' }
  ];

  /**
   * Getter reactivo: Filtra los usuarios en tiempo real.
   * Evalúa coincidencias de texto (nombre o correo) y filtro de estado (ACTIVO/INACTIVO).
   */
  get usuariosFiltrados(): Usuario[] {
    return this.usuarios.filter(u => {
      const matchSearch = u.nombre.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                          u.correo.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchEstado = this.estadoFilter === '' || u.estado === this.estadoFilter;
      return matchSearch && matchEstado;
    });
  }

  /**
   * Restablece los campos de búsqueda y limpia los filtros de la tabla
   */
  limpiarFiltros(): void {
    this.searchTerm = '';
    this.estadoFilter = '';
  }

  /**
   * Prepara y despliega la ventana modal en modo "Creación"
   */
  abrirModalCrear(): void {
    this.modoEdicion = false;
    this.usuarioForm = { id: Date.now(), nombre: '', correo: '', rol: 'PACIENTE', estado: 'ACTIVO' };
    this.mostrarModal = true;
  }

  /**
   * Carga la información del usuario en el formulario y abre la modal en modo "Edición"
   * Se utiliza el operador 'spread' ({ ...u }) para evitar mutaciones directas en la lista antes de guardar.
   */
  seleccionarEditar(u: Usuario): void {
    this.modoEdicion = true;
    this.usuarioForm = { ...u };
    this.mostrarModal = true;
  }

  /**
   * Oculta la ventana modal
   */
  cerrarModal(): void {
    this.mostrarModal = false;
  }

  /**
   * Procesa el guardado del formulario.
   * Si está en modo edición actualiza el registro existente; si no, inserta un nuevo usuario.
   */
  guardarUsuario(): void {
    if (!this.usuarioForm.nombre || !this.usuarioForm.correo) return;

    if (this.modoEdicion) {
      const index = this.usuarios.findIndex(u => u.id === this.usuarioForm.id);
      if (index !== -1) {
        this.usuarios[index] = { ...this.usuarioForm };
      }
    } else {
      this.usuarios.push({ ...this.usuarioForm });
    }
    this.cerrarModal();
  }

  /**
   * Alterna dinámicamente el estado del usuario entre 'ACTIVO' e 'INACTIVO'
   */
  toggleEstado(u: Usuario): void {
    u.estado = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
  }
}