<script setup lang="ts">
withDefaults(
  defineProps<{
    /** 容器最小高度，默认铺满视口 */
    minHeight?: string
  }>(),
  { minHeight: '100vh' },
)
</script>

<template>
  <div class="animated-bg" :style="{ minHeight }">
    <div class="animated-bg__layers" aria-hidden="true">
      <div class="animated-bg__gradient" />
      <div class="animated-bg__grid" />
      <span class="animated-bg__orb animated-bg__orb--1" />
      <span class="animated-bg__orb animated-bg__orb--2" />
      <span class="animated-bg__orb animated-bg__orb--3" />
    </div>

    <div class="animated-bg__content">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.animated-bg {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  color: var(--portal-text-on-dark, #fff);
}

.animated-bg__layers {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.animated-bg__gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    var(--portal-gradient-start, #0a2a6b) 0%,
    var(--portal-gradient-mid, #1677ff) 45%,
    var(--portal-gradient-end, #0d1f4a) 100%
  );
  background-size: 200% 200%;
  animation: animated-bg-gradient 24s ease-in-out infinite;
}

.animated-bg__grid {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: radial-gradient(ellipse 80% 70% at 50% 40%, #000 20%, transparent 75%);
}

.animated-bg__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
  will-change: transform;
}

.animated-bg__orb--1 {
  top: -12%;
  left: -6%;
  width: min(50vw, 480px);
  height: min(50vw, 480px);
  background: var(--portal-glow-1, rgba(64, 150, 255, 0.45));
  animation: animated-bg-float-1 20s ease-in-out infinite;
}

.animated-bg__orb--2 {
  right: -8%;
  bottom: 8%;
  width: min(42vw, 400px);
  height: min(42vw, 400px);
  background: var(--portal-glow-2, rgba(22, 119, 255, 0.35));
  animation: animated-bg-float-2 26s ease-in-out infinite;
}

.animated-bg__orb--3 {
  top: 38%;
  left: 32%;
  width: min(28vw, 260px);
  height: min(28vw, 260px);
  background: rgba(255, 255, 255, 0.1);
  animation: animated-bg-float-3 18s ease-in-out infinite;
}

.animated-bg__content {
  position: relative;
  z-index: 1;
  min-height: inherit;
}

@keyframes animated-bg-gradient {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes animated-bg-float-1 {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(28px, 36px);
  }
}

@keyframes animated-bg-float-2 {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(-36px, -28px);
  }
}

@keyframes animated-bg-float-3 {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(18px, -22px) scale(1.06);
  }
}

@media (max-width: 768px) {
  .animated-bg__grid {
    background-size: 32px 32px;
    opacity: 0.28;
  }

  .animated-bg__orb {
    filter: blur(40px);
    opacity: 0.4;
  }

  .animated-bg__orb--1 {
    width: min(70vw, 320px);
    height: min(70vw, 320px);
  }

  .animated-bg__orb--2 {
    width: min(55vw, 260px);
    height: min(55vw, 260px);
  }

  .animated-bg__orb--3 {
    width: min(45vw, 200px);
    height: min(45vw, 200px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .animated-bg__gradient,
  .animated-bg__orb {
    animation: none !important;
  }
}
</style>
