import request from '@/config/axios'

// 机架历史数据 API
export const EnergyConsumptionApi = {

  // 查询机架电量数据分页
  getEQDataPage: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/page`, params })
  },

   // 查询机架电量费数据分页
   getBillDataPage: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/bill-page`, params })
  },

  // 查询机架电量数据详情
  getEQDataDetails: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/details`, params })
  },

  // 查询机架各输出位电量数据详情
  getOutletsEQData: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/outlets-details`, params })
  },

  // 查询机架实时电量数据分页
  getRealtimeEQDataPage: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/realtime-page`, params })
  },

  // 查询机架导航的一周数据显示
  getNavOneWeekData: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/one-week`, params })
  },

  // 查询机架导航的一天数据显示
  getNavOneDayData: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/one-day`, params })
  },
  

}