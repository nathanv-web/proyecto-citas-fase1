import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** La bienvenida presenta CitaNova y ofrece acceso al registro y al inicio de sesión. */
@Component({
  imports: [RouterLink],
  selector: 'app-welcome',
  styleUrl: './welcome.css',
  templateUrl: './welcome.html',
})
export class Welcome {}
