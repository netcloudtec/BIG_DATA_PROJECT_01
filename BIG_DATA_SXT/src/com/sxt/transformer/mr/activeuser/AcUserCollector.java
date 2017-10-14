package com.sxt.transformer.mr.activeuser;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;

import com.sxt.common.GlobalConstants;
import com.sxt.transformer.model.dim.StatsUserDimension;
import com.sxt.transformer.model.dim.base.BaseDimension;
import com.sxt.transformer.model.value.BaseStatsValueWritable;
import com.sxt.transformer.model.value.reduce.MapWritableValue;
import com.sxt.transformer.mr.IOutputCollector;
import com.sxt.transformer.service.IDimensionConverter;

public class AcUserCollector implements IOutputCollector {

	/**
	 * key value： Reduce输出数据
	 * pstmt：sql
	 * converter：getDimensionIdByValue（维度对象） 返回对应维度的维度id 
	 */
	@Override
	public void collect(Configuration conf, BaseDimension key, BaseStatsValueWritable value, 
			PreparedStatement pstmt, IDimensionConverter converter) throws SQLException, IOException {

		StatsUserDimension userDimension = (StatsUserDimension) key;
		MapWritableValue mapWritableValue = (MapWritableValue) value;
		
		// 统计结果 活跃用户数
		IntWritable acUserCount = (IntWritable) mapWritableValue.getValue().get(new IntWritable(-1));
		
		int i = 1;
		pstmt.setInt(i++, converter.getDimensionIdByValue(userDimension.getStatsCommon().getPlatform()));
		pstmt.setInt(i++, converter.getDimensionIdByValue(userDimension.getStatsCommon().getDate()));
		pstmt.setInt(i++, acUserCount.get());
		pstmt.setString(i++, conf.get(GlobalConstants.RUNNING_DATE_PARAMES));
		pstmt.setInt(i++, acUserCount.get());

		pstmt.addBatch();
	}
}