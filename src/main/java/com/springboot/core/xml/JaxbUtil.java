package com.springboot.core.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jaxb2工具类
 */
public class JaxbUtil {

  private JaxbUtil() {
    throw new IllegalStateException("JaxbUtil class");
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(JaxbUtil.class);

  private static Map<String, JAXBContext> jaxbContextMap = new ConcurrentHashMap<>();


  /**
   * JavaBean转换成xml.
   */
  public static String convertToXml(Object obj) throws Exception {
    JAXBContext jaxbContext = jaxbContextMap.get(obj.getClass().getName());
    if (jaxbContext == null) {
      jaxbContext = JAXBContext.newInstance(obj.getClass());
      jaxbContextMap.put(obj.getClass().getName(), jaxbContext);
    }
    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

    StringWriter writer = new StringWriter();
    marshaller.marshal(obj, writer);
    return writer.toString();
  }

  /**
   * xml转成JavaBean.
   */
  @SuppressWarnings("unchecked")
  public static <T> T convertToJavaBean(String xml, Class<T> c) throws JAXBException {
    JAXBContext jaxbContext = jaxbContextMap.get(c.getName());
    if (jaxbContext == null) {
      jaxbContext = JAXBContext.newInstance(c);
      jaxbContextMap.put(c.getName(), jaxbContext);
    }
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return (T) unmarshaller.unmarshal(new StringReader(xml));
  }

  @SuppressWarnings("unchecked")
  public static <T> T getProxyObj(final Class<?> serviceInterface,
      InvocationHandler invocationHandler) {
    return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
        new Class<?>[]{serviceInterface},
        invocationHandler);
  }
}
