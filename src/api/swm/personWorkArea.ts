import request from '@/config/axios';

const apiPrefix = '/swm/swmPersonWorkArea';

export interface PersonWorkAreaModel {
  id?: string;
  identityCard?: string;
  personName?: string;
  areaId?: string;
  areaName?: string;
  identityCards?: string;
  areaIds?: string;
  company?: string;
  department?: string;
  prodLine?: string;
  team?: string;
  jobType?: string;
  remarks?: string;
}

export interface PersonWorkAreaParams {
  personName?: string;
  identityCard?: string;
  company?: string;
  department?: string;
  prodLine?: string;
  team?: string;
  areaName?: string;
  pageNo?: number;
  pageSize?: number;
}

export const getPersonWorkAreaList = (params?: PersonWorkAreaParams) =>
  request.post({ url: `${apiPrefix}/listData`, params });

export const savePersonWorkArea = (params: PersonWorkAreaModel) =>
  request.post({ url: `${apiPrefix}/save`, params });

export const deletePersonWorkArea = (id: string) =>
  request.post({ url: `${apiPrefix}/delete`, params: { id } });
