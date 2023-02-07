package cn.enums;

/**
 * 飓金嵘通证件类型
 * 
 */
public enum JjrtIdTypeEnum {

	J0000000("01" , "身份证"),
	J0001001("03" , "中国护照"),
	J0001002("04" , "军官证"),
	J0001003("05" , "武警证"),
	J0001004("06" , "港澳通行证"),
	J0001005("07" , "台胞证"),
	J0002001("08" , "外国护照"),
	J0101001("09" , "士兵证"),
	J0101002("10" , "临时身份证"),
	J0101003("11" , "户口本"),
	J0101004("12" , "警官证"),
	J0101005("13" , "外国人永久居留证"),
	J0101006("99" , "其他证件");
	
	
	public String code;
	public String name;

	private JjrtIdTypeEnum(String code, String name) {
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
		JjrtIdTypeEnum[] channelEnums = JjrtIdTypeEnum.values();
		for (JjrtIdTypeEnum channelEnum : channelEnums) {
			if (channelEnum.getCode().equals(code)) {
				name = channelEnum.getName();
				break;
			}
		}
		return name;
	}
	

}
