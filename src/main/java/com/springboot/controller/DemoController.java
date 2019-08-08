package com.springboot.controller;

import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.springboot.core.excel.ExcelModel;
import com.springboot.core.excel.ExcelUtils;
import com.springboot.core.excel.MapImportHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class DemoController {
    @RequestMapping("export")
    public void export(HttpServletResponse response) throws IOException {
        //模拟从数据库获取需要导出的数据
        List<ExcelModel> personList = new ArrayList<>();
        ExcelModel person1 = new ExcelModel("路飞", "1", "1354425615");
        ExcelModel person2 = new ExcelModel("娜美", "2", "1354425615");
        ExcelModel person3 = new ExcelModel("索隆", "1", "1354425615");
        ExcelModel person4 = new ExcelModel("小狸猫", "1", "1354425615");
        personList.add(person1);
        personList.add(person2);
        personList.add(person3);
        personList.add(person4);
        //导出操作
        ExcelUtils.exportExcel(personList, "test", ExcelModel.class, "海贼王", response);
    }

    @PostMapping("/import")
    public void importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<ExcelModel> list = ExcelUtils.importExcel(file, 0, 1, ExcelModel.class);
        list.forEach(System.out::println);
    }

    @RequestMapping("dynamicColumn")
    public void dynamicColumn(HttpServletResponse response) throws IOException {
        List<ExcelExportEntity> beanList = new ArrayList<>();
        beanList.add(new ExcelExportEntity("学生姓名", "name"));
        beanList.add(new ExcelExportEntity("学生性别", "sex"));
        beanList.add(new ExcelExportEntity("进校日期", "registrationDate"));
        beanList.add(new ExcelExportEntity("出生日期", "birthday"));
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("name", "李四");
        map.put("sex", "男");
        map.put("registrationDate", "1231231");
        map.put("birthday", "1232");
        list.add(map);
        Map<String, String> map1 = new HashMap<>();
        map1.put("name", "李四1");
        map1.put("sex", "男1");
        map1.put("registrationDate", "1231231aa");
        map1.put("birthday", "1232aaa");
        list.add(map1);
        ExcelUtils.exportExcel("sheetName", beanList, list, "动态列", response);
    }

    @PostMapping("/dynamicImportUser")
    public void dynamicColumnImport(@RequestParam("file") MultipartFile[] file) throws Exception {
        ImportParams params = new ImportParams();
        params.setDataHandler(new MapImportHandler());
        List<Map<String, Object>> list = ExcelUtils.importExcel(file[0], params, Map.class);
        List<User> rossUsers = list.stream().filter(m -> !m.isEmpty()).map(k -> new User(k.get("name") == null ? "" : k.get("name").toString(),
                k.get("email") == null ? "" : k.get("email").toString(), "")).collect(Collectors.toList());

        rossUsers.forEach(u -> System.out.println("db.user.update({\"username\":\"" + u.username + "\"},{$set:{\"email\":\"" + u.email + "\"}});"));

    }

    @PostMapping("/dynamicImportShop")
    public void dynamicColumnImport(@RequestParam("file") MultipartFile file) throws Exception {
        ImportParams params = new ImportParams();
        params.setDataHandler(new MapImportHandler());
        List<Map<String, Object>> list = ExcelUtils.importExcel(file, params, Map.class);
        List<User> users = list.stream().filter(m -> !m.isEmpty()).map(k -> new User(k.get("name") == null ? "" : k.get("name").toString(),
                k.get("username") == null ? "" : k.get("username").toString(), k.get("BG") == null ? "" : k.get("BG").toString())).collect(Collectors.toList());
        users.stream().map(u -> u.email).collect(Collectors.toSet()).forEach(System.out::println);
    }

    class User {
        public User() {
        }

        public User(String username, String email, String code) {
            this.code = code;
            this.email = email;
            this.username = username;
        }

        private String code;
        private String email;
        private String username;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("User{");
            sb.append("code='").append(code).append('\'');
            sb.append(", email='").append(email).append('\'');
            sb.append(", username='").append(username).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    class Shop {
        public Shop() {
        }

        public Shop(String name, String code, String createTime, String updateTime, String platform) {
            this.name = name;
            this.code = code;
            this.createTime = createTime;
            this.updateTime = updateTime;
            this.platform = platform;
        }

        private String name;
        private String code;
        private String createTime;
        private String updateTime;
        private String platform;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }
    }
}
