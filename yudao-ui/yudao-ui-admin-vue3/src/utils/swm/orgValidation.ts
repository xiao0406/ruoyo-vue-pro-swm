/**
 * 组织架构验证工具函数
 * @author Shawn
 * @date 2025-01-15
 */
import { fetchOrgTreeData } from '@/api/swm/organizationTree';
import { getWorkTypeList } from '@/api/swm/person';

// 组织架构验证树类型
export interface OrgValidationTree {
  [company: string]: {
    [department: string]: {
      [prodLine: string]: string[];
    };
  };
}

// 工种选项类型
export interface WorkTypeOption {
  workType: string;
  workTypeCode: string;
}

// 验证结果类型
export interface ValidationResult {
  isValid: boolean;
  errors: string[];
}

// 导入行数据类型
export interface ImportRowData {
  name?: string;
  personType?: string;
  gender?: string;
  identityCard?: string;
  phoneNumber?: string;
  isExternalPersonnel?: string;
  company?: string;
  department?: string;
  prodLine?: string;
  team?: string;
  jobType?: string;
  remarks?: string;
  [key: string]: any;
}

/**
 * 构建组织架构验证树
 */
export async function buildOrgValidationTree(): Promise<OrgValidationTree> {
  const tree: OrgValidationTree = {};

  try {
    // 获取所有单位
    const companies = await fetchOrgTreeData('root');

    for (const company of companies) {
      const companyTitle = company.title;
      tree[companyTitle] = {};

      // 获取单位下的车间
      const departments = await fetchOrgTreeData('office', company.value);

      for (const dept of departments) {
        const deptTitle = dept.title;
        tree[companyTitle][deptTitle] = {};

        // 获取车间下的产线
        const prodLines = await fetchOrgTreeData('workshop', dept.value);

        for (const line of prodLines) {
          const lineTitle = line.title;
          tree[companyTitle][deptTitle][lineTitle] = [];

          // 获取产线下的班组
          const teams = await fetchOrgTreeData('prodLine', line.value);
          tree[companyTitle][deptTitle][lineTitle] = teams.map((t) => t.title);
        }
      }
    }

    return tree;
  } catch (error) {
    console.error('构建组织架构验证树失败:', error);
    return {};
  }
}

/**
 * 获取工种选项列表
 */
export async function getWorkTypeOptions(): Promise<WorkTypeOption[]> {
  try {
    const result = await getWorkTypeList();
    if (result && result.success && result.list && Array.isArray(result.list)) {
      return result.list.map((item) => ({
        workType: item.workType,
        workTypeCode: item.workTypeCode,
      }));
    }
    return [];
  } catch (error) {
    console.error('获取工种选项失败:', error);
    return [];
  }
}

/**
 * 验证级联关系
 */
export function validateCascade(
  company: string,
  department: string,
  prodLine: string,
  team: string,
  validationTree: OrgValidationTree,
): ValidationResult {
  const errors: string[] = [];

  if (!company) {
    errors.push('所属单位不能为空');
    return { isValid: false, errors };
  }

  if (!validationTree[company]) {
    errors.push(`单位"${company}"不存在`);
    return { isValid: false, errors };
  }

  if (!department) {
    errors.push('所属车间不能为空');
    return { isValid: false, errors };
  }

  if (!validationTree[company][department]) {
    errors.push(`车间"${department}"不属于单位"${company}"`);
    return { isValid: false, errors };
  }

  if (!prodLine) {
    errors.push('所属产线不能为空');
    return { isValid: false, errors };
  }

  if (!validationTree[company][department][prodLine]) {
    errors.push(`产线"${prodLine}"不属于车间"${department}"`);
    return { isValid: false, errors };
  }

  if (!team) {
    errors.push('所属班组不能为空');
    return { isValid: false, errors };
  }

  if (!validationTree[company][department][prodLine].includes(team)) {
    errors.push(`班组"${team}"不属于产线"${prodLine}"`);
    return { isValid: false, errors };
  }

  return { isValid: true, errors: [] };
}

/**
 * 验证工种是否存在
 */
export function validateJobType(
  jobType: string,
  workTypeOptions: WorkTypeOption[],
): ValidationResult {
  if (!jobType) {
    return { isValid: false, errors: ['所属工种不能为空'] };
  }

  const exists = workTypeOptions.some(
    (option) => option.workType === jobType || option.workTypeCode === jobType,
  );

  if (!exists) {
    return { isValid: false, errors: [`工种"${jobType}"不存在`] };
  }

  return { isValid: true, errors: [] };
}

/**
 * 处理"是否场内员工"字段值
 */
export function mapExternalPersonnelValue(value: string | null | undefined): string {
  if (!value) return '0'; // 默认为非厂内员工

  const normalizedValue = String(value).trim().toLowerCase();

  // 支持多种格式
  if (normalizedValue === '是' || normalizedValue === '1' || normalizedValue === 'true') {
    return '1'; // 厂内员工
  }

  return '0'; // 非厂内员工
}

/**
 * 验证单行导入数据
 */
