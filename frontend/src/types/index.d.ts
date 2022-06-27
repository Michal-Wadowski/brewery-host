export {};

declare global {
  interface Window {
    globals: any; // 👈️ turn off type checking
  }
}
