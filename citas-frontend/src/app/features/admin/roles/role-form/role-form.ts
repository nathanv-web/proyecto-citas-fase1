import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { RoleService } from '../role.service';

@Component({
  selector: 'app-role-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './role-form.html'
})
export class RoleForm implements OnInit {

  private fb = inject(FormBuilder);
  private service = inject(RoleService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  id?: number;

  permissions =
    signal<string[]>([]);

  selectedPermissions =
    signal<string[]>([]);

  form = this.fb.group({
    nombre: [
      '',
      Validators.required
    ],

    descripcion: ['']
  });

  ngOnInit(): void {

    this.loadPermissions();

    const idParam =
      this.route.snapshot.paramMap.get('id');

    if (idParam) {

      this.id = Number(idParam);

      this.service
        .getRole(this.id)
        .subscribe(role => {

          this.form.patchValue({
            nombre: role.nombre,
            descripcion:
              role.descripcion
          });

          this.selectedPermissions.set(
            role.permisos ?? []
          );
        });
    }
  }

  loadPermissions() {

    this.service
      .getPermissions()
      .subscribe(data => {

        this.permissions.set(data);
      });
  }

  togglePermission(permission: string) {

    const current =
      this.selectedPermissions();

    if (current.includes(permission)) {

      this.selectedPermissions.set(
        current.filter(
          p => p !== permission
        )
      );

    } else {

      this.selectedPermissions.set([
        ...current,
        permission
      ]);
    }
  }

  save() {

    if (this.form.invalid) {
      return;
    }

    const role = {
      nombre:
        this.form.value.nombre!,

      descripcion:
        this.form.value.descripcion ?? ''
    };

    if (this.id) {

      this.service
        .updateRole(
          this.id,
          role
        )
        .subscribe(() => {

          this.service
            .assignPermissions(
              this.id!,
              this.selectedPermissions()
            )
            .subscribe(() => {

              this.router.navigate([
                '/admin/roles'
              ]);
            });
        });

    } else {

      this.service
        .createRole(role)
        .subscribe((created: any) => {

          const id =
            created.idRol;

          if (
            this.selectedPermissions()
              .length > 0
          ) {

            this.service
              .assignPermissions(
                id,
                this.selectedPermissions()
              )
              .subscribe(() => {

                this.router.navigate([
                  '/admin/roles'
                ]);
              });

          } else {

            this.router.navigate([
              '/admin/roles'
            ]);
          }
        });
    }
  }
}