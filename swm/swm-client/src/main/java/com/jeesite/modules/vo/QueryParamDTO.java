package com.jeesite.modules.vo;

import lombok.Data;

/**
 * tdengin 查询参数
 * @author: cjie
 * @date: 2022/7/19
 */
@Data
public class QueryParamDTO {

	/**
	 * 设备编号
	 */
	private String deviceCode;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 列名，column 值为   c1,c2  其中c1,c2为表中对应的需要查询的列名
     */
    private String column;

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;
}
