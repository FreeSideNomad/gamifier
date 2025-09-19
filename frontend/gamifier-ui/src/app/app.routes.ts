import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    title: 'Dashboard - Starfleet Gamifier'
  },
  {
    path: 'missions',
    loadComponent: () => import('./features/missions/missions.component').then(m => m.MissionsComponent),
    title: 'Missions - Starfleet Gamifier'
  },
  {
    path: 'leaderboards',
    loadComponent: () => import('./features/leaderboards/leaderboards.component').then(m => m.LeaderboardsComponent),
    title: 'Leaderboards - Starfleet Gamifier'
  },
  {
    path: 'actions',
    loadComponent: () => import('./features/actions/actions.component').then(m => m.ActionsComponent),
    title: 'Actions - Starfleet Gamifier'
  },
  {
    path: 'admin',
    children: [
      {
        path: '',
        redirectTo: 'users',
        pathMatch: 'full'
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin/users/users.component').then(m => m.UsersComponent),
        title: 'User Management - Starfleet Gamifier'
      },
      {
        path: 'organization',
        loadComponent: () => import('./features/admin/organization/organization.component').then(m => m.OrganizationComponent),
        title: 'Organization Settings - Starfleet Gamifier'
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/admin/reports/reports.component').then(m => m.ReportsComponent),
        title: 'Reports - Starfleet Gamifier'
      }
    ]
  },
  {
    path: '**',
    loadComponent: () => import('./shared/components/not-found/not-found.component').then(m => m.NotFoundComponent),
    title: 'Page Not Found - Starfleet Gamifier'
  }
];
