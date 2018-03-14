package me.kagura;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Service {
    //HTTP方法
    public static String[] methods = new String[]{"POST", "GET", "OPTIONS", "HEAD", "PUT", "DELETE", "TRACE", "CONNECT"};
    //最终生成的代码
    public static String generateCode = "";
    public String contentType = "";

    public String doGenerate(String rawString) throws UnsupportedEncodingException {
        generateCode = "";
        //解析出请求方法
        String method = "";
        for (String key : methods) {
            if (rawString.matches("\\s*" + key + " [\\d\\D]*")) {
                method = key;
                break;
            }
        }
        if (method.isEmpty()) {
            return "非标准HTTP RAW内容!";
        }

        //解析出请求链接
        String absUrl = "";
        Pattern pattern = Pattern.compile("\\s*" + method + " (http[\\d\\D]*) HTTP/\\d\\.?\\d?\\s");

        Matcher matcher = pattern.matcher(rawString);
        matcher.find();
        absUrl = matcher.group(1);

        //去除请求行
        rawString = rawString.replace(matcher.group(), "");

        //处理请求头
        String[] headers = rawString.contains("\r\n") ? rawString.split("\r\n") : rawString.split("\n");
        Map<String, String> headersMap = new HashMap<>();
        String cookiesStr = "";
        for (String header : headers) {
            if (header.isEmpty()) {
                break;
            }

            pattern = Pattern.compile("([\\d\\D]+): ([\\d\\D]+)");
            matcher = pattern.matcher(header);
            matcher.find();
            String key = matcher.group(1);
            String val = matcher.group(2);
            if (key.equals("Cookie")) {
                cookiesStr = val;
                continue;
            }

            headersMap.put(key, val.replaceAll("\"", "\\\\\""));
            if (key.equals("Content-Type")) {
                contentType = val;
            }
        }

        //处理Cookie
        String[] cookiesArray = cookiesStr.split(";");
        Map<String, String> cookieMap = new HashMap<>();
        for (String cookie : cookiesArray) {
            if (cookie.isEmpty()) {
                continue;
            }
            int first = cookie.indexOf("=");
            cookieMap.put(cookie.substring(0, first), cookie.substring(first + 1, cookie.length()));
        }

        String body = headers[headers.length - 1];
        //处理请求体
        Map<String, String> bodyMap = new HashMap<>();
        String bodyJson = "";
        if (contentType.contains("form")) {
            //提交方式为表单
            String decodeBody = URLDecoder.decode(body, "UTF-8");
            String[] formArray = decodeBody.split("&");
            for (String formItem : formArray) {
                System.err.println(formItem.split("=")[0] + "       " + formItem.split("=")[1]);
                String[] split = formItem.split("=");
                bodyMap.put(split[0], split[1].replaceAll("\"", "\\\\\""));
            }

        } else if (contentType.contains("json")) {
            //提交方式为JSON
            System.err.println(URLDecoder.decode(body, "UTF-8"));
            String decodeBody = URLDecoder.decode(body, "UTF-8");
            bodyJson = decodeBody;

        }

        //生成代码
        generateCode = generateCode += String.format("Connection.Response response = Jsoup.connect(\"%s\")", absUrl) + System.lineSeparator();
        generateCode = generateCode += String.format(".method(%s)", "Connection.Method." + method) + System.lineSeparator();
        if (!cookieMap.isEmpty()) {
            generateCode = generateCode += ".cookies(new HashMap<String, String>() {{" + System.lineSeparator();
            cookieMap.forEach((key, val) -> {
                generateCode = generateCode += String.format("    put(\"%s\", \"%s\");", key, val) + System.lineSeparator();
            });
            generateCode = generateCode += "}})" + System.lineSeparator();
        }

        generateCode = generateCode += ".headers(new HashMap<String, String>() {{" + System.lineSeparator();
        headersMap.forEach((key, val) -> {
            generateCode = generateCode += String.format("    put(\"%s\", \"%s\");", key, val) + System.lineSeparator();
        });
        generateCode = generateCode += "}})" + System.lineSeparator();

        if (contentType.contains("form")) {
            if (!bodyMap.isEmpty()) {
                //表单方式
                generateCode = generateCode += ".data(new HashMap<String, String>() {{" + System.lineSeparator();
                bodyMap.forEach((key, val) -> {
                    generateCode = generateCode += String.format("    put(\"%s\", \"%s\");", key, val) + System.lineSeparator();
                });
                generateCode = generateCode += "}})" + System.lineSeparator();
            }
        } else if (contentType.contains("json")) {
            //JSON方式
            generateCode = generateCode += String.format(".requestBody(\"%s\")", bodyJson.replaceAll("\"", "\\\\\"")) + System.lineSeparator();
        }
        generateCode = generateCode += ".ignoreContentType(true)" + System.lineSeparator();
        generateCode = generateCode += ".execute();" + System.lineSeparator();
        generateCode = generateCode += "\t//获取返回的原始body (字符串)" + System.lineSeparator();
        generateCode = generateCode += "String body = response.body();" + System.lineSeparator();
        generateCode = generateCode += "\t//将response转换为html Document" + System.lineSeparator();
        generateCode = generateCode += "Document document = response.parse();" + System.lineSeparator();
        generateCode = generateCode += "\t//将response转换为xml Document" + System.lineSeparator();
        generateCode = generateCode += "Document xmlDocument = Jsoup.parse(response.body(), response.parse().baseUri(), Parser.xmlParser());" + System.lineSeparator();
        generateCode = generateCode += "\t//将response转换为Base64结构的验证码字符串" + System.lineSeparator();
        generateCode = generateCode += "String responseToBase64Img = response == null ? null : \"data:image/jpeg;base64,\" + Base64.getEncoder().encodeToString(response.bodyAsBytes());" + System.lineSeparator();
        generateCode = generateCode += "\t//获取返回的BufferedInputStream" + System.lineSeparator();
        generateCode = generateCode += "BufferedInputStream bufferedInputStream = response.bodyStream();" + System.lineSeparator();

        return generateCode;
    }
}
