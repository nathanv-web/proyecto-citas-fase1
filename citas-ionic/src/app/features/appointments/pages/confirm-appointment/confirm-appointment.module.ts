import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular/lazy';

import { ConfirmAppointmentPageRoutingModule } from './confirm-appointment-routing.module';

import { ConfirmAppointmentPage } from './confirm-appointment.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ConfirmAppointmentPageRoutingModule
  ],
  declarations: [ConfirmAppointmentPage]
})
export class ConfirmAppointmentPageModule {}
