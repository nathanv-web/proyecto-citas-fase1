import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfirmAppointmentPage } from './confirm-appointment.page';

describe('ConfirmAppointmentPage', () => {
  let component: ConfirmAppointmentPage;
  let fixture: ComponentFixture<ConfirmAppointmentPage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmAppointmentPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
