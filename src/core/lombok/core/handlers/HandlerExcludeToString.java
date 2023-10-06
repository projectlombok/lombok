package lombok.core.handlers;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Handles the {@code ExcludeToString} annotation for javac.
 */
@Aspect
@Component
public class HandlerExcludeToString {


    /**
     * Gets params.
     *
     * @param joinPoint the join point
     * @throws Exception the exception
     */
    @Around("@annotation(src.core.lombok.ExcludeToString)")
    public void getParams(JoinPoint joinPoint) throws Exception {
        if(null != joinPoint){
            Object[] args = joinPoint.getArgs();
            List<Object> argList = new LinkedList<>(Arrays.asList(args));
            Map<String,Object> toStringMap = new LinkedHashMap<>();
            System.out.println("arg list size: "+argList.size());
            if(null != args && args.length > 0) {
                System.out.println("args:: " + args.toString());
                MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
                String[] parameters = methodSignature.getParameterNames();
                System.out.println("params:: "+parameters);

                ToString annotation = methodSignature.getMethod().getAnnotation(ToString.class);
                String[] exclusion = annotation.exclude();
                int argIndex = 0;
                for(String param : parameters){
                    toStringMap.put(param,args[argIndex]);
                    argIndex++;
                }
                if(null != exclusion && exclusion.length > 0){
                    for(String params : parameters){
                        for(String exclude : exclusion){
                            if(exclude.equals(params)){
                                toStringMap.remove(params);
                            }
                        }
                    }
                }
            }else{
                throw new Exception("There are no arguments for toString");
            }
            printToString(toStringMap,joinPoint);
        }else{
            throw new Exception("JoinPoint is null");
        }
    }

    /**
     * Print to string.
     *
     * @param toStringMap the to string map
     * @param joinPoint   the join point
     */
    private void printToString(Map<String, Object> toStringMap, JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        String methodName = methodSignature.getMethod().getName();
        StringBuffer customToString = new StringBuffer();
        customToString.append("\"");
        customToString.append(methodName)
                .append("{\" +\n");
        int count = 0;
        for (Map.Entry<String, Object> key : toStringMap.entrySet()) {
            customToString.append("\""+key.getKey())
                    .append("='\" + ").append(key.getValue()).append(" + '\\''");
            count++;
            if (count < toStringMap.size()) {
                customToString.append(", +\n");
            }
        }
        customToString.append("\n\"}\"");
        System.out.println(customToString);
    }

}