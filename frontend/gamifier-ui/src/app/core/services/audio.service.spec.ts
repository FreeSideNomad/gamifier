import { TestBed } from '@angular/core/testing';
import { AudioService } from './audio.service';

describe('AudioService', () => {
  let service: AudioService;

  beforeEach(() => {
    // Mock the global AudioContext constructor to avoid Web Audio API complexity
    const mockAudioContext = {
      createOscillator: jasmine.createSpy('createOscillator').and.returnValue({
        frequency: { setValueAtTime: jasmine.createSpy() },
        connect: jasmine.createSpy(),
        start: jasmine.createSpy(),
        stop: jasmine.createSpy(),
        type: 'sine'
      }),
      createGain: jasmine.createSpy('createGain').and.returnValue({
        gain: {
          setValueAtTime: jasmine.createSpy(),
          linearRampToValueAtTime: jasmine.createSpy(),
          exponentialRampToValueAtTime: jasmine.createSpy()
        },
        connect: jasmine.createSpy()
      }),
      resume: jasmine.createSpy('resume').and.returnValue(Promise.resolve()),
      currentTime: 0,
      state: 'running',
      destination: {}
    };

    spyOn(window, 'AudioContext').and.returnValue(mockAudioContext as any);
    (window as any).webkitAudioContext = jasmine.createSpy('webkitAudioContext').and.returnValue(mockAudioContext);

    TestBed.configureTestingModule({});
    service = TestBed.inject(AudioService);
  });

  describe('Service Initialization', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(service.isEnabled()).toBe(true);
      expect(service.getVolume()).toBe(0.3);
    });
  });

  describe('Basic Functionality', () => {
    it('should play button click sound', () => {
      spyOn(service, 'playBeep');
      service.playButtonClick();
      expect(service.playBeep).toHaveBeenCalled();
    });

    it('should play button hover sound', () => {
      spyOn(service, 'playBeep');
      service.playButtonHover();
      expect(service.playBeep).toHaveBeenCalled();
    });

    it('should play success sound', () => {
      spyOn(service, 'playBeep');
      service.playSuccess();
      expect(service.playBeep).toHaveBeenCalled();
    });

    it('should play error sound', () => {
      spyOn(service, 'playBeep');
      service.playError();
      expect(service.playBeep).toHaveBeenCalled();
    });

    it('should play notification sound', () => {
      spyOn(service, 'playBeep');
      service.playNotification();
      expect(service.playBeep).toHaveBeenCalled();
    });

    it('should play system startup sound', () => {
      spyOn(service, 'playBeep');
      service.playSystemStartup();
      expect(service.playBeep).toHaveBeenCalled();
    });
  });

  describe('Settings Management', () => {
    it('should enable and disable audio', () => {
      service.setEnabled(false);
      expect(service.isEnabled()).toBe(false);

      service.setEnabled(true);
      expect(service.isEnabled()).toBe(true);
    });

    it('should update volume', () => {
      service.setVolume(0.8);
      expect(service.getVolume()).toBe(0.8);
    });

    it('should clamp volume between 0 and 1', () => {
      service.setVolume(-0.1);
      expect(service.getVolume()).toBe(0);

      service.setVolume(1.5);
      expect(service.getVolume()).toBe(1);
    });
  });

  describe('Audio Context Management', () => {
    it('should handle resumeAudioContext gracefully', async () => {
      const result = await service.resumeAudioContext();
      expect(result).toBeUndefined();
    });

    it('should not play sounds when disabled', () => {
      service.setEnabled(false);
      spyOn(window, 'AudioContext');

      service.playButtonClick();

      expect(window.AudioContext).not.toHaveBeenCalled();
    });
  });
});