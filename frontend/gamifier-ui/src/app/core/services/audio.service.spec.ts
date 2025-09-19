import { TestBed } from '@angular/core/testing';
import { fakeAsync, tick } from '@angular/core/testing';

import { AudioService } from './audio.service';

describe('AudioService', () => {
  let service: AudioService;
  let mockAudioContext: jasmine.SpyObj<AudioContext>;
  let mockOscillator: jasmine.SpyObj<OscillatorNode>;
  let mockGainNode: jasmine.SpyObj<GainNode>;
  let mockAudioParam: jasmine.SpyObj<AudioParam>;

  beforeEach(() => {
    // Create comprehensive mocks
    mockAudioParam = jasmine.createSpyObj('AudioParam', ['setValueAtTime', 'linearRampToValueAtTime', 'exponentialRampToValueAtTime']);

    mockOscillator = jasmine.createSpyObj('OscillatorNode', ['connect', 'start', 'stop'], {
      frequency: mockAudioParam,
      type: 'sine'
    });

    mockGainNode = jasmine.createSpyObj('GainNode', ['connect'], {
      gain: mockAudioParam
    });

    mockAudioContext = jasmine.createSpyObj('AudioContext', [
      'createOscillator',
      'createGain',
      'resume'
    ], {
      destination: {}
    });

    // Setup property descriptors for readonly properties
    Object.defineProperty(mockAudioContext, 'currentTime', {
      value: 0,
      writable: true,
      configurable: true
    });

    Object.defineProperty(mockAudioContext, 'state', {
      value: 'running',
      writable: true,
      configurable: true
    });

    mockAudioContext.createOscillator.and.returnValue(mockOscillator);
    mockAudioContext.createGain.and.returnValue(mockGainNode);

    // Mock the global AudioContext constructor
    spyOn(window, 'AudioContext').and.returnValue(mockAudioContext);
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

    it('should not initialize audio context immediately', () => {
      expect(window.AudioContext).not.toHaveBeenCalled();
    });
  });

  describe('Audio Context Management', () => {
    it('should initialize audio context when needed', () => {
      service.playBeep();

      expect(window.AudioContext).toHaveBeenCalled();
      expect(mockAudioContext.createOscillator).toHaveBeenCalled();
      expect(mockAudioContext.createGain).toHaveBeenCalled();
    });

    it('should use webkit audio context if available', () => {
      (window as any).AudioContext = undefined;

      service.playBeep();

      expect((window as any).webkitAudioContext).toHaveBeenCalled();
    });

    it('should resume audio context if suspended', async () => {
      Object.defineProperty(mockAudioContext, 'state', { value: 'suspended', writable: true });

      await service.resumeAudioContext();

      expect(mockAudioContext.resume).toHaveBeenCalled();
    });

    it('should not resume audio context if not suspended', async () => {
      Object.defineProperty(mockAudioContext, 'state', { value: 'running', writable: true });

      await service.resumeAudioContext();

      expect(mockAudioContext.resume).not.toHaveBeenCalled();
    });

    it('should handle audio context resume when not initialized', async () => {
      // Don't initialize audio context first
      await service.resumeAudioContext();

      expect(mockAudioContext.resume).not.toHaveBeenCalled();
    });
  });

  describe('Basic Audio Playback', () => {
    beforeEach(() => {
      Object.defineProperty(mockAudioContext, 'currentTime', { value: 1.5, writable: true });
    });

    it('should play beep with default parameters', () => {
      service.playBeep();

      expect(mockAudioContext.createOscillator).toHaveBeenCalled();
      expect(mockAudioContext.createGain).toHaveBeenCalled();
      expect(mockOscillator.connect).toHaveBeenCalledWith(jasmine.any(Object));
      expect(mockGainNode.connect).toHaveBeenCalledWith(jasmine.any(Object));
      expect(mockOscillator.frequency.setValueAtTime).toHaveBeenCalledWith(440, 1.5);
      expect(mockOscillator.start).toHaveBeenCalledWith(1.5);
      expect(mockOscillator.stop).toHaveBeenCalledWith(1.65); // 1.5 + 0.15 (150ms)
    });

    it('should play beep with custom frequency and duration', () => {
      service.playBeep(800, 200);

      expect(mockOscillator.frequency.setValueAtTime).toHaveBeenCalledWith(800, 1.5);
      expect(mockOscillator.start).toHaveBeenCalledWith(1.5);
      expect(mockOscillator.stop).toHaveBeenCalledWith(1.7); // 1.5 + 0.2 (200ms)
    });

    it('should configure gain envelope correctly', () => {
      service.playBeep(440, 150);

      expect(mockGainNode.gain.setValueAtTime).toHaveBeenCalledWith(0, 1.5);
      expect(mockGainNode.gain.linearRampToValueAtTime).toHaveBeenCalledWith(0.3, 1.51); // 1.5 + 0.01
      expect(mockGainNode.gain.exponentialRampToValueAtTime).toHaveBeenCalledWith(0.01, 1.65); // 1.5 + 0.15
    });

    it('should not play beep when disabled', () => {
      service.setEnabled(false);
      service.playBeep();

      expect(mockAudioContext.createOscillator).not.toHaveBeenCalled();
      expect(mockAudioContext.createGain).not.toHaveBeenCalled();
    });

    it('should handle audio context creation errors gracefully', () => {
      (window as any).AudioContext = jasmine.createSpy('AudioContext').and.throwError('Audio not supported');
      spyOn(console, 'warn');

      expect(() => service.playBeep()).not.toThrow();
      expect(console.warn).toHaveBeenCalledWith('Audio playback failed:', jasmine.any(Error));
    });

    it('should handle oscillator creation errors gracefully', () => {
      mockAudioContext.createOscillator.and.throwError('Oscillator creation failed');
      spyOn(console, 'warn');

      expect(() => service.playBeep()).not.toThrow();
      expect(console.warn).toHaveBeenCalledWith('Audio playback failed:', jasmine.any(Error));
    });
  });

  describe('LCARS Sound Effects', () => {
    beforeEach(() => {
      spyOn(service, 'playBeep');
    });

    it('should play button click sound', () => {
      service.playButtonClick();
      expect(service.playBeep).toHaveBeenCalledWith(800, 100);
    });

    it('should play button hover sound', () => {
      service.playButtonHover();
      expect(service.playBeep).toHaveBeenCalledWith(600, 80);
    });

    it('should play success sound sequence', fakeAsync(() => {
      service.playSuccess();

      expect(service.playBeep).toHaveBeenCalledWith(440, 150);

      tick(100);
      expect(service.playBeep).toHaveBeenCalledWith(554, 150);

      tick(100);
      expect(service.playBeep).toHaveBeenCalledWith(659, 200);
    }));

    it('should play error sound sequence', fakeAsync(() => {
      service.playError();

      expect(service.playBeep).toHaveBeenCalledWith(659, 150);

      tick(100);
      expect(service.playBeep).toHaveBeenCalledWith(554, 150);

      tick(100);
      expect(service.playBeep).toHaveBeenCalledWith(440, 200);
    }));

    it('should play notification sound sequence', fakeAsync(() => {
      service.playNotification();

      expect(service.playBeep).toHaveBeenCalledWith(523, 120);

      tick(150);
      expect(service.playBeep).toHaveBeenCalledWith(698, 120);
    }));

    it('should play alert sound sequence', fakeAsync(() => {
      service.playAlert();

      // Should play 3 beeps at 150ms intervals
      expect(service.playBeep).toHaveBeenCalledWith(880, 100);

      tick(150);
      expect(service.playBeep).toHaveBeenCalledWith(880, 100);

      tick(150);
      expect(service.playBeep).toHaveBeenCalledWith(880, 100);

      expect((service.playBeep as jasmine.Spy).calls.count()).toBe(3);
    }));

    it('should play system startup sound sequence', fakeAsync(() => {
      service.playSystemStartup();

      const expectedFrequencies = [220, 293, 369, 440, 554, 659, 784, 880];

      expectedFrequencies.forEach((freq, index) => {
        tick(index * 100);
        expect(service.playBeep).toHaveBeenCalledWith(freq, 200);
      });

      expect((service.playBeep as jasmine.Spy).calls.count()).toBe(8);
    }));
  });

  describe('Volume and Enable Controls', () => {
    it('should set and get enabled status', () => {
      expect(service.isEnabled()).toBe(true);

      service.setEnabled(false);
      expect(service.isEnabled()).toBe(false);

      service.setEnabled(true);
      expect(service.isEnabled()).toBe(true);
    });

    it('should set and get volume within valid range', () => {
      expect(service.getVolume()).toBe(0.3);

      service.setVolume(0.8);
      expect(service.getVolume()).toBe(0.8);

      service.setVolume(1.5); // Above max
      expect(service.getVolume()).toBe(1.0);

      service.setVolume(-0.5); // Below min
      expect(service.getVolume()).toBe(0.0);

      service.setVolume(0.5);
      expect(service.getVolume()).toBe(0.5);
    });

    it('should use updated volume in beep playback', () => {
      service.setVolume(0.7);
      service.playBeep();

      expect(mockGainNode.gain.linearRampToValueAtTime).toHaveBeenCalledWith(0.7, jasmine.any(Number));
    });

    it('should not play any sounds when disabled', () => {
      service.setEnabled(false);

      service.playButtonClick();
      service.playButtonHover();
      service.playSuccess();
      service.playError();
      service.playNotification();
      service.playAlert();
      service.playSystemStartup();

      expect(mockAudioContext.createOscillator).not.toHaveBeenCalled();
    });
  });

  describe('Complex Audio Sequences', () => {
    beforeEach(() => {
      // Use real playBeep for sequence testing
      spyOn(service as any, 'initializeAudioContext');
    });

    it('should handle multiple overlapping sounds', fakeAsync(() => {
      spyOn(service, 'playBeep');

      service.playSuccess();
      service.playError(); // Overlapping sequence

      // Fast forward through all timeouts
      tick(300);

      // Should have called playBeep for both sequences
      expect((service.playBeep as jasmine.Spy).calls.count()).toBe(6); // 3 + 3 beeps
    }));

    it('should handle rapid successive calls', () => {
      spyOn(service, 'playBeep');

      // Rapid button clicks
      service.playButtonClick();
      service.playButtonClick();
      service.playButtonClick();

      expect((service.playBeep as jasmine.Spy).calls.count()).toBe(3);
    });

    it('should handle startup sequence interruption gracefully', fakeAsync(() => {
      spyOn(service, 'playBeep');

      service.playSystemStartup();
      tick(250); // Partway through sequence
      service.setEnabled(false); // Disable audio
      tick(1000); // Continue to end

      // Should have played some beeps before being disabled
      expect((service.playBeep as jasmine.Spy).calls.count()).toBeGreaterThan(0);
      expect((service.playBeep as jasmine.Spy).calls.count()).toBeLessThan(8);
    }));
  });

  describe('Browser Compatibility', () => {
    it('should handle browsers without AudioContext', () => {
      (window as any).AudioContext = undefined;
      (window as any).webkitAudioContext = undefined;
      spyOn(console, 'warn');

      expect(() => service.playBeep()).not.toThrow();
      expect(console.warn).toHaveBeenCalled();
    });

    it('should handle audio context state changes', async () => {
      // Simulate suspended state
      Object.defineProperty(mockAudioContext, 'state', { value: 'suspended', writable: true });
      mockAudioContext.resume.and.returnValue(Promise.resolve());

      await service.resumeAudioContext();

      expect(mockAudioContext.resume).toHaveBeenCalled();

      // Simulate closed state
      Object.defineProperty(mockAudioContext, 'state', { value: 'closed', writable: true });
      mockAudioContext.resume.calls.reset();

      await service.resumeAudioContext();

      expect(mockAudioContext.resume).not.toHaveBeenCalled();
    });

    it('should handle audio context resume failures', async () => {
      Object.defineProperty(mockAudioContext, 'state', { value: 'suspended', writable: true });
      mockAudioContext.resume.and.returnValue(Promise.reject(new Error('Resume failed')));

      await expectAsync(service.resumeAudioContext()).toBeRejected();
    });
  });

  describe('Performance and Memory', () => {
    it('should reuse audio context once created', () => {
      service.playBeep();
      service.playBeep();

      // AudioContext constructor should only be called once
      expect(window.AudioContext).toHaveBeenCalledTimes(1);
    });

    it('should create new oscillators for each sound', () => {
      service.playBeep();
      service.playBeep();

      // Each beep should create its own oscillator
      expect(mockAudioContext.createOscillator).toHaveBeenCalledTimes(2);
      expect(mockAudioContext.createGain).toHaveBeenCalledTimes(2);
    });

    it('should not leak audio nodes', () => {
      // Play multiple sounds to ensure proper cleanup
      for (let i = 0; i < 5; i++) {
        service.playBeep(440 + i * 100, 50);
      }

      // Each sound should properly connect and disconnect
      expect(mockOscillator.connect).toHaveBeenCalledTimes(5);
      expect(mockGainNode.connect).toHaveBeenCalledTimes(5);
      expect(mockOscillator.start).toHaveBeenCalledTimes(5);
      expect(mockOscillator.stop).toHaveBeenCalledTimes(5);
    });
  });

  describe('Edge Cases', () => {
    it('should handle zero duration beep', () => {
      expect(() => service.playBeep(440, 0)).not.toThrow();
      expect(mockOscillator.start).toHaveBeenCalled();
      expect(mockOscillator.stop).toHaveBeenCalled();
    });

    it('should handle negative frequency', () => {
      expect(() => service.playBeep(-440, 100)).not.toThrow();
      expect(mockOscillator.frequency.setValueAtTime).toHaveBeenCalledWith(-440, jasmine.any(Number));
    });

    it('should handle very high frequency', () => {
      expect(() => service.playBeep(20000, 100)).not.toThrow();
      expect(mockOscillator.frequency.setValueAtTime).toHaveBeenCalledWith(20000, jasmine.any(Number));
    });

    it('should handle very long duration', () => {
      expect(() => service.playBeep(440, 10000)).not.toThrow();
      expect(mockOscillator.stop).toHaveBeenCalledWith(jasmine.any(Number));
    });

    it('should handle audio context creation timing issues', () => {
      // Simulate audio context creation delay
      let contextCreated = false;
      (window as any).AudioContext = jasmine.createSpy('AudioContext').and.callFake(() => {
        if (!contextCreated) {
          contextCreated = true;
          return mockAudioContext;
        }
        throw new Error('Context already created');
      });

      // Multiple rapid calls should not cause issues
      service.playBeep();
      service.playBeep();

      expect((window as any).AudioContext).toHaveBeenCalledTimes(1);
    });
  });

  describe('Integration with User Settings', () => {
    it('should respect disabled state across all sound methods', () => {
      service.setEnabled(false);
      spyOn(service, 'playBeep');

      // Test all public sound methods
      service.playButtonClick();
      service.playButtonHover();
      service.playSuccess();
      service.playError();
      service.playNotification();
      service.playAlert();
      service.playSystemStartup();

      expect(service.playBeep).not.toHaveBeenCalled();
    });

    it('should apply volume changes immediately', () => {
      service.setVolume(0.1);
      service.playBeep();

      expect(mockGainNode.gain.linearRampToValueAtTime).toHaveBeenCalledWith(0.1, jasmine.any(Number));

      service.setVolume(0.9);
      service.playBeep();

      expect(mockGainNode.gain.linearRampToValueAtTime).toHaveBeenCalledWith(0.9, jasmine.any(Number));
    });

    it('should maintain state after audio context errors', () => {
      service.setEnabled(true);
      service.setVolume(0.8);

      // Cause an audio error
      mockAudioContext.createOscillator.and.throwError('Audio error');
      spyOn(console, 'warn');

      service.playBeep();

      // Settings should be preserved
      expect(service.isEnabled()).toBe(true);
      expect(service.getVolume()).toBe(0.8);
    });
  });
});