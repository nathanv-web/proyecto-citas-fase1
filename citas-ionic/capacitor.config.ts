
import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'io.ionic.starter',
  appName: 'citas-ionic',
  webDir: 'www',

  // Permitir conexiones HTTP durante el desarrollo
  server: {
    cleartext: true
  },

  // Permitir solicitudes HTTP desde la WebView
  android: {
    allowMixedContent: true
  }
};

export default config;
