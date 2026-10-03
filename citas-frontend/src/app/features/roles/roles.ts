import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RolService } from '../../core/services/rol';
import { Role, RoleRequest, Permission } from '../../core/models/role';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './roles.html',
  styleUrl: './roles.css'
})
export class RolesComponent implements OnInit {
  private rolService = inject(RolService);
  private cdr = inject(ChangeDetectorRef);

  roles: Role[] = [];
  allPermissions: Permission[] = [];
  selectedRoleDetail: Role | null = null;
  loading: boolean = true;
  errorMsg: string = '';

  // Control de modales y formularios
  showRoleModal: boolean = false;
  showPermissionModal: boolean = false;
  showDetailModal: boolean = false;
  isEditing: boolean = false;
  selectedRoleId: number | null = null;
  selectedRoleName: string = '';

  // Formulario de Rol
  roleForm: RoleRequest = {
    name: '',
    description: ''
  };

  // Formulario de Permisos
  selectedPermissionIds: number[] = [];

  ngOnInit(): void {
    this.loadRoles();
    this.loadPermissions();
  }

  // Cargar lista de roles
  loadRoles(): void {
    this.loading = true;
    this.errorMsg = '';
    
    this.rolService.getRoles().subscribe({
      next: (response: any) => {
        if (Array.isArray(response)) {
          this.roles = response;
        } else if (response && Array.isArray(response.data)) {
          this.roles = response.data;
        } else if (response && Array.isArray(response.content)) {
          this.roles = response.content;
        } else {
          this.roles = [];
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar roles:', err);
        this.errorMsg = 'No se pudieron cargar los roles. Verifica que el backend esté encendido.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Cargar catálogo de permisos disponibles
  loadPermissions(): void {
    this.rolService.getPermissions().subscribe({
      next: (response: any) => {
        if (Array.isArray(response)) {
          this.allPermissions = response;
        } else if (response && Array.isArray(response.data)) {
          this.allPermissions = response.data;
        } else if (response && Array.isArray(response.content)) {
          this.allPermissions = response.content;
        } else {
          this.allPermissions = [];
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar permisos:', err);
      }
    });
  }

  // Ver detalle de un rol
  viewRoleDetail(roleId: number): void {
    this.rolService.getRoleById(roleId).subscribe({
      next: (role) => {
        this.selectedRoleDetail = role;
        this.showDetailModal = true;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar detalle del rol:', err)
    });
  }

  // Abrir modal para crear
  openCreateModal(): void {
    this.isEditing = false;
    this.selectedRoleId = null;
    this.roleForm = { name: '', description: '' };
    this.showRoleModal = true;
  }

  // Abrir modal para editar datos básicos del rol
  openEditModal(role: Role): void {
    this.isEditing = true;
    this.selectedRoleId = role.id ?? null;
    this.roleForm = {
      name: role.name,
      description: role.description ?? ''
    };
    this.showRoleModal = true;
  }

  // Guardar (Crear o Editar)
  saveRole(): void {
    if (!this.roleForm.name.trim()) return;

    if (this.isEditing && this.selectedRoleId) {
      this.rolService.updateRole(this.selectedRoleId, this.roleForm).subscribe({
        next: () => {
          this.closeRoleModal();
          this.loadRoles();
        },
        error: (err) => console.error('Error al actualizar rol:', err)
      });
    } else {
      this.rolService.createRole(this.roleForm).subscribe({
        next: () => {
          this.closeRoleModal();
          this.loadRoles();
        },
        error: (err) => console.error('Error al crear rol:', err)
      });
    }
  }

  // Eliminar rol
  deleteRole(id: number | undefined): void {
    if (!id) return;
    if (confirm('¿Estás seguro de eliminar este rol?')) {
      this.rolService.deleteRole(id).subscribe({
        next: () => this.loadRoles(),
        error: (err) => console.error('Error al eliminar rol:', err)
      });
    }
  }

  // Abrir modal para gestionar permisos asignados
  openPermissionsModal(role: Role): void {
    this.selectedRoleId = role.id ?? null;
    this.selectedRoleName = role.name;
    this.selectedPermissionIds = role.permissions ? role.permissions.map(p => p.id!).filter(id => id !== undefined) : [];
    this.showPermissionModal = true;
  }

  // Marcar/desmarcar permiso
  togglePermission(permId: number | undefined): void {
    if (!permId) return;
    const index = this.selectedPermissionIds.indexOf(permId);
    if (index > -1) {
      this.selectedPermissionIds.splice(index, 1);
    } else {
      this.selectedPermissionIds.push(permId);
    }
  }

  // Guardar asignación de permisos
  savePermissions(): void {
    if (!this.selectedRoleId) return;
    this.rolService.updateRolePermissions(this.selectedRoleId, this.selectedPermissionIds).subscribe({
      next: () => {
        this.closePermissionModal();
        this.loadRoles();
      },
      error: (err) => console.error('Error al asignar permisos:', err)
    });
  }

  closeRoleModal(): void {
    this.showRoleModal = false;
  }

  closePermissionModal(): void {
    this.showPermissionModal = false;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
  }
}