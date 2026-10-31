import { HttpClient } from '@angular/common/http';
import { firstValueFrom, of } from 'rxjs';
import { Preferences } from '@capacitor/preferences';
import { Auth } from './auth';

vi.mock('@capacitor/preferences', () => ({
  Preferences: { get: vi.fn(), set: vi.fn(), remove: vi.fn() },
}));

describe('Auth', () => {
  let service: Auth;
  const user = {
    idUsuario: 8,
    nombre: 'Ana',
    apellido: 'Lopez',
    correo: 'ana@example.com',
    roles: ['ROLE_PATIENT'],
    permisos: ['VER_HISTORIAL'],
  };

  beforeEach(() => {
    const values = new Map<string, string>();
    vi.stubGlobal('localStorage', {
      getItem: (key: string) => values.get(key) ?? null,
      setItem: (key: string, value: string) => values.set(key, value),
      removeItem: (key: string) => values.delete(key),
      clear: () => values.clear(),
    });
    vi.mocked(Preferences.get).mockReset().mockResolvedValue({ value: null });
    vi.mocked(Preferences.set).mockReset().mockResolvedValue();
    vi.mocked(Preferences.remove).mockReset().mockResolvedValue();
    service = new Auth({ post: vi.fn() } as unknown as HttpClient);
  });

  afterEach(() => vi.unstubAllGlobals());

  it('restores a saved token and user from Capacitor Preferences', async () => {
    vi.mocked(Preferences.get).mockImplementation(async ({ key }) => ({
      value: key === 'auth_token' ? 'saved-token' : JSON.stringify(user),
    }));

    await expect(service.restoreSession()).resolves.toBe(true);
    expect(service.getToken()).toBe('saved-token');
    expect(service.getUsuario()).toEqual(user);
    expect(service.hasPermission('VER_HISTORIAL')).toBe(true);
  });

  it('migrates an existing browser session into Preferences', async () => {
    localStorage.setItem('auth_token', 'old-token');
    localStorage.setItem('auth_user', JSON.stringify(user));

    await expect(service.restoreSession()).resolves.toBe(true);
    expect(Preferences.set).toHaveBeenCalledWith({ key: 'auth_token', value: 'old-token' });
    expect(Preferences.set).toHaveBeenCalledWith({ key: 'auth_user', value: JSON.stringify(user) });
    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(localStorage.getItem('auth_user')).toBeNull();
  });

  it('persists login data before emitting a successful login', async () => {
    const response = { token: 'new-token', tipo: 'Bearer', usuario: user };
    const post = vi.fn().mockReturnValue(of(response));
    service = new Auth({ post } as unknown as HttpClient);

    await expect(firstValueFrom(service.login({ email: user.correo, password: 'secret' }))).resolves.toEqual(response);
    expect(Preferences.set).toHaveBeenCalledWith({ key: 'auth_token', value: 'new-token' });
    expect(service.getToken()).toBe('new-token');
    expect(service.getUsuario()).toEqual(user);
  });

  it('clears the in-memory session and native preferences on logout', async () => {
    vi.mocked(Preferences.get).mockImplementation(async ({ key }) => ({
      value: key === 'auth_token' ? 'saved-token' : JSON.stringify(user),
    }));
    await service.restoreSession();

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.getUsuario()).toBeNull();
    expect(Preferences.remove).toHaveBeenCalledWith({ key: 'auth_token' });
    expect(Preferences.remove).toHaveBeenCalledWith({ key: 'auth_user' });
  });
});
