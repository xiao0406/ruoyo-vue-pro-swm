/**
 * 区域管理DAO接口
 * @author Shawn
 * @version 2025-06-22
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.entity.SwmArea;
import com.jeesite.modules.entity.AiDto;

import java.util.List;

/**
 * 区域管理DAO接口
 */
@MyBatisDao
public interface SwmAreaDao extends CrudDao<SwmArea> {

    List<AiDto.Trajectory> findAddressList();

}