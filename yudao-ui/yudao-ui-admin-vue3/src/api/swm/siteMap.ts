import request from '@/config/axios';
// ErrorMessageMode type

enum Api {
  SiteMapList = '/swm/swmSiteMapManagement/listData',
  SiteMapSave = '/swm/swmSiteMapManagement/save',
  SiteMapDelete = '/swm/swmSiteMapManagement/delete',
  SiteMapEnable = '/swm/swmSiteMapManagement/enable',
  SiteMapDisable = '/swm/swmSiteMapManagement/disable',
  CompanyList = '/swm/common/options/companies',
}

/**
 * 获取公司列表数据
 */
export function getCompanyOptions() {
  return request.get({ url: Api.CompanyList });
}

/**
 * 获取场地底图列表
 */
export function getSiteMapList(params?: any, mode: ErrorMessageMode = 'modal') {
  return request
    .get(
      {
        url: Api.SiteMapList,
        params,
      },
      {
        errorMessageMode: mode,
        isTransformResponse: false, // 关闭全局转换，手动处理数据
      },
    )
    .then(async (res) => {
      // 获取公司选项数据用于转换office_code为office_name
      let companyOptions = [];
      try {
        companyOptions = await getCompanyOptions();
      } catch (error) {
        console.warn('获取公司选项失败:', error);
        companyOptions = [];
      }

      // 创建office_code到office_name的映射
      const companyMap = new Map();
      companyOptions.forEach((company) => {
        companyMap.set(company.value, company.label);
      });

      // 将服务器返回的数据格式转换为组件需要的格式
      const { list, count, pageNo, pageSize } = res;

      // 将字段名映射为组件使用的字段名
      const items = list.map((item) => {
        // 解析filePath中的JSON字符串获取实际URL
        let fileUrl = '';
        let fileName = '';
        if (item.filePath) {
          try {
            const fileInfo = JSON.parse(item.filePath);
            fileUrl = fileInfo.url || fileInfo.previewUrl || '';
            fileName = fileInfo.fileName || '';
          } catch (e) {
            fileUrl = item.filePath;
          }
        }

        // 将projectId(office_code)转换为对应的office_name用于显示
        const projectName = companyMap.get(item.projectId) || item.projectId;

        return {
          id: item.id,
          name: item.mapName,
          project: projectName, // 显示公司名称
          projectId: item.projectId, // 保留原始projectId用于编辑
          size: item.mapSize,
          scale: item.scale,
          url: fileUrl,
          is3d: item.is3d,
          fileName: fileName,
          filePath: item.filePath, // 保留原始JSON字符串以备其他用途
          drawingPixelX: item.drawingPixelX,
          drawingPixelY: item.drawingPixelY,
          siteCoordinateXM: item.siteCoordinateXM,
          siteCoordinateYM: item.siteCoordinateYM,
          status: item.status === '0' ? '启用' : '禁用', // 假设0表示启用
          createDate: item.createDate,
          updateDate: item.updateDate,
          remarks: item.remarks,
        };
      });

      // 返回符合BasicTable需要的格式
      return {
        items, // 数据列表
        total: count, // 总条数
        pageNo: pageNo || 1, // 当前页码
        pageSize: pageSize || 20, // 每页条数
      };
    });
}

/**
 * 创建场地底图
 */
export function createSiteMap(params: any, mode: ErrorMessageMode = 'modal') {
  // 将组件字段映射回服务器期望的字段
  const serverParams = {
    mapName: params.name,
    projectId: params.project,
    mapSize: params.size,
    scale: params.scale,
    filePath: params.url,
    drawingPixelX: params.drawingPixelX,
    drawingPixelY: params.drawingPixelY,
    siteCoordinateXM: params.siteCoordinateXM,
    siteCoordinateYM: params.siteCoordinateYM,
    status: params.status === '启用' ? '0' : '1', // 假设0表示启用，1表示禁用
    remarks: params.remarks || '',
  };

  return request.post(
    {
      url: Api.SiteMapSave,
      params: serverParams,
    },
    {
      errorMessageMode: mode,
    },
  );
}

/**
 * 更新场地底图
 */
export function updateSiteMap(params: any, mode: ErrorMessageMode = 'modal') {
  // 将组件字段映射回服务器期望的字段
  const serverParams = {
    id: params.id,
    mapName: params.name,
    projectId: params.project,
    mapSize: params.size,
    scale: params.scale,
    filePath: params.url,
    is3d: params.is3d,
    drawingPixelX: params.drawingPixelX,
    drawingPixelY: params.drawingPixelY,
    siteCoordinateXM: params.siteCoordinateXM,
    siteCoordinateYM: params.siteCoordinateYM,
    status: params.status === '启用' ? '0' : '1', // 假设0表示启用，1表示禁用
    remarks: params.remarks || '',
  };

  return request.post(
    {
      url: Api.SiteMapSave,
      params: serverParams,
    },
    {
      errorMessageMode: mode,
    },
  );
}

/**
 * 删除场地底图
 */
export function deleteSiteMap(id: string, mode: ErrorMessageMode = 'modal') {
  return request.delete({ url: Api.SiteMapDelete + '?id=' + id }, { errorMessageMode: mode });
}

/**
 * 启用场地底图
 */
export function enableSiteMap(id: string, mode: ErrorMessageMode = 'modal') {
  return request.post(
    {
      url: Api.SiteMapEnable,
      params: { id, status: '0' }, // 假设0表示启用
    },
    {
      errorMessageMode: mode,
    },
  );
}

/**
 * 禁用场地底图
 */
export function disableSiteMap(id: string, mode: ErrorMessageMode = 'modal') {
  return request.post(
    {
      url: Api.SiteMapDisable,
      params: { id, status: '1' }, // 假设1表示禁用
    },
    {
      errorMessageMode: mode,
    },
  );
}
