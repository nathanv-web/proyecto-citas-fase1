import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';

import { Auth } from '../../core/auth/services/auth';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  standalone: false,
})
export class LoginPage implements OnInit {

  loginForm: FormGroup;

  loading = false;
  errorMessage = '';

  constructor(
  private formBuilder: FormBuilder,
  private authService: Auth,
  private router: Router,
  private route: ActivatedRoute
) {
    this.loginForm = this.formBuilder.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],
      password: [
        '',
        [
          Validators.required
        ]
      ]
    });
  }

  ngOnInit(): void {
  }

  iniciarSesion(): void {

    this.errorMessage = '';

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    const credentials = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password
    };

    this.authService.login(credentials).subscribe({
      next: () => {
        this.loading = false;

        const returnUrl =
  this.route.snapshot.queryParamMap.get('returnUrl')
  || '/home';

this.router.navigateByUrl(
  returnUrl,
  {
    replaceUrl: true
  }
);  
      },

      error: (error: HttpErrorResponse) => {
        this.loading = false;

        if (error.status === 0) {
          this.errorMessage =
            'No se pudo conectar con el servidor.';
          return;
        }

        this.errorMessage =
          error.error?.message ||
          'No fue posible iniciar sesión.';
      }
    });
  }

}
