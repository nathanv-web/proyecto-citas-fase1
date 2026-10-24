import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// Estructura de datos para definir los Roles del sistema
interface Rol {
  id: number;
  nombre: string;
  descripcion: string;
  permisosCount: number;
  estado: string;
}

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './roles.component.html',
  styleUrl: './roles.component.css'
})
export class RolesComponent {
  // Búsqueda
  searchTerm: string = '';

  // Control de ventana modal
  mostrarModal: boolean = false;
  modoEdicion: boolean = false;

  // Formulario enlazado
  rolForm: Rol = {
    id: 0,
    nombre: '',
    descripcion: '',
    permisosCount: 5,
    estado: 'ACTIVO'
  };

  // Lista local de roles
  roles: Rol[] = [
    { id: 1, nombre: 'ADMINISTRADOR', descripcion: 'Acceso total al sistema CitaNova', permisosCount: 15, estado: 'ACTIVO' },
    { id: 2, nombre: 'MEDICO', descripcion: 'Gestión de agenda, horarios y citas médicas', permisosCount: 8, estado: 'ACTIVO' },
    { id: 3, nombre: 'RECEPCIONISTA', descripcion: 'Gestión de citas y atención a pacientes', permisosCount: 5, estado: 'ACTIVO' }
  ];

  // Getter reactivo para filtrar roles según búsqueda de texto
  get rolesFiltrados(): Rol[] {
    return this.roles.filter(r =>
      r.nombre.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      r.descripcion.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

  // Prepara y abre la modal en modo "Crear"
  abrirModalCrear(): void {
    this.modoEdicion = false;
    this.rolForm = { id: Date.now(), nombre: '', descripcion: '', permisosCount: 5, estado: 'ACTIVO' };
    this.mostrarModal = true;
  }

  // Carga el rol seleccionado y abre la modal en modo "Edición"
  seleccionarEditar(r: Rol): void {
    this.modoEdicion = true;
    this.rolForm = { ...r };
    this.mostrarModal = true;
  }

  // Cierra la modal
  cerrarModal(): void {
    this.mostrarModal = false;
  }

  // Guarda un nuevo rol o actualiza uno existente
  guardarRol(): void {
    if (!this.rolForm.nombre || !this.rolForm.descripcion) return;

    if (this.modoEdicion) {
      const index = this.roles.findIndex(r => r.id === this.rolForm.id);
      if (index !== -1) {
        this.roles[index] = { ...this.rolForm };
      }
    } else {
      this.roles.push({ ...this.rolForm });
    }
    this.cerrarModal();
  }

  // Elimina un rol de la lista
  eliminarRol(id: number): void {
    this.roles = this.roles.filter(r => r.id !== id);
  }
}