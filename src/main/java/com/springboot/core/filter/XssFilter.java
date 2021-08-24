package com.springboot.core.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

public class XssFilter extends OncePerRequestFilter {

  private static final Set<String> types = new HashSet<>();
  public static List<String> whitelist = new ArrayList<>(10);
  static {
    types.add("css");
    types.add("png");
    types.add("jpg");
    types.add("jpeg");
    types.add("gif");
    types.add("js");
    types.add("ico");
    whitelist.add("/module/saveModuleInstance.json");
    whitelist.add("/email/addOrEdit.json");
    whitelist.add("/cms/instance/data/save.json");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String uri = request.getRequestURI();
    if (whitelist.contains(uri)){
      chain.doFilter(request, response);
    }else {
      String contentType = request.getContentType();
      if (StringUtils.isNotBlank(contentType) && contentType.contains("multipart/form-data")){
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(
            request.getSession().getServletContext());
        MultipartHttpServletRequest multipartRequest = commonsMultipartResolver.resolveMultipart(request);
        XSSRequestWrapper xssRequest = new XSSRequestWrapper(multipartRequest);
        chain.doFilter(xssRequest, response);
      }else {
        int index = uri.lastIndexOf('.');
        if (index != -1){
          String type = uri.substring(index + 1).toLowerCase();
          if (!types.contains(type)){
            chain.doFilter(new XSSRequestWrapper(request), response);
          }else {
            chain.doFilter(request, response);
          }
        }else {
          chain.doFilter(new XSSRequestWrapper(request), response);
        }
      }
    }
  }
}
