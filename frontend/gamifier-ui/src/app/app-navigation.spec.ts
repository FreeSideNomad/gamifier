import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { App } from './app';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

describe('App Navigation Integration', () => {
  let component: App;
  let fixture: any;
  let router: Router;
  let location: Location;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter(routes),
        provideHttpClient()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(App);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    location = TestBed.inject(Location);

    fixture.detectChanges();
  });

  it('should navigate to dashboard on default route', async () => {
    await router.navigate(['']);
    expect(location.path()).toBe('/dashboard');
  });

  it('should navigate when clicking missions link', async () => {
    const missionsLink = fixture.debugElement.query(By.css('a[routerLink="/missions"]'));
    expect(missionsLink).toBeTruthy();

    missionsLink.nativeElement.click();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(location.path()).toBe('/missions');
  });

  it('should navigate when clicking leaderboards link', async () => {
    const leaderboardsLink = fixture.debugElement.query(By.css('a[routerLink="/leaderboards"]'));
    expect(leaderboardsLink).toBeTruthy();

    leaderboardsLink.nativeElement.click();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(location.path()).toBe('/leaderboards');
  });

  it('should navigate when clicking actions link', async () => {
    const actionsLink = fixture.debugElement.query(By.css('a[routerLink="/actions"]'));
    expect(actionsLink).toBeTruthy();

    actionsLink.nativeElement.click();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(location.path()).toBe('/actions');
  });

  it('should show active class on current route', async () => {
    await router.navigate(['/missions']);
    fixture.detectChanges();

    const missionsLink = fixture.debugElement.query(By.css('a[routerLink="/missions"]'));
    expect(missionsLink.nativeElement.classList.contains('active')).toBeTruthy();
  });

  it('should display admin sections when user is admin', () => {
    component.currentUser.set({
      id: 1,
      name: 'Test Admin',
      email: 'admin@starfleet.com',
      rank: 'ADMIRAL',
      points: 5000,
      role: 'ADMIN'
    });
    fixture.detectChanges();

    const adminUsersLink = fixture.debugElement.query(By.css('a[routerLink="/admin/users"]'));
    expect(adminUsersLink).toBeTruthy();
  });

  it('should hide admin sections when user is not admin', () => {
    component.currentUser.set({
      id: 2,
      name: 'Test User',
      email: 'user@starfleet.com',
      rank: 'ENSIGN',
      points: 100,
      role: 'USER'
    });
    fixture.detectChanges();

    const adminUsersLink = fixture.debugElement.query(By.css('a[routerLink="/admin/users"]'));
    expect(adminUsersLink).toBeFalsy();
  });

  it('should play click sound when navigation link is clicked', () => {
    spyOn(component, 'playClickSound');

    const missionsLink = fixture.debugElement.query(By.css('a[routerLink="/missions"]'));
    missionsLink.nativeElement.click();

    expect(component.playClickSound).toHaveBeenCalled();
  });

  it('should play hover sound when navigation link is hovered', () => {
    spyOn(component, 'playHoverSound');

    const missionsLink = fixture.debugElement.query(By.css('a[routerLink="/missions"]'));
    missionsLink.nativeElement.dispatchEvent(new Event('mouseenter'));

    expect(component.playHoverSound).toHaveBeenCalled();
  });
});