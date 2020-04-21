package com.springboot.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {

  private int apiVersion;

  public ApiVersionCondition(int apiVersion) {
    this.apiVersion = apiVersion;
  }

  @Override
  public ApiVersionCondition combine(ApiVersionCondition other) {
    return new ApiVersionCondition(other.getApiVersion());
  }

  @Override
  public ApiVersionCondition getMatchingCondition(HttpServletRequest httpServletRequest) {
    return this;
  }

  @Override
  public int compareTo(ApiVersionCondition other,
      HttpServletRequest httpServletRequest) {
    return other.getApiVersion() - this.apiVersion;
  }

  public int getApiVersion() {
    return apiVersion;
  }
}
