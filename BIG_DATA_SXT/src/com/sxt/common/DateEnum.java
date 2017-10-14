package com.sxt.common;

/**
 * 日期类型枚举类
 * 
 * @author root
 *
 */
public enum DateEnum {
	YEAR("year"), SEASON("season"), MONTH("month"), WEEK("week"), DAY("day"), HOUR("hour");
	// 定义变量
	public final String name;//注意：这里的name就是枚举定义方法中的参数

	// 创建构造方法
	private DateEnum(String name) {
		this.name = name;
	}

	/**
	 * 根据属性name的值获取对应的type对象
	 * type也就是枚举的方法名 即：YEAR、SEASON、MONTH等值
	 * @param name
	 * @return
	 */
	public static DateEnum valueOfName(String name) {
		for (DateEnum type : DateEnum.values()) {
			if (type.name.equals(name)) {
				return type;
			}
		}
		return null;
	}

}
