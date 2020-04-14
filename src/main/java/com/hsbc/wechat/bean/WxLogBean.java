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
		sb.append("makSeq");
		sb.append(split);
		sb.append("sdkFileId");
		sb.append(split);
		sb.append("fileName");
		sb.append(split);
		sb.append("makeTime");
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
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public long getStartTimeMillis() {
		return startTimeMillis;
	}

	public void setStartTimeMillis(long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
	}

	public long getEndTimeMillis() {
		return endTimeMillis;
	}

	public void setEndTimeMillis(long endTimeMillis) {
		this.endTimeMillis = endTimeMillis;
	}

	public long getCostTimeMillis() {
		return costTimeMillis;
	}

	public void setCostTimeMillis(long costTimeMillis) {
		this.costTimeMillis = costTimeMillis;
	}

	public long getDataRows() {
		return dataRows;
	}

	public void setDataRows(long dataRows) {
		this.dataRows = dataRows;
	}

	public long getStartSeq() {
		return startSeq;
	}

	public void setStartSeq(long startSeq) {
		this.startSeq = startSeq;
	}

	public long getMaxSeq() {
		return maxSeq;
	}

	public void setMaxSeq(long maxSeq) {
		this.maxSeq = maxSeq;
	}

	public String getSdkFileId() {
		return sdkFileId;
	}

	public void setSdkFileId(String sdkFileId) {
		this.sdkFileId = sdkFileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMakeTime() {
		return makeTime;
	}

	public void setMakeTime(String makeTime) {
		this.makeTime = makeTime;
	}
}
