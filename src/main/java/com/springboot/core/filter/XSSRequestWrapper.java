package com.springboot.core.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class XSSRequestWrapper extends HttpServletRequestWrapper {

  private final static Safelist user_content_filter = Safelist.simpleText();

  static {
    user_content_filter.addTags("div", "p", "ul", "li", "br");
  }

  public XSSRequestWrapper(HttpServletRequest servletRequest) {
    super(servletRequest);
  }

  @Override
  public String[] getParameterValues(String parameter) {
    String[] values = super.getParameterValues(parameter);
    if (values == null) {
      return null;
    }

    int count = values.length;
    String[] encodedValues = new String[count];
    for (int i = 0; i < count; i++) {
      if (values[i] != null){
        encodedValues[i] = stripXSS(values[i]);
      }
    }

    return encodedValues;
  }

  @Override
  public String getParameter(String parameter) {
    String value = super.getParameter(parameter);
    if (StringUtils.isBlank(value)){
      return value;
    }
    return stripXSS(value);
  }

  @Override
  public String getHeader(String name) {
    String value = super.getHeader(name);
    return stripXSS(value);
  }

  private String stripXSS(String value) {
    if (StringUtils.isBlank(value)){
      return value;
    }
    return Jsoup.clean(value, user_content_filter);
  }
}
