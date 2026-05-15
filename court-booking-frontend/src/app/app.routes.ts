import { Routes } from '@angular/router';

import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { DashboardComponent } from './user/dashboard/dashboard.component';
import { CourtsComponent } from './user/courts/courts.component';
import { BookingComponent } from './user/booking/booking.component';
import { PaymentComponent } from './user/payment/payment.component';
import { AdminDashboardComponent } from './admin/admin-dashboard/admin-dashboard.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  { path: 'dashboard', component: DashboardComponent },
  { path: 'courts', component: CourtsComponent },
  { path: 'booking', component: BookingComponent },
  { path: 'payment', component: PaymentComponent },

  { path: 'admin', component: AdminDashboardComponent }
];