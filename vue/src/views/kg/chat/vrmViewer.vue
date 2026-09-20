<template>
  <div class="vrmViewerContainer">
    <canvas ref="canvasRef" class="h-full w-full"></canvas>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useViewerContext } from '@/features/vrmViewer/viewerContext';
import { buildUrl } from '@/utils/buildUrl';

export default {
  name: 'VrmViewer',
  setup() {
    const { viewer } = useViewerContext();
    const canvasRef = ref(null);

    onMounted(() => {
      const canvas = canvasRef.value;
      if (canvas) {
        viewer.setup(canvas);
        viewer.loadVrm(buildUrl('/AvatarSample_C.vrm'));

        // Drag and DropでVRMを差し替え
        canvas.addEventListener('dragover', (event) => {
          event.preventDefault();
        });

        canvas.addEventListener('drop', (event) => {
          event.preventDefault();

          const files = event.dataTransfer?.files;
          if (!files) {
            return;
          }

          const file = files[0];
          if (!file) {
            return;
          }

          const file_type = file.name.split('.').pop();
          if (file_type === 'vrm') {
            const blob = new Blob([file], { type: 'application/octet-stream' });
            const url = window.URL.createObjectURL(blob);
            viewer.loadVrm(url);
          }
        });
      }
    });

    return {
      canvasRef,
    };
  },
};
</script>

<style scoped>
.vrmViewerContainer {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* .top-0 {
  top: 0;
}
.left-0 {
  left: 0;
}
.w-screen {
  width: 100vw;
}
.h-[100svh] {
  height: 100svh;
}
.-z-10 {
  z-index: -10;
} */
.h-full {
  height: 100%;
}
.w-full {
  width: 100%;
}
</style>