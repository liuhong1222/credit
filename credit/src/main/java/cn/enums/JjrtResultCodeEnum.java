package cn.enums;

/**
 * 飓金嵘通返回值结果
 * 
 */
public enum JjrtResultCodeEnum {

	J0000000("0000000" , "验证成功"),
	J0001001("0001001" , "HMAC验证成功, eID可用"),
	J0001002("0001002" , "HMAC验证成功, eID挂起"),
	J0001003("0001003" , "HMAC验证成功, eID过期"),
	J0001004("0001004" , "HMAC验证成功, eID已注销"),
	J0001005("0001005" , "HMAC验证成功, eID未开通"),
	J0002001("0002001" , "新用户编码"),
	J0101001("0101001" , "version为空"),
	J0101002("0101002" , "app_id为空"),
	J0101003("0101003" , "return_url为空"),
	J0101004("0101004" , "biz_type为空"),
	J0101005("0101005" , "biz_time为空"),
	J0101006("0101006" , "biz_sequence_id为空"),
	J0101007("0101007" , "security_factor为空"),
	J0101008("0101008" , "encrypt_factor为空"),
	J0101009("0101009" , "sign_factor为空"),
	J0101010("0101010" , "encrypt_type为空"),
	J0101011("0101011" , "sign_type为空"),
	J0101012("0101012" , "sign为空"),
	J0101013("0101013" , "security_type为空"),
	J0101014("0101014" , "user_id_info为空"),
	J0101015("0101015" , "name为空"),
	J0101016("0101016" , "idnum为空"),
	J0101017("0101017" , "idtype为空"),
	J0101018("0101018" , "carriered为空"),
	J0101019("0101019" , "eid_cert_id为空"),
	J0101020("0101020" , "eid_issuer为空"),
	J0101021("0101021" , "eid_issuer_sn为空"),
	J0101022("0101022" , "eid_sn为空"),
	J0101023("0101023" , "data_to_sign为空"),
	J0101024("0101024" , "eid_sign为空"),
	J0101025("0101025" , "eid_sign_algorithm为空"),
	J0101026("0101026" , "biz_data为空"),
	J01C1001("01C1001" , "人脸图片不存在"),
	J01C1002("01C1002" , "attach为空"),
	J01C1003("01C1003" , "手机号为空"),
	J01C1004("01C1004" , "银行卡号为空"),
	J01C1005("01C1005" , "视屏为空"),
	J01C1006("01C1006" , "唇语验证码为空"),
	J0102001("0102001" , "return_url格式错误"),
	J0102002("0102002" , "biz_time格式错误"),
	J0102003("0102003" , "biz_sequence_id格式错误"),
	J0102004("0102004" , "data_to_sign格式错误"),
	J0102005("0102005" , "security_factor格式错误"),
	J0102006("0102006" , "报文格式不正确"),
	J0102007("0102007" , "icarrier格式错误"),
	J0102008("0102008" , "attch格式错误(超长)"),
	J0102009("0102009" , "user_id_info格式错误"),
	J0102010("0102010" , "biz_data格式错误"),
	J0102011("0102011" , "idnum格式错误"),
	J01C2011("01C2011" , "name格式错误"),
	J01C2001("01C2001" , "手机号格式错误"),
	J01C2002("01C2002" , "银行卡号格式错误"),
	J0103001("0103001" , "version无效"),
	J0103002("0103002" , "biz_type无效"),
	J0103003("0103003" , "encrypt_type无效"),
	J0103004("0103004" , "sign_type无效"),
	J0103005("0103005" , "security_type无效"),
	J0103006("0103006" , "idtype无效"),
	J0103007("0103007" , "eid_sign_algorithm无效"),
	J0103008("0103008" , "encrypt_factor无效"),
	J0103009("0103009" , "sign_factor无效"),
	J0103010("0103010" , "extension无效"),
	J01C3001("01C3001" , "图片大小不符合要求"),
	J01C3002("01C3002" , "图片格式不符合要求"),
	J01C3003("01C3003" , "图片无效"),
	J01C3004("01C3004" , "idnum无效"),
	J01C3005("01C3005" , "手机号无效"),
	J01C3006("01C3006" , "银行卡号无效"),
	J01C3007("01C3007" , "视屏大小不符合要求"),
	J01C3008("01C3008" , "无效视屏"),
	J0201001("0201001" , "sign验证错误"),
	J0201002("0201002" , "biz_sequence_id重复"),
	J0201003("0201003" , "biz_time不在有效期范围内"),
	J0201004("0201004" , "data_to_sign重复"),
	J0201005("0201005" , "data_to_sign不在有效期范围内"),
	J0201006("0201006" , "app_id不可用"),
	J0201007("0201007" , "无权访问相关业务类型(biz_type)"),
	J0201008("0201008" , "重复请求(sign重复)"),
	J0201009("0201009" , "app_id未注册"),
	J0201010("0201010" , "app_id已暂停"),
	J0201011("0201011" , "app_id已停用"),
	J0201012("0201012" , "biz_type未注册"),
	J0201013("0201013" , "biz_type已暂停"),
	J0201014("0201014" , "biz_type已停用"),
	J0201015("0201015" , "请求地址无效(路径上app_id不匹配)"),
	J0201016("0201016" , "mac验证错误"),
	J0301000("0301000" , "eID证书已注销"),
	J0301001("0301001" , "eID证书已冻结"),
	J0301002("0301002" , "eID证书无效"),
	J0301005("0301005" , "eID载体已注销"),
	J0301006("0301006" , "eID载体已冻结"),
	J0301007("0301007" , "eID载体无效"),
	J03C1001("03C1001" , "银行卡状态错误"),
	J0302000("0302000" , "姓名身份证不匹配"),
	J0302001("0302001" , "eID证书已过期"),
	J0302002("0302002" , "eID签名验签失败"),
	J0302003("0302003" , "eID HMAC验签失败"),
	J03C2001("03C2001" , "银行卡验证不一致"),
	J03C2002("03C2002" , "验证失败"),
	J0401000("0401000" , "服务器异常"),
	J04C1000("04C1000" , "状态未知"),
	J04C1001("04C1001" , "账户余额不足"),
	J04C1002("04C1002" , "eid_cert_id为空键值对数量错误");
	
	public String code;
	public String name;

	private JjrtResultCodeEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static String getName(String code){
		String name = "";
		JjrtResultCodeEnum[] channelEnums = JjrtResultCodeEnum.values();
		for (JjrtResultCodeEnum channelEnum : channelEnums) {
			if (channelEnum.getCode().equals(code)) {
				name = channelEnum.getName();
				break;
			}
		}
		return name;
	}
	

}
