export const environment = {
  production: false,
  apiUrl: 'http://localhost:9080/api',
  appName: 'Corporate Gamifier',
  version: '1.0.0',
  theme: {
    name: 'corporate', // Corporate theme configuration
    enableSounds: false, // Disabled by default for corporate environments
    enableAnimations: true,
    enableEffects: false // No special effects for professional look
  },
  audio: {
    enabled: false, // Disabled by default in corporate theme
    volume: 0.2
  },
  features: {
    enableAudio: false,
    enableAnimations: true,
    enableLCARSEffects: false // No LCARS effects in corporate theme
  }
};