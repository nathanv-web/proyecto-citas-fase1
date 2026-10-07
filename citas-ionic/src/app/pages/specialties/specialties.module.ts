import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular/lazy';

import { SpecialtiesPageRoutingModule } from './specialties-routing.module';

import { SpecialtiesPage } from './specialties.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    SpecialtiesPageRoutingModule
  ],
  declarations: [SpecialtiesPage]
})
export class SpecialtiesPageModule {}
