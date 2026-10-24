import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// Definición de la estructura de datos para un Usuario
interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  rol: string;
  estado: string;
}

@Component({
  selector: 'app-usuarios',
  standalone: true, // Componente Standalone (Angular 17+)
  imports: [CommonModule, FormsModule], // Módulos necesarios para directivas y formulación (ngModel)
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent {
  // Variables para el control de búsqueda y filtros
  searchTerm: string = '';
  estadoFilter: string = '';

  // Control del estado visual de la ventana modal
  mostrarModal: boolean = false;
  modoEdicion: boolean = false;

  // Modelo de datos enlazado al formulario de la modal
  usuarioForm: Usuario = {
    id: 0,
    nombre: '',
    correo: '',
    rol: 'PACIENTE',
    estado: 'ACTIVO'
  };

  // Base de datos local (Mock data para pruebas en el Frontend)
  usuarios: Usuario[] = [
    { id: 101, nombre: 'Nathan Administrador', correo: 'nathan@citanova.com', rol: 'ADMINISTRADOR', estado: 'ACTIVO' },
    { id: 102, nombre: 'Maria Lopez', correo: 'maria@citanova.com', rol: 'MEDICO', estado: 'ACTIVO' },
    { id: 103, nombre: 'Juan Perez', correo: 'juan@citanova.com', rol: 'PACIENTE', estado: 'INACTIVO' }
  ];

  // Getter reactivo: Filtra usuarios en tiempo real por búsqueda de texto y estado seleccionado
  get usuariosFiltrados(): Usuario[] {
    return this.usuarios.filter(u => {
      const matchSearch = u.nombre.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                          u.correo.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchEstado = this.estadoFilter === '' || u.estado === this.estadoFilter;
      return matchSearch && matchEstado;
    });
  }

  // Limpia los campos de filtro y restablece la tabla
  limpiarFiltros(): void {
    this.searchTerm = '';
    this.estadoFilter = '';
  }

  // Prepara y abre la modal en modo "Creación"
  abrirModalCrear(): void {
    this.modoEdicion = false;
    this.usuarioForm = { id: Date.now(), nombre: '', correo: '', rol: 'PACIENTE', estado: 'ACTIVO' };
    this.mostrarModal = true;
  }

  // Carga los datos del usuario seleccionado y abre la modal en modo "Edición"
  seleccionarEditar(u: Usuario): void {
    this.modoEdicion = true;
    this.usuarioForm = { ...u }; // Se usa spread operator (...) para evitar mutación directa
    this.mostrarModal = true;
  }

  // Cierra la ventana modal
  cerrarModal(): void {
    this.mostrarModal = false;
  }

  // Guarda un nuevo usuario o actualiza uno existente según el modo activo
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

  // Alterna dinámicamente el estado del usuario entre ACTIVO e INACTIVO
  toggleEstado(u: Usuario): void {
    u.estado = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
  }
}