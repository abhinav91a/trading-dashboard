import { useEffect, useRef } from 'react'
import { createChart, CandlestickSeries } from 'lightweight-charts'

const COLORS = {
  background: '#131821',
  text: '#8b95a7',
  grid: '#1d2533',
  border: '#252e3d',
  up: '#22c55e',
  down: '#f43f5e',
}

export default function CandlestickChart({ candles }) {
  const containerRef = useRef(null)
  const chartRef = useRef(null)
  const seriesRef = useRef(null)

  useEffect(() => {
    const chart = createChart(containerRef.current, {
      autoSize: true,
      layout: { background: { type: 'solid', color: COLORS.background }, textColor: COLORS.text },
      grid: { vertLines: { color: COLORS.grid }, horzLines: { color: COLORS.grid } },
      timeScale: { timeVisible: false, borderColor: COLORS.border },
      rightPriceScale: { borderColor: COLORS.border },
    })
    const series = chart.addSeries(CandlestickSeries, {
      upColor: COLORS.up,
      downColor: COLORS.down,
      borderUpColor: COLORS.up,
      borderDownColor: COLORS.down,
      wickUpColor: COLORS.up,
      wickDownColor: COLORS.down,
    })
    chartRef.current = chart
    seriesRef.current = series

    return () => chart.remove()
  }, [])

  useEffect(() => {
    seriesRef.current?.setData(candles)
    chartRef.current?.timeScale().fitContent()
  }, [candles])

  return <div ref={containerRef} className="chart" />
}
