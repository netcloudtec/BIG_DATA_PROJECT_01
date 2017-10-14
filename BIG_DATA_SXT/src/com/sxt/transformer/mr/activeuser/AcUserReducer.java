package com.sxt.transformer.mr.activeuser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;

import com.sxt.common.KpiType;
import com.sxt.transformer.model.dim.StatsUserDimension;
import com.sxt.transformer.model.value.map.TimeOutputValue;
import com.sxt.transformer.model.value.reduce.MapWritableValue;

public class AcUserReducer
		extends
		Reducer<StatsUserDimension, TimeOutputValue, StatsUserDimension, MapWritableValue> {

	MapWritableValue mapWritableValue = new MapWritableValue();
	Set set = new HashSet();
	
	/**
	 * 相同维度组合  一组数据
	 * 根据用户id 去重统计   Set
	 */
	@Override
	protected void reduce(StatsUserDimension statsUserDimension, Iterable<TimeOutputValue> iterable, Context context)
			throws IOException, InterruptedException {
		this.set.clear();
		
		for (TimeOutputValue value : iterable) {
			this.set.add(value.getId());
		}
		
		// 去重之后  活跃用户数
		int acUserCount = this.set.size();
		MapWritable map = new MapWritable();
		map.put(new IntWritable(-1), new IntWritable(acUserCount));
		
		mapWritableValue.setValue(map);
		
		KpiType kpitype = KpiType.valueOfName(statsUserDimension.getStatsCommon().getKpi().getKpiName());
		mapWritableValue.setKpi(kpitype);
		
		context.write(statsUserDimension, mapWritableValue);
	}

}
