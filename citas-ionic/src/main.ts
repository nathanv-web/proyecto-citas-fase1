import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { registerAppIcons } from './app/core/config/icons.config';

registerAppIcons();

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.log(err));
