package com.jl.jasypt.controller;

import com.jl.jasypt.bean.Process;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Properties;

import static com.jl.jasypt.utils.ParseUtil.*;

/**
 * @author 蒋领
 * @date 2019年05月30日
 */
@Controller
public class BusinessController {

    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<String> process(Process process) {
        String outStr;
        if (!process.isEncrypt()) {
            outStr = decrypt(process);
        } else {
            if (process.isAuto()) {
                Boolean isYaml = isYaml(process.getText());
                if (isYaml != null && isYaml) {
                    Properties prop = yamlStrToProp(process.getText());
                    String propStr = autoEncrypt(process, prop);
                    try {
                        outStr = propStrToYamlStr(propStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(500).body(e.getMessage());
                    }
                } else {
                    try {
                        Properties prop = toProperties(process.getText());
                        outStr = autoEncrypt(process, prop);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(500).body(e.getMessage());
                    }
                }
            } else {
                outStr = encryptAll(process);
            }
        }

        try {
            return ResponseEntity.ok(parseByOutType(outStr, process.getOutType()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

}
