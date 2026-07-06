<template>
  <div ref="barcodeRef" style="display: inline-block;"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import JsBarcode from 'jsbarcode'
import type { BarcodeLabelProps } from '../types/api'

const props = withDefaults(defineProps<BarcodeLabelProps>(), {
  width: 2,
  height: 50,
  fontSize: 14,
  displayValue: true,
})

const barcodeRef = ref<HTMLElement | null>(null)

function render() {
  if (!barcodeRef.value) return
  JsBarcode(barcodeRef.value, props.barcode, {
    format: 'CODE128',
    width: props.width,
    height: props.height,
    fontSize: props.fontSize,
    displayValue: props.displayValue,
    margin: 4,
    background: '#ffffff',
    lineColor: '#000000',
  })
}

onMounted(render)
watch(() => props.barcode, render)
</script>
