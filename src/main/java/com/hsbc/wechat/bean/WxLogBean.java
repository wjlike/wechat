package com.hsbc.wechat.bean;

import lombok.Data;

@Data
public class WxLogBean {

	//日志id
	private String id;
	//返回的消息码（0-成功，!0失败）
	private String errCode;
	//错误信息或成功信息
	private String errMsg;
	//业务开始时间
	private long startTimeMillis;
	//业务完成时间
	private long endTimeMillis;
	//任务用时
	private long costTimeMillis;
	//处理的数据量，例如1000
	private long dataRows;
	//本次请求获取消息记录开始的seq值
	private long startSeq;
	//本次请求获取消息记录的最大seq值
	private long maxSeq;
	//返回的文件id
	private String sdkFileId;
	//返回的文件名（聊天记录对应的文件名）
	private String fileName;
	//日志记录时间
	private String makeTime;
	//备注
	private String remark;

	//获取表格格式的表头，字段之间通过\t分隔
	public String getTableHead() {
		String split = "\t";
		StringBuilder sb = new StringBuilder();
		sb.append("id");
		sb.append(split);
		sb.append("errCode");
		sb.append(split);
		sb.append("errMsg");
		sb.append(split);
		sb.append("startTimeMillis");
		sb.append(split);
		sb.append("endTimeMillis");
		sb.append(split);
		sb.append("costTimeMillis");
		sb.append(split);
		sb.append("dataRows");
		sb.append(split);
		sb.append("startSeq");
		sb.append(split);
		sb.append("maxSeq");
		sb.append(split);
		sb.append("sdkFileId");
		sb.append(split);
		sb.append("fileName");
		sb.append(split);
		sb.append("makeTime");
		sb.append(split);
		sb.append("remark");
		return sb.toString();
	}

	//获取表格形式的内容，拼接成表格格式的信息，字段间通过\t分隔
	public String getTableContent() {
		String split = "\t";
		StringBuilder sb = new StringBuilder();
		sb.append(getId());
		sb.append(split);
		sb.append(getErrCode());
		sb.append(split);
		sb.append(getErrMsg());
		sb.append(split);
		sb.append(getStartTimeMillis());
		sb.append(split);
		sb.append(getEndTimeMillis());
		sb.append(split);
		sb.append(getCostTimeMillis());
		sb.append(split);
		sb.append(getDataRows());
		sb.append(split);
		sb.append(getStartSeq());
		sb.append(split);
		sb.append(getMaxSeq());
		sb.append(split);
		sb.append(getSdkFileId());
		sb.append(split);
		sb.append(getFileName());
		sb.append(split);
		sb.append(getMakeTime());
		sb.append(split);
		sb.append(getRemark());
		return sb.toString();
	}

}
