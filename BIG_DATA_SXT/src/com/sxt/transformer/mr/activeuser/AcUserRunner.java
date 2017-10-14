package com.sxt.transformer.mr.activeuser;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.sxt.common.EventLogConstants;
import com.sxt.common.GlobalConstants;
import com.sxt.transformer.model.dim.StatsUserDimension;
import com.sxt.transformer.model.value.map.TimeOutputValue;
import com.sxt.transformer.model.value.reduce.MapWritableValue;
import com.sxt.transformer.mr.TransformerOutputFormat;
import com.sxt.util.TimeUtil;

/**
 * 计算活跃用户入口类
 * 1、分析需求（a、功能模块；b、维度组合）
 * 2、runner a、scan读取hbase数据规则；b、mapper输出类型 key类型；c、reducer输出类型
 * 3、mapper实现   a、读取字段数据；b、初始化维度对象；c、根据需求将维度组合；d、输出
 * 4、reducer实现   set去重统计
 * 5、将不同功能模块的insert Sql定义到query-mapping.xml
 * 6、实现不同kpi的collecter
 * 7、修改output-collector.xml 将实现好的collecter类名 定义好   （反射）
 * 
 * 修改mysql连接url：
 * 1、transformer-env.xml
 * 2、DimensionConverterImpl.java
 * 
 * @author root
 *
 */
public class AcUserRunner implements Tool {
    private static final Logger logger = Logger.getLogger(AcUserRunner.class);
    private Configuration conf = new Configuration();

    /**
     * 入口main方法
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            ToolRunner.run(new Configuration(), new AcUserRunner(), args);
        } catch (Exception e) {
            logger.error("运行计算新用户的job出现异常", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setConf(Configuration conf) {
    	// 用户自定的配置文件
        conf.addResource("output-collector.xml");
        conf.addResource("query-mapping.xml");
        conf.addResource("transformer-env.xml");
        conf.set("fs.defaultFS", "hdfs://node1:8020");
//    	conf.set("yarn.resourcemanager.hostname", "node3");
    	conf.set("hbase.zookeeper.quorum", "node4");
        this.conf = HBaseConfiguration.create(conf);
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();
        // 处理参数 时间  确定分析哪一天数据
        this.processArgs(conf, args);

        Job job = Job.getInstance(conf, "new_install_user");

        job.setJarByClass(AcUserRunner.class);
        // 本地运行
        TableMapReduceUtil.initTableMapperJob(
        		initScans(job), 
        		AcUserMapper.class, 
        		StatsUserDimension.class, 
        		TimeOutputValue.class, 
        		job, 
        		false);
        // 集群运行：本地提交和打包(jar)提交
//         TableMapReduceUtil.initTableMapperJob(initScans(job), NewInstallUserMapper.class, StatsUserDimension.class, TimeOutputValue.class, job);
        job.setReducerClass(AcUserReducer.class);
        job.setOutputKeyClass(StatsUserDimension.class);
        job.setOutputValueClass(MapWritableValue.class);

        // 指定 自定义输出规则
        job.setOutputFormatClass(TransformerOutputFormat.class);
        
        if (job.waitForCompletion(true)) {
            return 0;
        } else {
            return -1;
        }
    }


    
    /**
     * 处理参数
     * 
     * @param conf
     * @param args
     */
    private void processArgs(Configuration conf, String[] args) {
        String date = null;
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i])) {
                if (i + 1 < args.length) {
                    date = args[++i];
                    break;
                }
            }
        }

        // 要求date格式为: yyyy-MM-dd
        if (StringUtils.isBlank(date) || !TimeUtil.isValidateRunningDate(date)) {
            // date是一个无效时间数据
            date = TimeUtil.getYesterday(); // 默认时间是昨天
        }
        System.out.println("----------------------" + date);
        conf.set(GlobalConstants.RUNNING_DATE_PARAMES, date);
    }

    /**
     * 初始化scan集合
     * 
     * @param job
     * @return
     */
    private List<Scan> initScans(Job job) {
        // 时间戳+....
        Configuration conf = job.getConfiguration();
        // 获取运行时间: yyyy-MM-dd
        String date = conf.get(GlobalConstants.RUNNING_DATE_PARAMES);
        long startDate = TimeUtil.parseString2Long(date);
        long endDate = startDate + GlobalConstants.DAY_OF_MILLISECONDS;

        Scan scan = new Scan();
        // 定义hbase扫描的开始rowkey和结束rowkey
        scan.setStartRow(Bytes.toBytes("" + startDate));
        scan.setStopRow(Bytes.toBytes("" + endDate));
        
        FilterList filterList = new FilterList();
        // 定义mapper中需要获取的列名
        String[] columns = new String[] {
        			EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME, 
        			EventLogConstants.LOG_COLUMN_NAME_UUID, 
        			EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME, 
        			EventLogConstants.LOG_COLUMN_NAME_PLATFORM, 
        			EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME, 
        			EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION };
        filterList.addFilter(this.getColumnFilter(columns));

        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, Bytes.toBytes(EventLogConstants.HBASE_NAME_EVENT_LOGS));
        scan.setFilter(filterList);
        return Lists.newArrayList(scan);
    }

    /**
     * 获取这个列名过滤的column
     * 
     * @param columns
     * @return
     */
    private Filter getColumnFilter(String[] columns) {
        int length = columns.length;
        byte[][] filter = new byte[length][];
        for (int i = 0; i < length; i++) {
            filter[i] = Bytes.toBytes(columns[i]);
        }
        return new MultipleColumnPrefixFilter(filter);
    }
}
