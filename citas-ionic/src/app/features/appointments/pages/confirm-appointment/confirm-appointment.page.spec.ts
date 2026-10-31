import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ConfirmAppointmentPage } from './confirm-appointment.page';

describe('ConfirmAppointmentPage', () => {
  let component: ConfirmAppointmentPage;
  let fixture: ComponentFixture<ConfirmAppointmentPage>;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    fixture = TestBed.createComponent(ConfirmAppointmentPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
