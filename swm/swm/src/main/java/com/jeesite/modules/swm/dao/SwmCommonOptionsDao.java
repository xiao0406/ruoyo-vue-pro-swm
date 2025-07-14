package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通用选项数据DAO接口
 * 
 * @author zwf
 * @version 2025-06-21
 */
@MyBatisDao
public interface SwmCommonOptionsDao {

        /**
         * 获取单位选项
         */
        List<Map<String, Object>> getCompanyOptions();

        /**
         * 获取车间选项
         */
        List<Map<String, Object>> getDepartmentOptions();

        /**
         * 获取产线选项
         */
        List<Map<String, Object>> getProdLineOptions();

        /**
         * 获取班组选项
         */
        List<Map<String, Object>> getWorkGroupOptions();

        /**
         * 获取工种选项
         */
        List<Map<String, Object>> getWorkTypeOptions();

        // 名称到编码的映射方法

        /**
         * 根据单位名称获取编码
         * 
         * @param companyName 单位名称
         * @return 单位编码
         */
        String getCompanyCodeByName(@Param("companyName") String companyName);

        /**
         * 根据车间名称获取编码（在指定单位下查找）
         * 
         * @param companyId      单位编码
         * @param departmentName 车间名称
         * @return 车间编码
         */
        String getDepartmentCodeByName(@Param("companyId") String companyId,
                        @Param("departmentName") String departmentName);

        /**
         * 根据产线名称获取编码（在指定车间下查找）
         * 
         * @param departmentId 车间编码
         * @param prodLineName 产线名称
         * @return 产线编码
         */
        String getProdLineCodeByName(@Param("departmentId") String departmentId,
                        @Param("prodLineName") String prodLineName);

        /**
         * 根据班组名称获取编码（在指定产线下查找）
         * 
         * @param prodLineId 产线编码
         * @param teamName   班组名称
         * @return 班组编码
         */
        String getTeamCodeByName(@Param("prodLineId") String prodLineId, @Param("teamName") String teamName);

        /**
         * 根据工种名称获取编码(工种的编码和名称相同)
         * 
         * @param workTypeName 工种名称
         * @return 工种编码
         */
        String getWorkTypeCodeByName(@Param("workTypeName") String workTypeName);

        /**
         * 根据人员类型名称获取编码
         * 
         * @param personTypeName 人员类型名称
         * @return 人员类型编码
         */
        String getPersonTypeCodeByName(@Param("personTypeName") String personTypeName);

        // 层级关系验证方法

        /**
         * 验证车间是否属于指定单位
         * 
         * @param companyCode    单位编码
         * @param departmentCode 车间编码
         * @return 是否属于
         */
        boolean validateDepartmentBelongsToCompany(@Param("companyCode") String companyCode,
                        @Param("departmentCode") String departmentCode);

        /**
         * 验证产线是否属于指定车间
         * 
         * @param departmentCode 车间编码
         * @param prodLineCode   产线编码
         * @return 是否属于
         */
        boolean validateProdLineBelongsToDepartment(@Param("departmentCode") String departmentCode,
                        @Param("prodLineCode") String prodLineCode);

        /**
         * 验证班组是否属于指定产线
         * 
         * @param prodLineCode 产线编码
         * @param teamCode     班组编码
         * @return 是否属于
         */
        boolean validateTeamBelongsToProdLine(@Param("prodLineCode") String prodLineCode,
                        @Param("teamCode") String teamCode);
}