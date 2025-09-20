export const environment = {
  production: false,
  apiUrl: 'http://localhost:9080/api',
  appName: 'Starfleet Gamifier',
  version: '1.0.0',
  theme: {
    name: 'starfleet', // 'starfleet' or 'corporate'
    enableSounds: true,
    enableAnimations: true,
    enableEffects: true
  },
  audio: {
    enabled: true,
    volume: 0.3
  },
  features: {
    enableAudio: true,
    enableAnimations: true,
    enableLCARSEffects: true
  },
  mockData: {
    enabled: false,
    fallbackOnError: false
  }
};