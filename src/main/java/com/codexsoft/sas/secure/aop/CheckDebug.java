package com.codexsoft.sas.secure.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.List;

@Aspect
@Component
public class CheckDebug {
//    @Before("execution(* com.codexsoft.sas.config.SpringConfig.*(..))")
    public void checkDebug(JoinPoint joinPoint) {
        List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        arguments.forEach(System.out::println);
        for(String val : arguments){
            //todo push to encrypted file
            if(val.contains("debug")|| val.contains("DEBUG") || val.contains("jdwp")|| val.contains("JDWP")|| val.contains("127.0.0.1")){
                System.exit(0);
            }
        }
    }
}
 


		/*
		* -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:63285,suspend=y,server=n
-Dfile.encoding=UTF-8
*/


		/*
		* -Didea.launcher.port=7533
-Didea.launcher.bin.path=/Applications/IntelliJ IDEA CE.app/Contents/bin
-Dfile.encoding=UTF-8
		* */


		/*
		* -Dmaven.multiModuleProjectDirectory=/Users/mikhail/work/sas-proxy
-Dmaven.home=/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3
-Dclassworlds.conf=/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3/bin/m2.conf
-Didea.launcher.port=7534
-Didea.launcher.bin.path=/Applications/IntelliJ IDEA CE.app/Contents/bin
-Dfile.encoding=UTF-8
		*
		* */


		/*
		-agentlib:jdwp=transport=dt_socket,address=127.0.0.1:63301,suspend=y,server=n
-Dmaven.multiModuleProjectDirectory=/Users/mikhail/work/sas-proxy
-Dmaven.home=/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3
-Dclassworlds.conf=/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3/bin/m2.conf
-Dfile.encoding=UTF-8
		* */