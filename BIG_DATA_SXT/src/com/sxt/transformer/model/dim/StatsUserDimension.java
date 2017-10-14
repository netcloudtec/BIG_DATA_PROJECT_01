package com.sxt.transformer.model.dim;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.sxt.transformer.model.dim.base.BaseDimension;
import com.sxt.transformer.model.dim.base.BrowserDimension;

/**
 * 进行用户分析(用户基本分析和浏览器分析)定义的组合维度
 * 
 * @author root
 *
 */
public class StatsUserDimension extends StatsDimension {
	
    private StatsCommonDimension statsCommon = new StatsCommonDimension();//公用的维度组合（时间、平台、项目功能模块）
    private BrowserDimension browser = new BrowserDimension();//除了公用的维度组合以外，还可以添加其他的维度信息（如：浏览器维度）

    /**
     * close一个实例对象
     * 
     * @param dimension
     * @return
     */
    public static StatsUserDimension clone(StatsUserDimension dimension) {
        BrowserDimension browser = new BrowserDimension(dimension.browser.getBrowserName(), dimension.browser.getBrowserVersion());
        StatsCommonDimension statsCommon = StatsCommonDimension.clone(dimension.statsCommon);
        return new StatsUserDimension(statsCommon, browser);
    }

    public StatsUserDimension() {
        super();
    }

    public StatsUserDimension(StatsCommonDimension statsCommon, BrowserDimension browser) {
        super();
        this.statsCommon = statsCommon;
        this.browser = browser;
    }

    public StatsCommonDimension getStatsCommon() {
        return statsCommon;
    }

    public void setStatsCommon(StatsCommonDimension statsCommon) {
        this.statsCommon = statsCommon;
    }

    public BrowserDimension getBrowser() {
        return browser;
    }

    public void setBrowser(BrowserDimension browser) {
        this.browser = browser;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        this.statsCommon.write(out);
        this.browser.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.statsCommon.readFields(in);
        this.browser.readFields(in);
    }

    @Override
    public int compareTo(BaseDimension o) {
        if (this == o) {
            return 0;
        }

        StatsUserDimension other = (StatsUserDimension) o;
        int tmp = this.statsCommon.compareTo(other.statsCommon);
        if (tmp != 0) {
            return tmp;
        }
        tmp = this.browser.compareTo(other.browser);
        return tmp;
    }

}
