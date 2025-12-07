import { Routes } from '@angular/router';
import { canActivateAuthRole } from './guards/auth.guard'
import { HomeComponent } from './components/home/home.component';
import { VoorzieningenComponent } from './components/voorzieningen/voorzieningen.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  {
    path: 'voorzieningen',
    component: VoorzieningenComponent,
    canActivate: [canActivateAuthRole],
    data: { role: 'admin' }
  },
  { path: '**', redirectTo: '' },
];
