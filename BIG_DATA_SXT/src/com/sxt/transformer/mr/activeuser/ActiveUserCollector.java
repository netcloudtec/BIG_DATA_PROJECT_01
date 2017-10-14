package com.sxt.transformer.mr.activeuser;

import java.io.IOException;
import java.sql.Date;
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
import com.sxt.util.TimeUtil;

public class ActiveUserCollector implements IOutputCollector {

	@Override
	public void collect(Configuration conf, BaseDimension key,
			BaseStatsValueWritable value, PreparedStatement pstmt,
			IDimensionConverter converter) throws SQLException, IOException {

		StatsUserDimension userDim = (StatsUserDimension)key;
		MapWritableValue val = (MapWritableValue)value;
		
		IntWritable active_user = (IntWritable) val.getValue().get(new IntWritable(-1));
		
		pstmt.setInt(1, converter.getDimensionIdByValue(userDim.getStatsCommon().getPlatform()));
		pstmt.setInt(2, converter.getDimensionIdByValue(userDim.getStatsCommon().getDate()));
		pstmt.setInt(3, active_user.get());
		pstmt.setDate(4, new Date(Long.valueOf(TimeUtil.parseString2Long(conf.get(GlobalConstants.RUNNING_DATE_PARAMES)))));
		pstmt.setInt(5, active_user.get());
		
		pstmt.addBatch();
	}
}