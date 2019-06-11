package com.jl.jasypt.bean;

/**
 * @author 蒋领
 * @date 2019年05月30日
 */
public class Process {
    private String text;
    private boolean encrypt;
    private boolean auto;
    private String salt;
    private String encType;
    /**
     * 输出格式
     * 0 自动  1 prop  2 yaml
     */
    private int outType;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public int getOutType() {
        return outType;
    }

    public void setOutType(int outType) {
        this.outType = outType;
    }

    public String getEncType() {
        return encType;
    }

    public void setEncType(String encType) {
        this.encType = encType;
    }
}
