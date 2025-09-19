import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

@Component({
  template: '<router-outlet></router-outlet>',
  imports: [RouterOutlet]
})
class TestComponent { }

describe('App Routing', () => {
  let router: Router;
  let location: Location;
  let fixture: any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideRouter(routes)
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    fixture = TestBed.createComponent(TestComponent);
    fixture.detectChanges();
  });

  it('should navigate to dashboard by default', async () => {
    await router.navigate(['']);
    expect(location.path()).toBe('/dashboard');
  });

  it('should navigate to missions', async () => {
    await router.navigate(['/missions']);
    expect(location.path()).toBe('/missions');
  });

  it('should navigate to leaderboards', async () => {
    await router.navigate(['/leaderboards']);
    expect(location.path()).toBe('/leaderboards');
  });

  it('should navigate to actions', async () => {
    await router.navigate(['/actions']);
    expect(location.path()).toBe('/actions');
  });

  it('should navigate to admin/users', async () => {
    await router.navigate(['/admin/users']);
    expect(location.path()).toBe('/admin/users');
  });

  it('should navigate to admin/organization', async () => {
    await router.navigate(['/admin/organization']);
    expect(location.path()).toBe('/admin/organization');
  });

  it('should navigate to admin/reports', async () => {
    await router.navigate(['/admin/reports']);
    expect(location.path()).toBe('/admin/reports');
  });

  it('should navigate to not-found for invalid routes', async () => {
    await router.navigate(['/invalid-route']);
    expect(location.path()).toBe('/invalid-route');
  });
});