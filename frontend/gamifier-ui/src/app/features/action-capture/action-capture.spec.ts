import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionCapture } from './action-capture';

describe('ActionCapture', () => {
  let component: ActionCapture;
  let fixture: ComponentFixture<ActionCapture>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActionCapture]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActionCapture);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
