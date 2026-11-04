import catalog from '@/data/catalog.json'
export const destinationInfo = (name: string) => catalog.spots.find(item => item.name === name)
export const guideInfo = (title: string) => catalog.guides.find(item => item.title === title)
export function priceBrief(name: string, fallback: number): string {
  const item = destinationInfo(name)
  if (!item) return `¥${fallback}`
  if (item.price === undefined) return '票价见详情'
  return item.price === 0 ? '基本参观免费' : `¥${item.price}起`
}

export const destinationCities = [...new Set(catalog.spots.map(item => item.city))]
