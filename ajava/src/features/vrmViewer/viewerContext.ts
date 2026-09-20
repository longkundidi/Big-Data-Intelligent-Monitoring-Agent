import { provide, inject } from 'vue';
import { Viewer } from './viewer';

const viewer = new Viewer();

const ViewerSymbol = Symbol();

export function provideViewer() {
  provide(ViewerSymbol, { viewer });
}

export function useViewerContext() {
  const context = inject(ViewerSymbol);
  if (!context) {
    throw new Error('useViewerContext must be used within a provideViewer');
  }
  return context;
}