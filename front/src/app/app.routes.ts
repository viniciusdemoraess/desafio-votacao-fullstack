import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { PautaListComponent } from './components/pauta-list/pauta-list.component';
import { PautaFormComponent } from './components/pauta-form/pauta-form.component';
import { PautaDetailComponent } from './components/pauta-detail/pauta-detail.component';
import { VotingComponent } from './components/voting/voting.component';
import { AssociadoListComponent } from './components/associado-list/associado-list.component';
import { ApiTestComponent } from './components/api-test/api-test.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'pautas', component: PautaListComponent },
  { path: 'pautas/nova', component: PautaFormComponent },
  { path: 'pautas/:id', component: PautaDetailComponent },
  { path: 'pautas/:id/votar', component: VotingComponent },
  { path: 'associados', component: AssociadoListComponent },
  { path: 'api-test', component: ApiTestComponent },
  { path: '**', redirectTo: '' }
];
