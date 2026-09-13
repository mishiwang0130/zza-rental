/**
 * 城市标签选项（演示阶段直接写死，不接公寓系统的城市表）。
 *
 * value 为空字符串表示"不限城市"，此时提问不带 city 参数，检索不做城市过滤。
 */
export const CITY_OPTIONS: ReadonlyArray<{ label: string; value: string }> = [
  { label: '不限城市', value: '' },
  { label: '武汉', value: '武汉' },
  { label: '广州', value: '广州' },
  { label: '深圳', value: '深圳' },
]
