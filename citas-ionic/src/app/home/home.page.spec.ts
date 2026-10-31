import { ComponentFixture, TestBed } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular/lazy';
import { Router } from '@angular/router';
import { Auth } from '../core/auth/services/auth';

import { HomePage } from './home.page';

describe('HomePage', () => {
  let component: HomePage;
  let fixture: ComponentFixture<HomePage>;
  let authMock: { getUsuario: ReturnType<typeof vi.fn>; logout: ReturnType<typeof vi.fn> };
  let navigateByUrl: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    authMock = { getUsuario: vi.fn(() => null), logout: vi.fn() };
    navigateByUrl = vi.fn().mockResolvedValue(true);
    await TestBed.configureTestingModule({
      declarations: [HomePage],
      imports: [IonicModule],
      providers: [
        { provide: Router, useValue: { navigateByUrl } },
        { provide: Auth, useValue: authMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HomePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('permite que el botón de cerrar sesión llegue a su acción', () => {
    const button = fixture.nativeElement.querySelector('ion-button') as HTMLElement;
    button.dispatchEvent(new MouseEvent('click', { bubbles: true }));

    expect(authMock.logout).toHaveBeenCalledOnce();
    expect(navigateByUrl).toHaveBeenCalledWith('/login', { replaceUrl: true });
  });
});
