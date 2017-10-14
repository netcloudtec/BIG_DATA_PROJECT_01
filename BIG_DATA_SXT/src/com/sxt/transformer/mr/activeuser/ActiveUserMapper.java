package com.sxt.transformer.mr.activeuser;

import java.io.IOException;
import java.util.List;

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

public class ActiveUserMapper extends
		TableMapper<StatsUserDimension, TimeOutputValue> {
	
	private KpiDimension userKpiDimension = new KpiDimension(KpiType.ACTIVE_USER.name);
	private KpiDimension browserUserKpiDimension = new KpiDimension(KpiType.BROWSER_ACTIVE_USER.name);

	private byte[] cf = EventLogConstants.EVENT_LOGS_FAMILY_NAME.getBytes();
	
	/**
	 * 1、读取hbase数据
	 * 2、构建维度对象
	 * 3、根据需求 将维度对象组合 作为key进行输出
	 */
	@Override
	protected void map(ImmutableBytesWritable key, Result value, Context context)
			throws IOException, InterruptedException {

		String uud = new String(value.getValue(cf, EventLogConstants.LOG_COLUMN_NAME_UUID.getBytes()));
		String pl = new String(value.getValue(cf, EventLogConstants.LOG_COLUMN_NAME_PLATFORM.getBytes()));
		String dateStr = new String(value.getValue(cf, EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME.getBytes()));

		long dateL = Long.parseLong(dateStr);
		
		TimeOutputValue outputValue = new TimeOutputValue();
		outputValue.setId(uud);
		outputValue.setTime(dateL);
		
		StatsUserDimension userDim = new StatsUserDimension();
		
		// 时间维度构建好
		DateDimension dateDim = DateDimension.buildDate(dateL, DateEnum.DAY);
		
		// 构建平台维度
		List<PlatformDimension> platDims = PlatformDimension.buildList(pl);
		
		// 浏览器维度
		String browserName = new String(value.getValue(cf, EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME.getBytes()));
		String browserVersion = new String(value.getValue(cf, EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION.getBytes()));
		List<BrowserDimension> borwserDims = BrowserDimension.buildList(browserName, browserVersion);
		
		// 空的浏览器维度
		BrowserDimension defultBrowserDim = new BrowserDimension("", "");
		
		// 将维度组合 进行输出
		for (PlatformDimension plat : platDims) {
			// 用户基本信息功能模块
			userDim.setBrowser(defultBrowserDim);
			StatsCommonDimension commonDim = userDim.getStatsCommon();
			commonDim.setDate(dateDim);
			commonDim.setKpi(userKpiDimension);
			commonDim.setPlatform(plat);
			
			context.write(userDim, outputValue);
			
			for (BrowserDimension browser : borwserDims) {
				// 浏览器分析模块
				userDim.setBrowser(browser);
				commonDim.setKpi(browserUserKpiDimension);
				
				context.write(userDim, outputValue);
			}
		}
	}
}