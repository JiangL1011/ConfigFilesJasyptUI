package com.jl.jasypt.utils;

import com.jl.jasypt.bean.Process;
import org.jasypt.util.text.BasicTextEncryptor;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 蒋领
 * @date 2019年05月31日
 */
public class ParseUtil {
    private static final String[] KEY_WORDS = {"username", "password", "key"};
    private static final String LINE_SEPARATOR = "\n";

    private static final Pattern ENC_PATTERN = Pattern.compile("ENC\\(.*\\)");
    private static final Pattern PROP_PATTERN = Pattern.compile("^([A-Za-z0-9]+\\.)*[A-Za-z0-9]*+\\s?=");
    private static final Pattern YAML_PATTERN = Pattern.compile("^[a-zA-Z0-9]+:");

    private static final String PROP_JOINT = "=";
    private static final String YAML_JOINT = ": ";

    public static Boolean isYaml(String text) {
        String[] lines = text.split(LINE_SEPARATOR);
        String line1 = lines[0];
        if (YAML_PATTERN.matcher(line1).find()) {
            return true;
        } else if (PROP_PATTERN.matcher(line1).find()) {
            return false;
        } else if (ENC_PATTERN.matcher(line1).find()) {
            return null;
        } else {
            throw new RuntimeException("解析异常");
        }
    }

    public static Properties toProperties(String text) throws IOException {
        Properties properties = new OrderProperties();
        properties.load(new ByteArrayInputStream(text.getBytes()));
        return properties;
    }

    public static Properties yamlStrToProp(String text) {
        Map map = toMap(text);
        Properties properties = new OrderProperties();
        mapToProp(map, "", properties);
        return properties;
    }

    public static String propStrToYamlStr(String propStr) throws IOException {
        Properties prop = new OrderProperties();
        prop.load(new ByteArrayInputStream(propStr.getBytes()));
        Map<String, Object> yamlMap = new HashMap<>(32);
        for (Object o : prop.keySet()) {
            String[] keys = ((String) o).split("\\.");
            buildYamlMap(yamlMap, keys, 0, prop.getProperty((String) o));
        }

        StringBuilder sb = new StringBuilder();
        yamlMapToYamlStr(yamlMap, sb, 0);

        return sb.toString();
    }

    public static String decrypt(Process process) {
        BasicTextEncryptor encryptor = getEncryptor(process.getSalt());
        String[] lines = process.getText().split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            Matcher matcher = ENC_PATTERN.matcher(line);
            if (matcher.find()) {
                String target = matcher.group();
                String encrypted = target.substring(4, target.length() - 1);
                String decrypt = encryptor.decrypt(encrypted);
                String replace = line.replace(target, decrypt);
                sb.append(replace).append("\n");
            } else {
                sb.append(line).append("\n");
            }
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static String encryptAll(Process process) {
        BasicTextEncryptor encryptor = getEncryptor(process.getSalt());
        String[] lines = process.getText().split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String encrypt = encryptor.encrypt(line);
            sb.append("ENC(").append(encrypt).append(")").append("\n");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static String parseByOutType(String out, int outType) throws IOException {
        Boolean yaml = isYaml(out);
        // 全文加密之后的格式既不是prop也不是yaml，所以直接返回即可
        if (yaml == null) {
            return out;
        }
        if (yaml) {
            if (outType == 1) {
                Properties properties = yamlStrToProp(out);
                StringBuilder sb = new StringBuilder();
                for (Object o : properties.keySet()) {
                    sb.append((String) o).append(PROP_JOINT).append(properties.get(o)).append("\n");
                }
                return sb.substring(0, sb.length() - 1);
            } else {
                return out;
            }
        } else {
            if (outType == 2) {
                return propStrToYamlStr(out);
            } else {
                return out;
            }
        }
    }

    private static boolean hasKeyWords(String key) {
        for (String keyWord : KEY_WORDS) {
            if (key.contains(keyWord)) {
                return true;
            }
        }
        return false;
    }

    private static BasicTextEncryptor getEncryptor(String salt) {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(salt);
        return encryptor;
    }

    private static void yamlMapToYamlStr(Map<String, Object> map, StringBuilder sb, int blankCount) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            for (int i = 0; i < blankCount; i++) {
                sb.append(" ");
            }
            if (value instanceof Map) {
                sb.append(key).append(YAML_JOINT).append(LINE_SEPARATOR);
                blankCount += 2;
                yamlMapToYamlStr((Map<String, Object>) value, sb, blankCount);
                blankCount -= 2;
            } else {
                sb.append(key).append(YAML_JOINT).append((String) value).append(LINE_SEPARATOR);
            }
        }
    }

    private static void buildYamlMap(Map<String, Object> map, String[] keys, int index, String value) {
        Map<String, Object> childMap;
        if (keys.length - 1 == index) {
            map.put(keys[index], value);
        } else {
            boolean b = map.containsKey(keys[index]);
            if (b) {
                childMap = (Map<String, Object>) map.get(keys[index]);
            } else {
                childMap = new HashMap<>(32);
                map.put(keys[index], childMap);
            }
            index++;
            buildYamlMap(childMap, keys, index, value);
        }
    }

    /**
     * @return prop格式的字符串
     */
    public static String autoEncryptOrDecrypt(Process process, Properties prop) {
        BasicTextEncryptor encryptor = getEncryptor(process.getSalt());

        StringBuilder sb = new StringBuilder();
        for (Object o : prop.keySet()) {
            String key = (String) o;
            String value = String.valueOf(prop.get(o));

            sb.append(key).append(PROP_JOINT);

            if (hasKeyWords(key)) {
                if (process.isEncrypt()) {
                    String encrypted = encryptor.encrypt(value);
                    sb.append("ENC(");
                    sb.append(encrypted);
                    sb.append(")");
                }
            } else {
                sb.append(value);
            }
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private static void mapToProp(Map map, String key, Properties properties) {
        for (Object k : map.keySet()) {
            Object v = map.get(k);
            String propKey;
            if ("".equals(key)) {
                propKey = (String) k;
            } else {
                propKey = key + "." + k;
            }
            if (v instanceof Map) {
                mapToProp((Map) v, propKey, properties);
            } else {
                properties.put(propKey, v == null ? "" : v);
            }
        }
    }

    private static Map toMap(String text) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(text, HashMap.class);
    }
}
