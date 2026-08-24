// jsdom (the test environment) doesn't implement window.matchMedia, but
// ThemeService calls it eagerly on construction — any test that renders a
// component depending on ThemeService (directly or via ThemeToggle) fails
// without this polyfill.
if (typeof window !== 'undefined' && !window.matchMedia) {
  window.matchMedia = (query: string): MediaQueryList =>
    ({
      matches: false,
      media: query,
      onchange: null,
      addListener: () => {},
      removeListener: () => {},
      addEventListener: () => {},
      removeEventListener: () => {},
      dispatchEvent: () => false,
    }) as MediaQueryList;
}