export async function validateImportRow(
  rowData: ImportRowData,
  rowIndex: number,
  validationTree: OrgValidationTree,
  workTypeOptions: WorkTypeOption[],
): Promise<{ rowIndex: number; errors: string[]; processedData: ImportRowData }> {
  const errors: string[] = [];
  const processedData = { ...rowData };

  // 基础字段验证
  if (!rowData.name || !rowData.name.trim()) {
    errors.push('姓名不能为空');
  }

  if (!rowData.identityCard || !rowData.identityCard.trim()) {
    errors.push('身份证号码不能为空');
  } else if (!/^\d{17}[\dXx]$/.test(rowData.identityCard.trim())) {
    errors.push('身份证号码格式不正确');
  }

  if (!rowData.phoneNumber || !rowData.phoneNumber.trim()) {
    errors.push('手机号码不能为空');
  } else if (!/^1[3-9]\d{9}$/.test(rowData.phoneNumber.trim())) {
    errors.push('手机号码格式不正确');
  }

  if (!rowData.gender || !rowData.gender.trim()) {
    errors.push('性别不能为空');
  } else if (!['男', '女'].includes(rowData.gender.trim())) {
    errors.push('性别只能填写"男"或"女"');
  }

  if (!rowData.personType) {
    errors.push('人员类型不能为空');
  } else {
    const personType = String(rowData.personType).trim();
    if (!['0', '1', '工人', '管理者'].includes(personType)) {
      errors.push('人员类型只能填写"0"(工人)或"1"(管理者)');
    } else {
      // 标准化人员类型值
      if (personType === '工人') processedData.personType = '0';
      else if (personType === '管理者') processedData.personType = '1';
    }
  }

  // 处理"是否场内员工"字段
  const isInternal = mapExternalPersonnelValue(rowData.isExternalPersonnel);
  processedData.isExternalPersonnel = isInternal;

  if (isInternal === '1') {
    // 厂内员工需要验证组织架构字段
    const cascadeResult = validateCascade(
      rowData.company || '',
      rowData.department || '',
      rowData.prodLine || '',
      rowData.team || '',
      validationTree,
    );

    if (!cascadeResult.isValid) {
      errors.push(...cascadeResult.errors);
    }

    // 验证工种
    const jobTypeResult = validateJobType(rowData.jobType || '', workTypeOptions);
    if (!jobTypeResult.isValid) {
      errors.push(...jobTypeResult.errors);
    }
  } else {
    // 非厂内员工，清空组织架构字段
    processedData.company = null;
    processedData.department = null;
    processedData.prodLine = null;
    processedData.team = null;
    processedData.jobType = null;
  }

  return {
    rowIndex,
    errors,
    processedData,
  };
}

/**
 * 批量验证导入数据
 */
export async function validateImportData(excelData: ImportRowData[]): Promise<{
  isValid: boolean;
  results: Array<{ rowIndex: number; errors: string[]; processedData: ImportRowData }>;
  summary: { total: number; success: number; failed: number };
}> {
  const results: Array<{ rowIndex: number; errors: string[]; processedData: ImportRowData }> = [];

  // 构建验证数据
  const validationTree = await buildOrgValidationTree();
  const workTypeOptions = await getWorkTypeOptions();

  // 逐行验证
  for (let i = 0; i < excelData.length; i++) {
    const result = await validateImportRow(
      excelData[i],
      i + 1, // 行号从1开始
      validationTree,
      workTypeOptions,
    );
    results.push(result);
  }

  const summary = {
    total: results.length,
    success: results.filter((r) => r.errors.length === 0).length,
    failed: results.filter((r) => r.errors.length > 0).length,
  };

  return {
    isValid: summary.failed === 0,
    results,
    summary,
  };
}

/**
 * 生成组织架构选项数据（用于Excel模板）
 */
export async function generateOrgOptionsForTemplate(): Promise<{
  companies: string[];
  departments: Array<{ company: string; department: string }>;
  prodLines: Array<{ company: string; department: string; prodLine: string }>;
  teams: Array<{ company: string; department: string; prodLine: string; team: string }>;
  workTypes: Array<{ workType: string; workTypeCode: string }>;
}> {
  const validationTree = await buildOrgValidationTree();
  const workTypeOptions = await getWorkTypeOptions();

  const companies: string[] = [];
  const departments: Array<{ company: string; department: string }> = [];
  const prodLines: Array<{ company: string; department: string; prodLine: string }> = [];
  const teams: Array<{ company: string; department: string; prodLine: string; team: string }> = [];

  // 遍历验证树生成选项数据
  for (const [company, deptMap] of Object.entries(validationTree)) {
    companies.push(company);

    for (const [department, lineMap] of Object.entries(deptMap)) {
      departments.push({ company, department });

      for (const [prodLine, teamList] of Object.entries(lineMap)) {
        prodLines.push({ company, department, prodLine });

        for (const team of teamList) {
          teams.push({ company, department, prodLine, team });
        }
      }
    }
  }

  return {
    companies,
    departments,
    prodLines,
    teams,
    workTypes: workTypeOptions,
  };
}
