package com.sxt.transformer.mr.activeuser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.sxt.common.KpiType;
import com.sxt.transformer.model.dim.StatsUserDimension;
import com.sxt.transformer.model.value.map.TimeOutputValue;
import com.sxt.transformer.model.value.reduce.MapWritableValue;

public class ActiveUserReducer
		extends
		Reducer<StatsUserDimension, TimeOutputValue, StatsUserDimension, MapWritableValue> {

	private Set<String> s = new HashSet<String>();
	
	@Override
	/**
	 * 相同维度组合 一组数据
	 * 根据uud去重统计
	 */
	protected void reduce(StatsUserDimension dimension, Iterable<TimeOutputValue> iterable, Context context)
			throws IOException, InterruptedException {
		
		s.clear();
		
		for (TimeOutputValue i : iterable) {
			s.add(i.getId());
		}
	
		// 活跃用户数
		int totalActiverUser = s.size();
		
		MapWritableValue value = new MapWritableValue();
		MapWritable map = new MapWritable();
		map.put(new IntWritable(-1), new IntWritable(totalActiverUser));
		
		value.setValue(map);
		value.setKpi(KpiType.valueOfName(dimension.getStatsCommon().getKpi().getKpiName()));
		
		context.write(dimension, value);
	}
}
