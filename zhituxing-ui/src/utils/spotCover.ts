import type { Spot } from '@/types'
import jinfoshan from '@/assets/scenery/jinfoshan.jpg'
import karst from '@/assets/scenery/karst.jpg'
import dazu from '@/assets/scenery/dazu.jpg'
import gugong from '@/assets/scenery/gugong.jpg'
import fallback from '@/assets/scenery/hero.jpg'
const covers: Record<string, string> = { 金佛山: jinfoshan, 喀斯特: karst, 大足石刻: dazu, 故宫: gugong }
export function spotCover(spot: Pick<Spot, 'name' | 'cover'>): string { return spot.cover || covers[spot.name] || fallback }
