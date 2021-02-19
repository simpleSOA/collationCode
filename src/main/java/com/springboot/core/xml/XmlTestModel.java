package com.springboot.core.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CUBXML")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTestModel {
  @XmlElement(name = "ORDERINFO")
  private EposOrderInfoCommand orderInfo;
  @XmlElement(name = "CAVALUE")
  private String cavalue;


  public EposOrderInfoCommand getOrderInfo() {
    return orderInfo;
  }

  public void setOrderInfo(EposOrderInfoCommand orderInfo) {
    this.orderInfo = orderInfo;
  }

  public String getCavalue() {
    return cavalue;
  }

  public void setCavalue(String cavalue) {
    this.cavalue = cavalue;
  }
}
