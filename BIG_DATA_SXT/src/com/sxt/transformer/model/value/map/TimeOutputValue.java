package com.sxt.transformer.model.value.map;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.sxt.common.KpiType;
import com.sxt.transformer.model.value.BaseStatsValueWritable;

public class TimeOutputValue extends BaseStatsValueWritable {
    private String id; // id  这里的id可以是用户id也可以是会员id
    private long time; // 时间戳

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(this.id);
        out.writeLong(this.time);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.id = in.readUTF();
        this.time = in.readLong();
    }

    @Override
    public KpiType getKpi() {
        // TODO Auto-generated method stub
        return null;
    }

}
