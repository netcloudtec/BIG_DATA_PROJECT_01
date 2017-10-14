package com.sxt.transformer.mr.activeuser;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;

import com.sxt.common.DateEnum;
import com.sxt.common.EventLogConstants;
import com.sxt.common.KpiType;
import com.sxt.transformer.model.dim.StatsCommonDimension;
import com.sxt.transformer.model.dim.StatsUserDimension;
import com.sxt.transformer.model.dim.base.BrowserDimension;
import com.sxt.transformer.model.dim.base.DateDimension;
import com.sxt.transformer.model.dim.base.KpiDimension;
import com.sxt.transformer.model.dim.base.PlatformDimension;
import com.sxt.transformer.model.value.map.TimeOutputValue;

public class AcUserMapper extends
		TableMapper<StatsUserDimension, TimeOutputValue> {
	
	byte[] family = EventLogConstants.EVENT_LOGS_FAMILY_NAME.getBytes();
	TimeOutputValue outputVal = new TimeOutputValue();
	// 用户基本信息功能模块 活跃用户统计 kpi
	KpiDimension userKpiDim = new KpiDimension(KpiType.ACTIVE_USER.name);
	// 浏览器分析功能模块  活跃用户统计 kpi
	KpiDimension browUserKpiDim = new KpiDimension(KpiType.BROWSER_ACTIVE_USER.name); 
	
	StatsUserDimension statsUserDim = new StatsUserDimension();

	/**
	 * 读取hbase数据
	 * 每次调用map方法  处理hbase当中一行数据
	 */
	@Override
	protected void map(ImmutableBytesWritable key, Result value, Context context)
			throws IOException, InterruptedException {
		
		String uuid = new String(CellUtil.cloneValue(value.getColumnLatestCell(family, EventLogConstants.LOG_COLUMN_NAME_UUID.getBytes())));
		String pl = new String(CellUtil.cloneValue(value.getColumnLatestCell(family, EventLogConstants.LOG_COLUMN_NAME_PLATFORM.getBytes())));
		String stime = new String(CellUtil.cloneValue(value.getColumnLatestCell(family, EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME.getBytes())));
		
		long time = Long.parseLong(stime);
		
		// 初始化输出value
		outputVal.setId(uuid);
		outputVal.setTime(time);
		
		
		// 构建各个维度对象
		
		// 构建时间维度
		DateDimension dateDimension = DateDimension.buildDate(time, DateEnum.DAY);
		
		// 构建平台维度
		List<PlatformDimension> platformDimensions = PlatformDimension.buildList(pl);
		
		// 构建浏览器维度
		String brow = new String(CellUtil.cloneValue(value.getColumnLatestCell(family, EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME.getBytes())));
		String brow_v = new String(CellUtil.cloneValue(value.getColumnLatestCell(family, EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION.getBytes())));
		List<BrowserDimension> brows = BrowserDimension.buildList(brow, brow_v);
		
		// 用户基本信息kpi 输出
		BrowserDimension defultBrowDim = new BrowserDimension("", "");
		for (PlatformDimension platform : platformDimensions) {
			StatsCommonDimension commDim = statsUserDim.getStatsCommon();
			commDim.setPlatform(platform);
			commDim.setDate(dateDimension);
			commDim.setKpi(userKpiDim);
			
			statsUserDim.setBrowser(defultBrowDim);
			
			context.write(statsUserDim, outputVal);
		}
		
		
		// 浏览器功能模块Kpi  输出
		for (PlatformDimension platform : platformDimensions) {
			for (BrowserDimension browserDimension : brows) {
				StatsCommonDimension commDim = statsUserDim.getStatsCommon();
				commDim.setPlatform(platform);
				commDim.setDate(dateDimension);
				commDim.setKpi(browUserKpiDim);
				
				statsUserDim.setBrowser(browserDimension);
				
				context.write(statsUserDim, outputVal);
			}
		}
	}
}